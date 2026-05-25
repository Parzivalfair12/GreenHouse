from flask import Flask, request, jsonify
from modelo import predict_next, detect_anomaly, get_risk_level, train_model
import numpy as np

app = Flask(__name__)

try:
    train_model()
except Exception as e:
    print(f"[IA] Model training note: {e}")


@app.route("/ia/health", methods=["GET"])
def health():
    return jsonify({"status": "UP", "service": "greenhouse-ia", "version": "1.0.0"})


@app.route("/ia/predict", methods=["POST"])
def predict():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Request body required"}), 400

    temperatures = data.get("temperature", [])
    humidities = data.get("humidity", [])

    temp_pred = predict_next(temperatures, "temperature") if temperatures else None
    hum_pred = predict_next(humidities, "humidity") if humidities else None

    temp_anomaly = detect_anomaly(temp_pred, temperatures) if temp_pred and temperatures else False
    hum_anomaly = detect_anomaly(hum_pred, humidities) if hum_pred and humidities else False

    risk = get_risk_level(temp_pred or 25, hum_pred or 60,
                          temperatures[-1] if temperatures else None,
                          humidities[-1] if humidities else None)

    return jsonify({
        "predictedTemperature": temp_pred,
        "predictedHumidity": hum_pred,
        "riskLevel": risk,
        "anomalies": {
            "temperature": temp_anomaly,
            "humidity": hum_anomaly
        }
    })


@app.route("/ia/recommend", methods=["POST"])
def recommend():
    data = request.get_json() or {}
    temp_pred = data.get("predictedTemperature", 25)
    hum_pred = data.get("predictedHumidity", 60)
    risk = data.get("riskLevel", "LOW")

    if risk == "HIGH":
        if temp_pred and temp_pred > 35:
            return jsonify({"action": "VENTILATE_GREENHOUSE", "reason": f"Alta temperatura pronosticada: {temp_pred}°C"})
        if hum_pred and hum_pred < 20:
            return jsonify({"action": "ACTIVATE_IRRIGATION", "reason": f"Humedad baja pronosticada: {hum_pred}%"})
        return jsonify({"action": "ALERT_OPERATOR", "reason": "Condiciones criticas detectadas"})

    if risk == "MEDIUM":
        if temp_pred and temp_pred > 30:
            return jsonify({"action": "ACTIVATE_FAN", "reason": f"Temperatura elevada: {temp_pred}°C"})
        if hum_pred and hum_pred < 40:
            return jsonify({"action": "INCREASE_IRRIGATION", "reason": f"Humedad moderadamente baja: {hum_pred}%"})
        return jsonify({"action": "MONITOR", "reason": "Condiciones estables con riesgo medio"})

    return jsonify({"action": "NORMAL_OPERATION", "reason": "Todas las condiciones dentro de parametros normales"})


@app.route("/ia/anomaly", methods=["POST"])
def check_anomaly():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Request body required"}), 400

    value = data.get("value")
    values = data.get("recentValues", [])
    sensor_type = data.get("type", "unknown")

    if value is None or not values:
        return jsonify({"anomaly": False, "message": "Datos insuficientes"})

    is_anomaly = __import__("modelo", fromlist=["detect_anomaly"]).detect_anomaly(value, values)

    return jsonify({
        "anomaly": is_anomaly,
        "sensorType": sensor_type,
        "value": value,
        "message": f"Anomalia detectada en {sensor_type}: {value}" if is_anomaly else "Lectura normal"
    })


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
