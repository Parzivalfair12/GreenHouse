import { useEffect, useState } from 'react';
import { Activity, ShieldCheck, ShieldAlert, ShieldOff, Loader, RefreshCw } from 'lucide-react';
import { Section, Panel } from './shared.jsx';
import { fetchLogs } from '../api.js';

const ORIGIN_CLASSES = {
  MANUAL: 'logOrigin-manual',
  AUTOMATIC: 'logOrigin-automatic',
  RULE: 'logOrigin-automatic',
  SIMULATION: 'logOrigin-automatic',
};

export function LogsSection({ t }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  async function loadLogs() {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchLogs();
      setLogs(data || []);
    } catch (err) {
      setError(err.message || t.loadError);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadLogs();
  }, []);

  return (
    <Section title={t.auditLog} subtitle={t.auditSubtitle}>
      <div className="toolbar" style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 12 }}>
        <button type="button" className="btnSecondary" onClick={loadLogs} disabled={loading}>
          <RefreshCw size={16} className={loading ? 'spin' : ''} /> Actualizar
        </button>
      </div>

      {loading && logs.length === 0 && (
        <div className="loadingOverlay">
          <Loader className="spin" size={32} />
          <p>{t.loading}</p>
        </div>
      )}

      {error && (
        <Panel title={t.error}>
          <p className="errorText">{error}</p>
          <button className="btnPrimary" onClick={loadLogs}>{t.retry}</button>
        </Panel>
      )}

      {!loading && logs.length === 0 && <p className="emptyState">{t.noRecords}</p>}

      {logs.length > 0 && (
        <div className="tableWrap">
          <table className="styledTable" aria-label="Registro de auditoria">
            <thead>
              <tr>
                <th>{t.date}</th>
                <th>{t.action}</th>
                <th>{t.origin}</th>
                <th>{t.detail}</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td>{new Date(log.createdAt).toLocaleString()}</td>
                  <td>
                    <span className="actionBadge">{log.action}</span>
                  </td>
                  <td>
                    <span className={`logOrigin ${ORIGIN_CLASSES[log.origin] || ''}`}>{log.origin || 'MANUAL'}</span>
                  </td>
                  <td>{log.detail}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Section>
  );
}
