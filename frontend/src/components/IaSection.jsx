import { useEffect, useState } from 'react';
import { Thermometer, Droplets, Shield, TrendingUp, Loader, AlertTriangle } from 'lucide-react';
import { Section, Panel } from './shared.jsx';
import { fetchAiPrediction } from '../api.js';

const RISK_CLASSES = {
  LOW: 'risk-badge-low',
  MEDIUM: 'risk-badge-medium',
  HIGH: 'risk-badge-high'
};

export function IaPreview() {
  const [risk, setRisk] = useState(null);

  useEffect(() => {
    fetchAiPrediction()
      .then((data) => setRisk(data.riskLevel))
      .catch(() => setRisk('Offline'));
  }, []);

  const color = risk === 'HIGH' ? '#ff5b5b' : risk === 'MEDIUM' ? '#d9a400' : risk === 'LOW' ? '#00cc7a' : '#9ca3af';
  return <span style={{ color, fontWeight: 700 }}>{risk ?? '...'}</span>;
}

export function IaSection({ t }) {
  const [prediction, setPrediction] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  async function loadPrediction() {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchAiPrediction();
      setPrediction(data);
    } catch (err) {
      setError(err.message || t.loadError);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadPrediction();
    const id = setInterval(loadPrediction, 10000);
    return () => clearInterval(id);
  }, []);

  const trendArrow = prediction?.trend === 'UP' ? '\u2191' : prediction?.trend === 'DOWN' ? '\u2193' : '\u2192';
  const riskClass = RISK_CLASSES[prediction?.riskLevel] || '';

  return (
    <Section title={t.iaTitle} subtitle={t.iaSubtitle}>
      {loading && !prediction && (
        <div className="loadingOverlay minimal">
          <Loader className="spin" size={24} />
          <p>{t.analyzing}</p>
        </div>
      )}

      {error && (
        <Panel title={t.error}>
          <p className="errorText">{error}</p>
          <button className="btnPrimary" onClick={loadPrediction}>{t.retry}</button>
        </Panel>
      )}

      {prediction && (
        <div className="dashboardGrid">
          <Panel title={t.predictionDetails}>
            <div className="iaPredictionGrid">
              <div className="iaCard">
                <Thermometer size={24} style={{ color: 'var(--accent)' }} />
                <span className="iaLabel">{t.predictedTemp}</span>
                <strong className="iaValue">
                  {prediction.predictedTemperature != null ? `${prediction.predictedTemperature}°C` : '--'}
                </strong>
              </div>
              <div className="iaCard">
                <Droplets size={24} style={{ color: 'var(--cyan)' }} />
                <span className="iaLabel">{t.predictedHum}</span>
                <strong className="iaValue">
                  {prediction.predictedHumidity != null ? `${prediction.predictedHumidity}%` : '--'}
                </strong>
              </div>
              <div className="iaCard">
                <Shield size={24} style={{ color: 'var(--accent)' }} />
                <span className="iaLabel">{t.riskLevel}</span>
                <span className={`iaBadge ${riskClass}`}>{prediction.riskLevel || '--'}</span>
              </div>
              <div className="iaCard">
                <TrendingUp size={24} style={{ color: 'var(--accent)' }} />
                <span className="iaLabel">{t.trend}</span>
                <strong className="iaValue">{trendArrow}</strong>
              </div>
            </div>
          </Panel>

          <Panel title={t.recommendation}>
            <div className="iaRecommendation">
              <p>{prediction.recommendation || t.noRecords}</p>
            </div>
          </Panel>
        </div>
      )}
    </Section>
  );
}
