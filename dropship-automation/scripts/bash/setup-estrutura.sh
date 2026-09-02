#!/usr/bin/env bash
# =============================================================================
# setup-estrutura.sh
# Cria a estrutura modular de diretórios do projeto na raiz onde for executado.
# Uso: bash scripts/bash/setup-estrutura.sh
# =============================================================================
set -euo pipefail

echo ">> Criando estrutura de diretórios do projeto..."

# --- Workflows do n8n (JSON exportado de cada sub-agente) ---
mkdir -p workflows/sub-agente-1-mineracao
mkdir -p workflows/sub-agente-2-edicao
mkdir -p workflows/sub-agente-3-distribuicao

# --- Scripts de suporte (Python para processamento, Bash para operação) ---
mkdir -p scripts/python
mkdir -p scripts/bash

# --- Assets de mídia (separados por estágio do pipeline) ---
mkdir -p assets/raw          # vídeos/imagens originais minerados
mkdir -p assets/processed    # vídeos já editados/transformados
mkdir -p assets/audio        # locuções/áudio gerado (TTS)
mkdir -p assets/thumbnails   # capas/preview enviadas ao Telegram

# --- Templates reutilizáveis ---
mkdir -p templates/video     # overlays, marcas d'água, transições padrão
mkdir -p templates/captions  # templates de legenda/hook por nicho

# --- Schemas de dados (contratos entre sub-agentes) ---
mkdir -p schemas

# --- Docker (build customizado) ---
mkdir -p docker/python-ffmpeg

# --- Worker Python (pacote, ver /worker) ---
mkdir -p worker/services

# --- Documentação ---
mkdir -p docs

# --- Logs locais (fora do volume Docker, para debug rápido) ---
mkdir -p logs

# Mantém pastas vazias rastreáveis no Git
find assets workflows logs -type d -empty -exec touch {}/.gitkeep \;

echo ">> Estrutura criada com sucesso:"
tree -L 3 . 2>/dev/null || find . -maxdepth 3 -type d | sort
