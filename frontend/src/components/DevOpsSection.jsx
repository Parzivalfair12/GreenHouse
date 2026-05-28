import { useEffect, useState } from 'react';
import { Activity, CheckCircle, XCircle, Clock, Cpu, BarChart3, Loader, ListChecks, RefreshCw, GitBranch, ExternalLink } from 'lucide-react';
import { Metric, Panel, Section } from './shared.jsx';
import { fetchDevOpsSummary, fetchDevOpsWorkflows, syncDevOpsWorkflows } from '../api.js';

const STATUS_COLORS = {
  success: { bg: 'rgba(0, 204, 122, 0.12)', text: '#00cc7a' },
  failure: { bg: 'rgba(239, 68, 68, 0.12)', text: '#ef4444' },
  cancelled: { bg: 'rgba(245, 158, 11, 0.12)', text: '#f59e0b' },
  running: { bg: 'rgba(14, 165, 233, 0.12)', text: '#0ea5e9' },
  queued: { bg: 'rgba(156, 163, 175, 0.12)', text: '#9ca3af' },
  in_progress: { bg: 'rgba(14, 165, 233, 0.12)', text: '#0ea5e9' },
  completed: { bg: 'rgba(0, 204, 122, 0.12)', text: '#00cc7a' },
  default: { bg: 'rgba(156, 163, 175, 0.12)', text: '#9ca3af' }
};

function getStatusColor(status) {
  return STATUS_COLORS[(status || '').toLowerCase()] || STATUS_COLORS.default;
}

export function DevOpsSection({ t }) {
  const [summary, setSummary] = useState(null);
  const [workflows, setWorkflows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [syncing, setSyncing] = useState(false);
  const [syncResult, setSyncResult] = useState(null);

  const loadData = () => {
    setLoading(true);
    setError(null);
    Promise.all([fetchDevOpsSummary(), fetchDevOpsWorkflows()])
      .then(([summaryData, workflowsData]) => {
        setSummary(summaryData);
        setWorkflows(workflowsData || []);
      })
      .catch((err) => {
        setError(err.message || t.loadError);
        setSummary(null);
        setWorkflows([]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [t]);

  const handleSync = async () => {
    setSyncing(true);
    setSyncResult(null);
    try {
      const result = await syncDevOpsWorkflows();
      setSyncResult(result);
      loadData();
    } catch (err) {
      setSyncResult({ message: err.message });
    } finally {
      setSyncing(false);
    }
  };

  if (loading) {
    return (
      <Section title={t.devopsTitle ?? 'Panel DevOps'} subtitle={t.devopsSubtitle ?? 'Pipeline CI/CD, builds, cobertura y auditoria'}>
        <div className="loadingOverlay minimal"><Loader className="spin" size={24} /><p>{t.loading}</p></div>
      </Section>
    );
  }

  if (error) {
    return (
      <Section title={t.devopsTitle ?? 'Panel DevOps'} subtitle={t.devopsSubtitle ?? 'Pipeline CI/CD, builds, cobertura y auditoria'}>
        <Panel title={t.error ?? 'Error'}>
          <p className="errorText">{error}</p>
        </Panel>
      </Section>
    );
  }

  const github = summary?.github || {};
  const githubEnabled = github.githubEnabled || false;

  return (
    <Section title={t.devopsTitle ?? 'Panel DevOps'} subtitle={t.devopsSubtitle ?? 'Pipeline CI/CD, builds, cobertura y auditoria'}>
      {/* Métricas GitHub Actions */}
      {summary && (
        <div className="metrics">
          <Metric icon={<ListChecks />} label={t.totalPipelines ?? 'Total workflows'} value={github.total ?? 0} />
          <Metric icon={<CheckCircle />} label={t.passedPipelines ?? 'Exitosos'} value={github.success ?? 0} tone="power" />
          <Metric icon={<XCircle />} label={t.failedPipelines ?? 'Fallidos'} value={github.failure ?? 0} tone="danger" />
          <Metric icon={<BarChart3 />} label={t.passRate ?? 'Tasa de exito'} value={`${github.passRate ?? 0}%`} />
          {githubEnabled
            ? <Metric icon={<Cpu />} label="GitHub API" value="CONECTADO" tone="power" />
            : <Metric icon={<Cpu />} label="GitHub API" value="NO CONFIGURADO" tone="danger" />
          }
        </div>
      )}

      {/* Botón sync */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem' }}>
        <button className="btn btn-secondary" onClick={handleSync} disabled={syncing} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <RefreshCw size={16} className={syncing ? 'spin' : ''} />
          {syncing ? (t.syncing ?? 'Sincronizando...') : (t.taigaSync ?? 'Sincronizar GitHub')}
        </button>
      </div>
      {syncResult && (
        <div className="alert" style={{ marginBottom: '1rem', padding: '0.75rem', borderRadius: '8px', background: 'rgba(14,165,233,0.1)', color: '#0ea5e9' }}>
          {syncResult.message}
        </div>
      )}

      {/* Workflows reales de GitHub */}
      <Panel title="Workflows de GitHub Actions (datos reales)">
        {workflows.length === 0 && (
          <p className="emptyState">
            {githubEnabled
              ? 'No hay workflows sincronizados. Haz clic en Sincronizar para obtener datos de GitHub Actions.'
              : 'GitHub Actions no configurado. Establece GITHUB_TOKEN, GITHUB_OWNER y GITHUB_REPO en variables de entorno.'}
          </p>
        )}
        {workflows.map((w) => {
          const colors = getStatusColor(w.conclusion || w.status);
          const statusLabel = w.conclusion || w.status || 'unknown';
          return (
            <article key={w.id} className="storyCard" style={{ borderLeftColor: colors.text }}>
              <div className="storyHeader">
                <span className="storyEpic" style={{ color: colors.text }}>{w.workflowName}</span>
                <span className="storyTitle">{w.commitMessage || w.branch}</span>
                <span className="storyStatus" style={{ background: colors.bg, color: colors.text }}>
                  {statusLabel === 'success' ? <CheckCircle size={14} /> : statusLabel === 'failure' ? <XCircle size={14} /> : <Clock size={14} />}
                  {statusLabel}
                </span>
              </div>
              <div className="storyMeta" style={{ display: 'flex', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem', flexWrap: 'wrap' }}>
                <span><GitBranch size={12} style={{ display: 'inline', verticalAlign: 'middle', marginRight: '0.25rem' }} />{w.branch}</span>
                <span>Commit: {w.commitSha ? w.commitSha.substring(0, 7) : '-'}</span>
                <span>Actor: {w.actor}</span>
                {w.durationMs > 0 && <span>Duracion: {(w.durationMs / 1000).toFixed(1)}s</span>}
                {w.event && <span>Evento: {w.event}</span>}
                {w.runUrl && (
                  <a href={w.runUrl} target="_blank" rel="noopener noreferrer" style={{ color: 'var(--accent)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                    <ExternalLink size={12} /> Ver en GitHub
                  </a>
                )}
              </div>
              {w.startedAt && (
                <p className="storyDesc" style={{ marginTop: '0.25rem', fontSize: '0.8rem' }}>
                  {new Date(w.startedAt).toLocaleString()}
                  {w.completedAt && ` → ${new Date(w.completedAt).toLocaleString()}`}
                </p>
              )}
            </article>
          );
        })}
      </Panel>
    </Section>
  );
}
