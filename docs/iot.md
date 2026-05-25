# IoT — GreenHouse Sensor Simulator

## Arquitectura

```
IoT Simulator (Python)
    │ POST /api/readings (cada N segundos)
    ▼
Spring Boot Backend (OperationsService)
    ├── Crea lectura en BD
    ├── Evalua umbrales → genera alertas
    └── Activa reglas de automatizacion
```

## Sensores simulados

| Sensor | Tipo | Rango normal | Unidad |
|--------|------|-------------|--------|
| Temperatura | `TEMPERATURE` | 18-30 | °C |
| Humedad | `HUMIDITY` | 40-85 | % |
| Luz | `LIGHT` | 200-8000 | lux |
| CO2/Humedad suelo | `SOIL_MOISTURE` | 30-80 | % |

## Simulador (`iot/simulador.py`)

### Comportamiento

- Cada sensor tiene un valor base que fluctua con `drift` aleatorio
- Cada ciclo genera una lectura para cada sensor
- Probabilidad configurable de anomalia (valor extremo)
- Envia `POST /api/readings` al backend

### Alertas

El backend evalua automaticamente:
- Si el valor supera `maxThreshold` → alerta `WARNING` o `CRITICAL`
- Si el valor esta por debajo `minThreshold` → alerta
- Las alertas se muestran en el frontend

### Uso

```bash
cd iot
pip install requests

# Simulacion normal
python simulador.py --interval 5

# Alta probabilidad de anomalias
python simulador.py --interval 3 --anomaly-prob 0.3

# Backend personalizado
python simulador.py --interval 5 --backend http://localhost:8080
```

### Parametros

| Parametro | Default | Descripcion |
|-----------|---------|-------------|
| `--interval` | 5 | Segundos entre ciclos |
| `--anomaly-prob` | 0.1 | Probabilidad de anomalia (0-1) |
| `--backend` | `http://localhost:8080` | URL del backend |

### Logs

```
[INFO] Lectura enviada: TEMP-001=24.3C
[WARNING] Anomalia generada en TEMPERATURE: 41.2
[ALERT] [WARNING] Temperatura fuera de rango: 41.2C (limite 18-30)
```

## Integracion con IA

Las lecturas generadas por el IoT simulator alimentan:
- El dashboard principal (metricas en tiempo real)
- El modulo IA (predicciones basadas en historico)
- El sistema de alertas (deteccion de anomalias)
