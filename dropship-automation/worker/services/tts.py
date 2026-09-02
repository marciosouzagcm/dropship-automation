"""
services/tts.py
-----------------
Geração de locução/dublagem sintética a partir do script gerado por
Gemini/Claude. Suporta dois provedores intercambiáveis via
`config.TTS_PROVIDER`: "elevenlabs" (qualidade de voz superior) ou
"openai" (fallback, mesma chave já usada para Whisper/GPT).
"""
import logging
from typing import Optional

import requests

from worker import config

logger = logging.getLogger("tts")

REQUEST_TIMEOUT = 30


def _gerar_elevenlabs(texto: str, output_path: str) -> Optional[str]:
    if not config.ELEVENLABS_API_KEY:
        logger.warning("[tts:elevenlabs] Chave ausente — pulando geração de áudio.")
        return None

    url = f"https://api.elevenlabs.io/v1/text-to-speech/{config.ELEVENLABS_VOICE_ID}"
    headers = {
        "xi-api-key": config.ELEVENLABS_API_KEY,
        "Content-Type": "application/json",
        "Accept": "audio/mpeg",
    }
    body = {
        "text": texto,
        "model_id": "eleven_multilingual_v2",
        "voice_settings": {"stability": 0.5, "similarity_boost": 0.75},
    }

    try:
        resp = requests.post(url, json=body, headers=headers, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        with open(output_path, "wb") as f:
            f.write(resp.content)
        return output_path
    except requests.RequestException as exc:
        logger.error("[tts:elevenlabs] Falha ao gerar áudio: %s", exc)
        return None


def _gerar_openai(texto: str, output_path: str) -> Optional[str]:
    if not config.OPENAI_API_KEY:
        logger.warning("[tts:openai] Chave ausente — pulando geração de áudio.")
        return None

    url = "https://api.openai.com/v1/audio/speech"
    headers = {
        "Authorization": f"Bearer {config.OPENAI_API_KEY}",
        "Content-Type": "application/json",
    }
    body = {"model": "tts-1", "voice": "alloy", "input": texto}

    try:
        resp = requests.post(url, json=body, headers=headers, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        with open(output_path, "wb") as f:
            f.write(resp.content)
        return output_path
    except requests.RequestException as exc:
        logger.error("[tts:openai] Falha ao gerar áudio: %s", exc)
        return None


def gerar_narracao(texto: str, output_path: str, provider: Optional[str] = None) -> Optional[str]:
    """
    Gera o arquivo de áudio da narração. `provider` sobrescreve
    `config.TTS_PROVIDER` quando informado (útil para fallback manual entre
    provedores caso um deles esteja fora do ar).
    """
    texto = (texto or "").strip()
    if not texto:
        logger.warning("[tts] Texto vazio — nada a narrar.")
        return None

    provedor = (provider or config.TTS_PROVIDER or "elevenlabs").lower()

    resultado = (
        _gerar_elevenlabs(texto, output_path)
        if provedor == "elevenlabs"
        else _gerar_openai(texto, output_path)
    )

    # Fallback automático: se o provedor primário falhar, tenta o outro
    if resultado is None:
        alternativo = "openai" if provedor == "elevenlabs" else "elevenlabs"
        logger.info("[tts] Tentando provedor alternativo: %s", alternativo)
        resultado = (
            _gerar_openai(texto, output_path)
            if alternativo == "openai"
            else _gerar_elevenlabs(texto, output_path)
        )

    return resultado
