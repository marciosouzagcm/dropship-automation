"""
services/captions.py
----------------------
Transcrição e alinhamento de legendas usando o modelo Whisper (OpenAI,
executado localmente via pacote `openai-whisper`). Gera um arquivo .srt
e, opcionalmente, "queima" a legenda diretamente no vídeo via FFmpeg
(filtro `subtitles`).

NOTA DE INFRAESTRUTURA: `openai-whisper` depende de PyTorch, o que aumenta
consideravelmente o tamanho da imagem Docker. Se o ambiente não tiver GPU,
use o modelo "base" ou "tiny" (ver WHISPER_MODEL_SIZE no .env) para manter
tempos de processamento aceitáveis em CPU.
"""
import logging
import subprocess
from typing import List, Optional

from worker import config

logger = logging.getLogger("captions")

_whisper_model = None  # cache do modelo carregado (evita recarregar a cada job)


def _carregar_modelo():
    global _whisper_model
    if _whisper_model is None:
        import whisper  # import tardio: só carrega a lib pesada quando necessário
        logger.info("[captions] Carregando modelo Whisper '%s'...", config.WHISPER_MODEL_SIZE)
        _whisper_model = whisper.load_model(config.WHISPER_MODEL_SIZE)
    return _whisper_model


def _segundos_para_srt_timestamp(segundos: float) -> str:
    horas = int(segundos // 3600)
    minutos = int((segundos % 3600) // 60)
    segs = segundos % 60
    milissegundos = int((segs - int(segs)) * 1000)
    return f"{horas:02d}:{minutos:02d}:{int(segs):02d},{milissegundos:03d}"


def transcrever_audio(audio_ou_video_path: str) -> Optional[List[dict]]:
    """
    Executa o Whisper sobre o arquivo de áudio/vídeo e retorna a lista de
    segmentos (cada um com `start`, `end`, `text`). Retorna None em caso
    de falha, permitindo que o worker siga sem legenda (fallback seguro).
    """
    try:
        modelo = _carregar_modelo()
        resultado = modelo.transcribe(audio_ou_video_path, language="pt")
        return resultado.get("segments", [])
    except Exception as exc:  # pylint: disable=broad-except
        logger.error("[captions] Falha na transcrição Whisper: %s", exc)
        return None


def gerar_srt(segmentos: List[dict], output_path: str) -> Optional[str]:
    """Converte os segmentos do Whisper em um arquivo .srt padrão."""
    if not segmentos:
        return None
    try:
        linhas = []
        for i, seg in enumerate(segmentos, start=1):
            inicio = _segundos_para_srt_timestamp(seg.get("start", 0.0))
            fim = _segundos_para_srt_timestamp(seg.get("end", 0.0))
            texto = (seg.get("text") or "").strip()
            linhas.append(f"{i}\n{inicio} --> {fim}\n{texto}\n")
        with open(output_path, "w", encoding="utf-8") as f:
            f.write("\n".join(linhas))
        return output_path
    except OSError as exc:
        logger.error("[captions] Falha ao escrever .srt: %s", exc)
        return None


def queimar_legenda_no_video(video_path: str, srt_path: str, output_path: str) -> Optional[str]:
    """
    Usa o filtro `subtitles` do FFmpeg para embutir a legenda diretamente
    nos pixels do vídeo (necessário para Reels/TikTok, que não suportam
    faixas de legenda separadas em todos os players).
    """
    cmd = [
        "ffmpeg", "-y", "-i", video_path,
        "-vf", f"subtitles={srt_path}:force_style='FontSize=16,PrimaryColour=&HFFFFFF&,Outline=2'",
        "-c:a", "copy", output_path,
    ]
    try:
        subprocess.run(cmd, check=True, capture_output=True, text=True)
        return output_path
    except subprocess.CalledProcessError as exc:
        logger.error("[captions] Falha ao queimar legenda: %s", exc.stderr)
        return None
    except FileNotFoundError:
        logger.error("[captions] Binário ffmpeg não encontrado.")
        return None


def pipeline_legendagem(video_path: str, output_dir: str, job_id: str) -> dict:
    """Orquestra: transcrever -> gerar .srt -> queimar no vídeo."""
    segmentos = transcrever_audio(video_path)
    if not segmentos:
        return {"video_com_legenda": video_path, "srt_path": None, "status": "sem_legenda"}

    srt_path = f"{output_dir}/{job_id}.srt"
    srt_gerado = gerar_srt(segmentos, srt_path)
    if not srt_gerado:
        return {"video_com_legenda": video_path, "srt_path": None, "status": "erro_srt"}

    video_final_path = f"{output_dir}/{job_id}_legendado.mp4"
    resultado = queimar_legenda_no_video(video_path, srt_gerado, video_final_path)

    return {
        "video_com_legenda": resultado or video_path,
        "srt_path": srt_gerado,
        "status": "concluido" if resultado else "erro_burn_in",
    }
