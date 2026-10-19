FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

COPY requirements.txt ./requirements.txt
RUN pip install --no-cache-dir --disable-pip-version-check -r requirements.txt \
    && addgroup --system --gid 10001 app \
    && adduser --system --uid 10001 --gid 10001 --no-create-home app

COPY --chown=10001:10001 rpa/ ./rpa/
COPY --chown=10001:10001 docs/ ./docs/

USER 10001:10001

ENTRYPOINT ["python", "rpa/extrair_clientes.py"]
CMD ["--dry-run"]