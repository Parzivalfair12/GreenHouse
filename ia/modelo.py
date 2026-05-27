"""GreenHouse IA prediction model with PostgreSQL integration.

Fetches real historical readings from PostgreSQL,
trains models on actual data, and returns predictions.
"""

import logging
import os
import numpy as np
import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures
import joblib
import psycopg2
from psycopg2.extras import RealDictCursor

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

MODEL_PATH = os.path.join(os.path.dirname(__file__), "data", "model.pkl")

DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": os.getenv("DB_PORT", "5432"),
    "database": os.getenv("DB_NAME", "greenhouse"),
    "user": os.getenv("DB_USER", "postgres"),
    "password": os.getenv("DB_PASS", "postgres")
}


def get_db_connection():
    return psycopg2.connect(**DB_CONFIG)


def fetch_recent_readings(sensor_type=None, limit=50):
    """Fetch recent readings from PostgreSQL."""
    try:
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        if sensor_type:
            cursor.execute(
                """SELECT r.reading_value as value, r.recorded_at, s.type, s.code
                   FROM reading r
                   JOIN sensor s ON r.sensor_id = s.id
                   WHERE s.type = %s
                   ORDER BY r.recorded_at DESC
                   LIMIT %s""",
                (sensor_type, limit)
            )
        else:
            cursor.execute(
                """SELECT r.reading_value as value, r.recorded_at, s.type, s.code
                   FROM reading r
                   JOIN sensor s ON r.sensor_id = s.id
                   ORDER BY r.recorded_at DESC
                   LIMIT %s""",
                (limit,)
            )
        rows = cursor.fetchall()
        conn.close()
        return rows
    except Exception as e:
        logger.error("DB fetch error: %s", e)
        return []


def _ensure_model():
    """Train and cache model if not already saved."""
    if not os.path.exists(MODEL_PATH):
        # Train on dummy data initially; will retrain on real data
        X = np.arange(1, 25).reshape(-1, 1)
        y_temp = 20 + np.sin(X.ravel() * 0.3) * 5 + np.random.normal(0, 0.5, 24)
        y_hum = 60 + np.cos(X.ravel() * 0.3) * 10 + np.random.normal(0, 1, 24)

        model_temp = LinearRegression()
        model_hum = LinearRegression()
        model_temp.fit(X, y_temp)
        model_hum.fit(X, y_hum)

        joblib.dump({"temperature": model_temp, "humidity": model_hum}, MODEL_PATH)


def load_models():
    _ensure_model()
    return joblib.load(MODEL_PATH)


def predict_next(values, model_key):
    """Predict the next value based on recent readings."""
    if not values:
        return 0.0
    if len(values) < 2:
        return round(float(values[-1]), 1)
    try:
        models = load_models()
        model = models.get(model_key)
        if model is None:
            return round(float(np.mean(values[-3:])), 1)
        next_step = len(values) + 1
        pred = model.predict([[next_step]])
        return round(float(pred[0]), 1)
    except Exception:
        return round(float(np.mean(values[-3:])), 1)


def predict_with_trend(values, steps=1):
    """Predict next value using linear regression on recent data."""
    if not values or len(values) < 2:
        return round(float(values[-1]), 1) if values else 0.0
    try:
        X = np.arange(len(values)).reshape(-1, 1)
        y = np.array(values)
        model = LinearRegression()
        model.fit(X, y)
        pred = model.predict([[len(values) + steps - 1]])
        return round(float(pred[0]), 1)
    except Exception:
        return round(float(np.mean(values[-3:])), 1)


def detect_anomaly(value, recent_values, threshold=2.5):
    """Detect if a value is anomalous using Z-score."""
    if len(recent_values) < 3:
        return False
    mean = float(np.mean(recent_values))
    std = float(np.std(recent_values))
    if std == 0:
        return False
    z_score = abs(float(value) - mean) / std
    return bool(z_score > threshold)


def get_risk_level(temp_pred, hum_pred, temp_current=None, hum_current=None):
    """Calculate risk level from predicted values."""
    risk = 0
    if temp_pred is not None and (temp_pred > 32 or temp_pred < 8):
        risk += 2 if temp_pred > 35 else 1
    if temp_pred is not None and temp_pred > 35:
        risk += 2
    if hum_pred is not None and (hum_pred > 90 or hum_pred < 25):
        risk += 2
    if hum_pred is not None and hum_pred < 20:
        risk += 1
    if risk >= 4:
        return "HIGH"
    if risk >= 2:
        return "MEDIUM"
    return "LOW"


def get_recommendation(temp_pred, hum_pred, risk):
    """Generate recommendation based on predictions."""
    if risk == "HIGH":
        if temp_pred and temp_pred > 35:
            return "Activar ventilacion inmediatamente. Temperatura critica pronosticada."
        if hum_pred and hum_pred < 25:
            return "Activar riego urgente. Humedad critica pronosticada."
        return "Condiciones criticas detectadas. Revisar sistema completo."
    if risk == "MEDIUM":
        if temp_pred and temp_pred > 30:
            return "Preparar ventilacion. Temperatura elevada pronosticada."
        if hum_pred and hum_pred < 40:
            return "Aumentar riego gradualmente. Humedad baja pronosticada."
        return "Monitoreo activo recomendado. Condiciones estables con riesgo medio."
    return "Operacion normal. Condiciones dentro de parametros."


def predict_from_db(sensor_type, limit=50):
    """Fetch real readings from DB and predict next value."""
    rows = fetch_recent_readings(sensor_type, limit)
    if not rows:
        return None
    values = [float(r["value"]) for r in reversed(rows)]
    return predict_with_trend(values)
