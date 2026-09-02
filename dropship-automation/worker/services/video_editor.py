"""
services/video_editor.py
-------------------------
Pipeline de edição transformativa: normaliza o vídeo bruto minerado para o
formato vertical 9:16 (Shorts/Reels/TikTok), remove silêncios longos com
FFmpeg e aplica overlays de texto (hook/legenda) via MoviePy.

Todas as funções recebem/retornam caminhos de arquivo (strings) para
manterem-se independentes de estado — facilita rodar cada etapa isolada
em testes ou reprocessar apenas um estágio do pipeline.
"""
import logging
import subprocess
from typing import List, Optional, Tuple

from moviepy.editor import (
    CompositeVideoClip,
    TextClip,
    VideoFileClip,
    concatenate_videoclips,
)

logger = logging.getLogger("video_editor")

TARGET_WIDTH = 1080
TARGET_HEIGHT = 1920  # proporção 9:16


def _run_ffmpeg(cmd: List[str]) -> bool:
    """Executa um comando FFmpeg capturando stderr para log em caso de erro."""
    try:
        subprocess.run(cmd, check=True, capture_output=True, text=True)
        return True
    except subprocess.CalledProcessError as exc:
        logger.error("[ffmpeg] Comando falhou: %s\nSTDERR: %s", " ".join(cmd), exc.stderr)
        return False
    except FileNotFoundError:
        logger.error("[ffmpeg] Binário ffmpeg não encontrado no PATH do container.")
        return False


def detectar_silencios(
    input_path: str, limiar_db: float = -35.0, duracao_minima: float = 0.6
) -> List[Tuple[float, float]]:
    """
    Usa o filtro `silencedetect` do FFmpeg para localizar trechos de silêncio.
    Retorna lista de tuplas (inicio, fim) em segundos.
    """
    cmd = [
        "ffmpeg", "-i", input_path,
        "-af", f"silencedetect=noise={limiar_db}dB:d={duracao_minima}",
        "-f", "null", "-",
    ]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True)
    except FileNotFoundError:
        logger.error("[ffmpeg] Binário ffmpeg não encontrado.")
        return []

    silencios: List[Tuple[float, float]] = []
    inicio_atual: Optional[float] = None
    for linha in result.stderr.splitlines():
        if "silence_start" in linha:
            try:
                inicio_atual = float(linha.split("silence_start:")[1].strip())
            except (IndexError, ValueError):
                inicio_atual = None
        elif "silence_end" in linha and inicio_atual is not None:
            try:
                fim = float(linha.split("silence_end:")[1].split("|")[0].strip())
                silencios.append((inicio_atual, fim))
            except (IndexError, ValueError):
                pass
            inicio_atual = None
    return silencios


def remover_silencios(input_path: str, output_path: str) -> str:
    """
    Remove trechos de silêncio > 0.6s do vídeo, gerando um corte dinâmico
    mais engajante. Se nenhum silêncio for detectado ou o FFmpeg falhar,
    retorna o próprio input_path (fallback seguro — nunca quebra o
    pipeline por causa desta etapa opcional).
    """
    silencios = detectar_silencios(input_path)
    if not silencios:
        logger.info("[video_editor] Nenhum silêncio relevante detectado, mantendo original.")
        return input_path

    try:
        clip = VideoFileClip(input_path)
        duracao_total = clip.duration
        cortes_manter = []
        cursor = 0.0
        for inicio, fim in silencios:
            if inicio > cursor:
                cortes_manter.append(clip.subclip(cursor, inicio))
            cursor = fim
        if cursor < duracao_total:
            cortes_manter.append(clip.subclip(cursor, duracao_total))

        if not cortes_manter:
            clip.close()
            return input_path

        final = concatenate_videoclips(cortes_manter)
        final.write_videofile(output_path, codec="libx264", audio_codec="aac", logger=None)
        clip.close()
        final.close()
        return output_path
    except Exception as exc:  # pylint: disable=broad-except
        logger.error("[video_editor] Falha ao remover silêncios: %s", exc)
        return input_path


