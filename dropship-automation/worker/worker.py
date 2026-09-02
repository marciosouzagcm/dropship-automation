"""
worker.py
---------
Processo persistente do container `media-processor`. Escuta duas filas Redis
com responsabilidades distintas:

  1. `fila:jobs_mineracao` — jobs crus vindos do Sub-Agente 1 (n8n).
     O worker gera o affiliate_link, roda o pipeline de vídeo (corte de
     silêncio -> render vertical -> legenda -> narração) e monta o
     `product_payload` completo, enviando o resultado por HTTP ao webhook
     do Sub-Agente 2 para que ele poste no Telegram (ver `_notificar_aprovacao`).

  2. `fila:aprovados` — jobs já aprovados pelo operador via Telegram
     (publicados pelo Sub-Agente 3 após o callback). O worker então chama
     `services.distribution.distribuir` para publicar nos canais.

CORREÇÃO IMPORTANTE (auditoria v2.1): a versão anterior apenas dava RPUSH
em `fila:aguardando_aprovacao` ao final do pipeline de mineração, mas nenhum
node do n8n lia essa fila — o Sub-Agente 2 só é acionado por webhook HTTP
(`/webhook/conteudo-pronto-aprovacao`). Ou seja, o conteúdo processado nunca
chegava ao Telegram. Agora o worker chama esse webhook diretamente; o RPUSH
na fila é mantido apenas como registro histórico/auditoria (não é mais o
mecanismo de entrega).

Rodar localmente para debug:
    python3 -m worker.worker
"""
import json
import logging
import os
import time
from typing import Optional

import redis
import requests

from worker import config
from worker.services import affiliate, captions, distribution, tts, video_editor

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger("worker")


def conectar_redis() -> redis.Redis:
    """Instancia conexão resiliente com Redis evitando estouro de socket timeout no BLPOP."""
    return redis.Redis(
        host=config.REDIS_HOST,
        port=config.REDIS_PORT,
        password=config.REDIS_PASSWORD,  # None quando não configurado (dev sem auth)
        decode_responses=True,
        socket_timeout=None,  # Previne TimeoutError do socket no BLPOP
        socket_connect_timeout=10,
    )


def _get(d: Optional[dict], *chaves, default=None):
    """Optional chaining seguro para dicts aninhados: _get(payload, 'media', 'raw_videos', 0)."""
    atual = d
    for chave in chaves:
        if atual is None:
            return default
        try:
            atual = atual[chave]
        except (KeyError, IndexError, TypeError):
            return default
    return atual if atual is not None else default


def _baixar_video_se_necessario(video_ref: str, local_path: str) -> bool:
    """
    Verifica se video_ref é uma URL remota ou caminho local existente.
    Se for URL, realiza o download em streaming para local_path.
    """
    if os.path.exists(video_ref):
        return True

    if video_ref.startswith("http://") or video_ref.startswith("https://"):
        try:
            logger.info("Baixando vídeo remoto: %s -> %s", video_ref, local_path)
            response = requests.get(video_ref, stream=True, timeout=30)
            response.raise_for_status()

            os.makedirs(os.path.dirname(local_path), exist_ok=True)
            with open(local_path, "wb") as f:
                for chunk in response.iter_content(chunk_size=8192):
                    if chunk:
                        f.write(chunk)
            return True
        except requests.RequestException as exc:
            logger.error("Erro ao baixar vídeo de %s: %s", video_ref, exc)
            return False

    logger.error("Caminho local do vídeo não encontrado e não é uma URL válida: %s", video_ref)
    return False


