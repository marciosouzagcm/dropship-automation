# =============================================================================
# Imagem customizada: Python 3.11 + FFmpeg + ImageMagick + libs de edição
# Usada pelo Sub-Agente 2 (Edição & Criação Transformativa de Mídia)
# =============================================================================
FROM python:3.11-slim

# Evita prompts interativos durante instalação de pacotes do sistema
ENV DEBIAN_FRONTEND=noninteractive

# Define o caminho do executável do ImageMagick para o MoviePy encontrar
ENV IMAGEMAGICK_BINARY=/usr/bin/convert

# Instala FFmpeg, ImageMagick e dependências de sistema necessárias
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    imagemagick \
    libsm6 \
    libxext6 \
    fonts-dejavu-core \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

# Libera a política do ImageMagick para processar textos/fontes sem restrição de política de segurança
RUN sed -i 's/rights="none" pattern="PDF"/rights="read | write" pattern="PDF"/' /etc/ImageMagick-6/policy.xml || true

WORKDIR /app

# Copia e instala dependências Python primeiro (aproveita cache de camadas).
COPY worker/requirements.txt /tmp/requirements.txt
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r /tmp/requirements.txt

# Diretórios padrão (também montados via volume no docker-compose)
RUN mkdir -p /data/assets /data/templates /app/worker

# NOTA DE SEGURANÇA (não aplicado automaticamente): este container roda
# como root. O ideal seria um usuário dedicado (USER worker_user), mas
# como /data/assets e /app/worker são bind mounts do host (ver
# docker-compose.yml), trocar de usuário sem mapear o UID/GID do host
# quebraria a permissão de escrita nesses volumes. Se for endurecer isso,
# crie o usuário com o mesmo UID do dono das pastas no host:
#   docker build --build-arg UID=$(id -u) --build-arg GID=$(id -g) ...
CMD ["python3", "-m", "worker.worker"]
