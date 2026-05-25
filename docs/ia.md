# IA — GreenHouse Prediction Service

## Arquitectura

```
Frontend (React)
    │ POST /api/ia/predict
    ▼
Spring Boot Backend (IaService)
    │ POST /ia/predict (RestTemplate)
    ▼
Flask IA Microservice (puerto 5000)
    ├── modelo.py       → Regresion lineal (scikit-learn)
    ├── app.py          → Endpoints REST
    └── tests/          → pytest suite
```

## Endpoints Flask

| Metodo | Ruta | Entrada | Salida |
|--------|------|---------|--------|
| GET | `/ia/health` | — | `{"status": "UP"}` |
| POST | `/ia/predict` | `{temperature: [28,29,30], humidity: [...]}` | `{predictedTemperature, riskLevel}` |
| POST | `/ia/recommend` | `{predictedTemperature, riskLevel}` | `{action, reason}` |
| POST | `/ia/anomaly` | `{value, recentValues, type}` | `{anomaly: bool, message}` |

## Modelo (`modelo.py`)

- **Regresion lineal**: entrena con 24 datos simulados (temperatura sinusoidal + ruido)
- **Prediccion**: usa el modelo para estimar el proximo valor
- **Deteccion de anomalias**: algoritmo Z-score (umbral > 2.5 desviaciones)
- **Nivel de riesgo**: basado en temperatura y humedad pronosticadas:
  - `HIGH`: temp > 35°C o hum < 20%
  - `MEDIUM`: temp 30-35°C o hum 20-40%
  - `LOW`: condiciones normales

## Spring Boot Integration (`IaService.java`)

- Usa `RestTemplate` para comunicarse con Flask
- Si Flask no responde, devuelve fallback `UNAVAILABLE`
- Configurable via `app.ia-url` (default: `http://localhost:5000`)

## Frontend Integration (`IaSection.jsx`)

- Componente React que consume `/api/ia/health`, `/api/ia/predict`, `/api/ia/recommend`
- Muestra: estado IA, predicciones, nivel de riesgo, recomendaciones
- Manejo de estado offline (Flask no disponible)

## Como ejecutar

```bash
cd ia
pip install -r requirements.txt
python app.py
# → http://localhost:5000

# Probar
curl http://localhost:5000/ia/health
```