def _notificar_aprovacao(product_payload: dict) -> bool:
    """
    Envia o product_payload pronto ao webhook do Sub-Agente 2 (n8n), que é
    quem efetivamente monta a mensagem e envia ao Telegram para aprovação.

    Esta chamada é o elo que faltava entre o worker e o n8n — sem ela, o
    conteúdo processado ficava apenas armazenado no Redis, sem nunca chegar
    ao operador. Retorna True/False para o chamador decidir se tenta de novo
    ou apenas loga o problema (nunca lança exceção para não derrubar o loop).
    """
    url = f"{config.N8N_WEBHOOK_URL}{config.N8N_WEBHOOK_APROVACAO_PATH}"
    headers = {"x-webhook-secret": config.WEBHOOK_SHARED_SECRET} if config.WEBHOOK_SHARED_SECRET else {}
    try:
        resp = requests.post(url, json=product_payload, headers=headers, timeout=15)
        resp.raise_for_status()
        logger.info(
            "[notificar_aprovacao] Job %s enviado ao Sub-Agente 2 (%s) -> HTTP %s",
            product_payload.get("product_id"),
            url,
            resp.status_code,
        )
        return True
    except requests.RequestException as exc:
        logger.error(
            "[notificar_aprovacao] Falha ao notificar o Sub-Agente 2 para o job %s: %s",
            product_payload.get("product_id"),
            exc,
        )
        return False


# ---------------------------------------------------------------------------
# PIPELINE 1: mineração -> conteúdo pronto para aprovação
# ---------------------------------------------------------------------------
def processar_job_mineracao(job: dict) -> dict:
    job_id = job.get("product_id") or job.get("job_id") or f"job_{int(time.time())}"
    logger.info("[pipeline] Iniciando processamento de %s", job_id)

    # --- 1) Link de afiliado padronizado ---
    origem = job.get("origem") or _get(job, "origem", default="")
    raw_url = job.get("raw_product_url") or _get(job, "produto", "url_produto", default="")
    affiliate_link = affiliate.gerar_affiliate_link(origem, raw_url)

    # --- 2) Vídeo bruto de entrada ---
    raw_video = (
        job.get("video_url")
        or job.get("raw_video_path")
        or _get(job, "media", "raw_videos", 0)
        or _get(job, "midia_referencia", 0, "url")
    )

    if not raw_video:
        logger.error("[pipeline] Job %s sem vídeo bruto — abortando.", job_id)
        return _payload_erro(job, job_id, affiliate_link, "sem_video_bruto")

    input_local_path = f"{config.RAW_DIR}/{job_id}_raw.mp4"

    # Faz o download do arquivo se for URL remota ou valida existência local
    if not _baixar_video_se_necessario(raw_video, input_local_path):
        return _payload_erro(job, job_id, affiliate_link, "falha_download_video_bruto")

    # Garante que o input_path apontará para o arquivo local resolvido
    path_para_edicao = input_local_path if os.path.exists(input_local_path) else raw_video

    hook_text = job.get("hook_text") or "Você precisa ver isso 👀"

    resultado_video = video_editor.pipeline_completo(
        input_path=path_para_edicao,
        output_dir=config.PROCESSED_DIR,
        job_id=job_id,
        hook_text=hook_text,
    )

    if resultado_video.get("status") != "concluido":
        return _payload_erro(job, job_id, affiliate_link, "falha_edicao_video")

    video_path = resultado_video["rendered_video_path"]

    # --- 3) Legendagem automática (Whisper) ---
    legendagem = captions.pipeline_legendagem(video_path, config.PROCESSED_DIR, job_id)
    video_final = legendagem.get("video_com_legenda", video_path)

    # --- 4) Narração sintética (opcional, quando há script de locução) ---
    script_narracao = job.get("script_narracao")
    audio_path = None
    if script_narracao:
        audio_path = tts.gerar_narracao(
            script_narracao, f"{config.AUDIO_DIR}/{job_id}_narracao.mp3"
        )

    # --- 5) Monta o product_payload unificado (contrato entre sub-agentes) ---
    payload = {
        "product_id": job_id,
        "title": job.get("title") or _get(job, "produto", "titulo", default=""),
        "description": job.get("description") or "",
        "raw_product_url": raw_url,
        "affiliate_link": affiliate_link,
        "category": job.get("category") or _get(job, "produto", "categoria", default=""),
        "price": job.get("price") or {
            "original": _get(job, "produto", "preco", default=0.0),
            "promotional": _get(job, "produto", "preco", default=0.0),
            "currency": "BRL",
        },
        "media": {
            "raw_images": _get(job, "media", "raw_images", default=[]),
            "raw_videos": [raw_video],
            "rendered_video_path": video_final,
            "thumbnail_path": resultado_video.get("thumbnail_path"),
        },
        "copywriting": {
            "telegram_post": job.get("telegram_post") or "",
            "instagram_caption": job.get("instagram_caption") or "",
            "tiktok_caption": job.get("tiktok_caption") or "",
            "hashtags": job.get("hashtags") or [],
        },
        "approval": {"status": "PENDING", "approved_at": None},
        "extras": {"audio_narracao_path": audio_path, "srt_path": legendagem.get("srt_path")},
    }
    return payload