def renderizar_vertical(
    input_path: str,
    output_path: str,
    hook_text: Optional[str] = None,
    marca_dagua_text: Optional[str] = None,
) -> Optional[str]:
    """
    Normaliza o vídeo para 1080x1920 (9:16), centraliza com crop/pad e
    aplica overlays de texto (hook no topo, marca d'água no rodapé).
    Retorna o caminho final ou None em caso de falha irrecuperável.
    """
    try:
        clip = VideoFileClip(input_path)

        # --- Redimensiona mantendo proporção e faz crop central para 9:16 ---
        clip_ratio = clip.w / clip.h
        target_ratio = TARGET_WIDTH / TARGET_HEIGHT

        if clip_ratio > target_ratio:
            # vídeo mais "largo" que o alvo -> ajusta pela altura e corta laterais
            clip_resized = clip.resize(height=TARGET_HEIGHT)
            excedente = (clip_resized.w - TARGET_WIDTH) / 2
            clip_final = clip_resized.crop(x1=excedente, x2=clip_resized.w - excedente)
        else:
            # vídeo mais "alto"/estreito -> ajusta pela largura e corta topo/base
            clip_resized = clip.resize(width=TARGET_WIDTH)
            excedente = (clip_resized.h - TARGET_HEIGHT) / 2
            clip_final = clip_resized.crop(y1=excedente, y2=clip_resized.h - excedente)

        camadas = [clip_final]

        if hook_text:
            hook_clip = (
                TextClip(
                    hook_text, fontsize=64, color="white", font="DejaVu-Sans-Bold",
                    stroke_color="black", stroke_width=2, method="caption",
                    size=(TARGET_WIDTH - 120, None),
                )
                .set_position(("center", 140))
                .set_duration(clip_final.duration)
            )
            camadas.append(hook_clip)

        if marca_dagua_text:
            marca_clip = (
                TextClip(marca_dagua_text, fontsize=30, color="white", font="DejaVu-Sans")
                .set_position(("center", TARGET_HEIGHT - 100))
                .set_duration(clip_final.duration)
                .set_opacity(0.8)
            )
            camadas.append(marca_clip)

        composto = CompositeVideoClip(camadas, size=(TARGET_WIDTH, TARGET_HEIGHT))
        composto.write_videofile(output_path, codec="libx264", audio_codec="aac", logger=None)

        clip.close()
        composto.close()
        return output_path
    except Exception as exc:  # pylint: disable=broad-except
        logger.error("[video_editor] Falha ao renderizar vídeo vertical: %s", exc)
        return None


def gerar_thumbnail(video_path: str, output_path: str, timestamp: float = 1.0) -> Optional[str]:
    """Extrai um frame do vídeo para usar como thumbnail/preview no Telegram."""
    cmd = [
        "ffmpeg", "-y", "-ss", str(timestamp), "-i", video_path,
        "-frames:v", "1", "-q:v", "2", output_path,
    ]
    return output_path if _run_ffmpeg(cmd) else None


def pipeline_completo(
    input_path: str,
    output_dir: str,
    job_id: str,
    hook_text: Optional[str] = None,
    marca_dagua_text: str = "@sualoja",
) -> dict:
    """
    Orquestra as etapas: remover silêncio -> renderizar vertical com overlays
    -> gerar thumbnail. Retorna um dicionário com os caminhos gerados,
    já no formato esperado pela chave `media` do schema `product_payload`.
    """
    sem_silencio_path = f"{output_dir}/{job_id}_sem_silencio.mp4"
    final_path = f"{output_dir}/{job_id}.mp4"
    thumb_path = f"{output_dir}/{job_id}_thumb.jpg"

    etapa1 = remover_silencios(input_path, sem_silencio_path)
    etapa2 = renderizar_vertical(etapa1, final_path, hook_text, marca_dagua_text)

    if etapa2 is None:
        logger.error("[video_editor] Pipeline falhou no job %s", job_id)
        return {"rendered_video_path": None, "thumbnail_path": None, "status": "erro"}

    thumb = gerar_thumbnail(etapa2, thumb_path)

    return {
        "rendered_video_path": etapa2,
        "thumbnail_path": thumb,
        "status": "concluido",
    }
