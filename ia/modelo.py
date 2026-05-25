"""GreenHouse IA prediction model.

Uses linear regression to predict temperature and humidity,
detect anomalies via Z-score, and calculate risk levels.
"""

import numpy as np
from sklearn.linear_model import LinearRegression
import joblib
import os

MODEL_PATH = os.path.join(os.path.dirname(__file__), "data", "model.pkl")


def _ensure_model():
    """Train and cache model if not already saved."""
    if not os.path.exists(MODEL_PATH):
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