def _payload_erro(job: dict, job_id: str, affiliate_link: str, motivo: str) -> dict:
    return {
        "product_id": job_id,
        "title": job.get("title") or "",
        "affiliate_link": affiliate_link,
        "media": {"rendered_video_path": None, "thumbnail_path": None},
        "approval": {"status": "ERROR", "approved_at": None},
        "erro": motivo,
    }


# ---------------------------------------------------------------------------
# PIPELINE 2: aprovado -> distribuição multicanal
# ---------------------------------------------------------------------------
def processar_job_aprovado(product_payload: dict) -> dict:
    job_id = product_payload.get("product_id", "desconhecido")
    logger.info("[distribuicao] Publicando job aprovado: %s", job_id)
    resultados = distribution.distribuir(product_payload)
    logger.info("[distribuicao] Resultado job %s: %s", job_id, resultados)
    return resultados


# ---------------------------------------------------------------------------
# LOOP PRINCIPAL — multiplexa as duas filas com BLPOP
# ---------------------------------------------------------------------------
def loop_principal() -> None:
    r = conectar_redis()
    logger.info(
        "Worker conectado ao Redis (%s:%s). Aguardando jobs em '%s' e '%s'...",
        config.REDIS_HOST,
        config.REDIS_PORT,
        config.FILA_JOBS_MINERACAO,
        config.FILA_DISTRIBUICAO,
    )
    while True:
        try:
            item = r.blpop([config.FILA_JOBS_MINERACAO, config.FILA_DISTRIBUICAO], timeout=5)
        except redis.exceptions.TimeoutError:
            item = None
        except redis.exceptions.RedisError as exc:
            logger.error("Erro na conexão Redis durante BLPOP: %s", exc)
            time.sleep(2)
            r = conectar_redis()
            continue

        if item is None:
            continue

        fila_origem, payload_str = item
        try:
            job = json.loads(payload_str)
        except json.JSONDecodeError as exc:
            logger.error("Payload JSON inválido recebido em %s: %s", fila_origem, exc)
            continue

        if fila_origem == config.FILA_JOBS_MINERACAO:
            resultado = processar_job_mineracao(job)

            # Registro histórico/auditoria em Redis (não é mais o mecanismo
            # de entrega — ver docstring do módulo e `_notificar_aprovacao`).
            r.rpush(config.FILA_APROVACAO, json.dumps(resultado, ensure_ascii=False))

            # Entrega de fato: chama o webhook do Sub-Agente 2 para que ele
            # poste no Telegram. Se falhar, o job já está salvo em
            # FILA_APROVACAO e pode ser reenviado manualmente depois.
            _notificar_aprovacao(resultado)

            logger.info("Job processado e notificado: %s", resultado.get("product_id"))
        elif fila_origem == config.FILA_DISTRIBUICAO:
            processar_job_aprovado(job)


if __name__ == "__main__":
    try:
        loop_principal()
    except KeyboardInterrupt:
        logger.info("Worker encerrado pelo usuário.")
