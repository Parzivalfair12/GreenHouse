import { useEffect, useState } from 'react';
import { AlertTriangle, Cpu, FlaskConical, Thermometer, Droplets, Shield, TrendingUp } from 'lucide-react';
import { Section, Panel, Metric } from './shared.jsx';

const RISK_COLORS = {
  LOW: { bg: 'rgba(0, 204, 122, 0.12)', text: '#00cc7a' },
  MEDIUM: { bg: 'rgba(217, 164, 0, 0.12)', text: '#d9a400' },
  HIGH: { bg: 'rgba(255, 91, 91, 0.12)', text: '#ff5b5b' },
  UNAVAILABLE: { bg: 'rgba(156, 163, 175, 0.12)', text: '#9ca3af' }
};

export function IaPreview({ readings, sensors }) {
  const [iaOnline, setIaOnline] = useState(null);

  useEffect(() => {
    fetch('/api/ia/health')
      .then((r) => setIaOnline(r.ok ? 'UP' : 'DOWN'))
      .catch(() => setIaOnline('DOWN'));
  }, []);

  if (iaOnline === null) return '...';
  return iaOnline === 'UP' ? 'Online' : 'Offline';
}

export function IaSection({ readings, sensors, t }) {
  const [prediction, setPrediction] = useState(null);
  const [recommendation, setRecommendation] = useState(null);
  const [iaHealth, setIaHealth] = useState(null);

  useEffect(() => {
    fetchIaHealth();
    if (readings.length >= 2) {
      fetchPrediction();
    }
  }, [readings.length]);

  function getReadingsByType(type) {
    const typeSensors = sensors.filter((s) => s.type === type);
    const sensorIds = new Set(typeSensors.map((s) => s.id));
    return readings
      .filter((r) => sensorIds.has(r.sensorId))
      .slice(-12)
      .map((r) => Number(r.value));
  }

  async function fetchIaHealth() {
    try {
      const resp = await fetch('/api/ia/health');
      setIaHealth(resp.ok ? 'UP' : 'DOWN');
    } catch {
      setIaHealth('DOWN');
    }
  }

  async function fetchPrediction() {
    const temps = getReadingsByType('TEMPERATURE');
    const hums = getReadingsByType('HUMIDITY');
    if (temps.length < 2 && hums.length < 2) return;

    try {
      const resp = await fetch('/api/ia/predict', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ temperature: temps, humidity: hums })
      });
      if (resp.ok) {
        const data = await resp.json();
        setPrediction(data);
        fetchRecommendation(data);
      }
    } catch {}
  }

  async function fetchRecommendation(pred) {
    if (!pred || pred.riskLevel === 'UNAVAILABLE') return;
    try {
      const resp = await fetch('/api/ia/recommend', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          predictedTemperature: pred.predictedTemperature,
          predictedHumidity: pred.predictedHumidity,
          riskLevel: pred.riskLevel
        })
      });
      if (resp.ok) {
        setRecommendation(await resp.json());
      }
    } catch {}
  }

  const riskColor = RISK_COLORS[prediction?.riskLevel] ?? RISK_COLORS.UNAVAILABLE;
  const iaOnline = iaHealth === 'UP';

  return (
    <Section title={t.iaTitle ?? 'Inteligencia Artificial'} subtitle={t.iaSubtitle ?? 'Predicciones y recomendaciones basadas en lecturas de sensores'}>
      <div className="metrics">
        <Metric icon={<FlaskConical />} label={t.iaStatus ?? 'Estado IA'} value={iaOnline ? 'Online' : 'Offline'} tone={iaOnline ? 'normal' : 'warning'} />
        <Metric icon={<TrendingUp />} label={t.predictedTemp ?? 'Temp. pronosticada'} value={prediction?.predictedTemperature != null ? `${prediction.predictedTemperature}°C` : '--'} />
        <Metric icon={<Droplets />} label={t.predictedHum ?? 'Humedad pronosticada'} value={prediction?.predictedHumidity != null ? `${prediction.predictedHumidity}%` : '--'} />
        <Metric icon={<Shield />} label={t.riskLevel ?? 'Nivel de riesgo'} value={prediction?.riskLevel ?? '--'} tone={prediction?.riskLevel === 'HIGH' ? 'warning' : 'normal'} />
      </div>

      {iaOnline && prediction && (
        <div className="dashboardGrid">
          <Panel title={t.predictionDetails ?? 'Detalle de prediccion'}>
            <div className="iaPredictionGrid">
              <div className="iaCard" style={{ borderColor: riskColor.text }}>
                <Thermometer size={24} style={{ color: riskColor.text }} />
                <span className="iaLabel">{t.temperature}</span>
                <strong className="iaValue">{prediction.predictedTemperature ?? '--'}°C</strong>
                <span className="iaBadge" style={{ background: riskColor.bg, color: riskColor.text }}>
                  {prediction.anomalies?.temperature ? '⚠ Anomalia' : '✓ Normal'}
                </span>
              </div>
              <div className="iaCard">
                <Droplets size={24} style={{ color: '#0ea5e9' }} />
                <span className="iaLabel">{t.humidity}</span>
                <strong className="iaValue">{prediction.predictedHumidity ?? '--'}%</strong>
                <span className="iaBadge" style={{
                  background: prediction.anomalies?.humidity ? 'rgba(255,91,91,0.12)' : 'rgba(0,204,122,0.12)',
                  color: prediction.anomalies?.humidity ? '#ff5b5b' : '#00cc7a'
                }}>
                  {prediction.anomalies?.humidity ? '⚠ Anomalia' : '✓ Normal'}
                </span>
              </div>
              <div className="iaCard" style={{ borderColor: riskColor.text, background: riskColor.bg }}>
                <AlertTriangle size={24} style={{ color: riskColor.text }} />
                <span className="iaLabel">{t.riskLevel ?? 'Riesgo'}</span>
                <strong className="iaValue" style={{ color: riskColor.text }}>{prediction.riskLevel}</strong>
              </div>
            </div>
          </Panel>

          {recommendation && (
            <Panel title={t.recommendation ?? 'Recomendacion'}>
              <div className="iaRecommendation">
                <Cpu size={32} style={{ color: riskColor.text }} />
                <strong>{recommendation.action?.replace(/_/g, ' ')}</strong>
                <p>{recommendation.reason}</p>
              </div>
            </Panel>
          )}
        </div>
      )}

      {!iaOnline && (
        <Panel title={t.iaStatus ?? 'Estado IA'}>
          <p className="emptyState">{t.iaOffline ?? 'El servicio de IA no esta disponible. Inicia el microservicio Flask en el puerto 5000.'}</p>
        </Panel>
      )}
    </Section>
  );
}
