FROM python:3.11-slim

WORKDIR /app

# psycopg2-binary provides pre-compiled wheels; no build deps needed
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app.py .
COPY modelo.py .
COPY data/ ./data/

EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:5000/ia/health || exit 1
CMD ["gunicorn", "-b", "0.0.0.0:5000", "--timeout", "30", "--workers", "1", "--log-level", "info", "app:app"]
