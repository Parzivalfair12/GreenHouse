import { useEffect, useState } from 'react';
import { BookOpen, CheckCircle, Circle, ListChecks, Target, Loader, GitBranch, RefreshCw, Activity } from 'lucide-react';
import { Metric, Panel, Section } from './shared.jsx';
import { fetchTaigaStories, fetchTaigaSummary, fetchTaigaTraceability, fetchTaigaCommits, fetchGeneratedStories, syncTaigaStories } from '../api.js';

function resolveNested(obj, path, fallback) {
  if (obj == null) return fallback;
  const parts = path.split('.');
  let current = obj;
  for (const part of parts) {
    if (current == null || typeof current !== 'object') return fallback;
    current = current[part];
  }
  return current != null ? current : fallback;
}

function getStatusLabel(story) {
  const status = story.status;
  if (status == null) return 'N/A';
  if (typeof status === 'object' && status !== null) return status.name || status.id || 'N/A';
  return String(status);
}

function getPriorityLabel(story) {
  const priority = story.priority;
  if (priority == null) return null;
  if (typeof priority === 'object' && priority !== null) return priority.name || priority.id || null;
  return String(priority);
}

function getAssignedTo(story) {
  const assigned = story.assigned_to;
  if (assigned == null) return null;
  if (typeof assigned === 'object' && assigned !== null) return assigned.full_name || assigned.username || assigned.id || null;
  return String(assigned);
}

const STATUS_COLORS = {
  completed: { bg: 'rgba(0, 204, 122, 0.12)', text: '#00cc7a' },
  done: { bg: 'rgba(0, 204, 122, 0.12)', text: '#00cc7a' },
  closed: { bg: 'rgba(0, 204, 122, 0.12)', text: '#00cc7a' },
  in_progress: { bg: 'rgba(14, 165, 233, 0.12)', text: '#0ea5e9' },
  ready_for_test: { bg: 'rgba(168, 85, 247, 0.12)', text: '#a855f7' },
  new: { bg: 'rgba(156, 163, 175, 0.12)', text: '#9ca3af' },
  default: { bg: 'rgba(156, 163, 175, 0.12)', text: '#9ca3af' }
};

function getStatusColor(label) {
  const key = String(label).toLowerCase().replace(/\s+/g, '_');
  return STATUS_COLORS[key] || STATUS_COLORS.default;
}

export function TaigaSection({ t }) {
  const [tab, setTab] = useState('stories');
  const [stories, setStories] = useState([]);
  const [summary, setSummary] = useState(null);
  const [traceability, setTraceability] = useState([]);
  const [commits, setCommits] = useState([]);
  const [generated, setGenerated] = useState([]);
  const [expandedId, setExpandedId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [syncing, setSyncing] = useState(false);
  const [syncResult, setSyncResult] = useState(null);

  const loadData = (activeTab) => {
    setLoading(true);
    setError(null);
    const basePromises = [fetchTaigaStories(), fetchTaigaSummary()];
    const extraPromises = [];
    if (activeTab === 'traceability') extraPromises.push(fetchTaigaTraceability());
    if (activeTab === 'commits') extraPromises.push(fetchTaigaCommits());
    if (activeTab === 'generated') extraPromises.push(fetchGeneratedStories());

    Promise.all([...basePromises, ...extraPromises])
      .then(([storiesData, summaryData, ...rest]) => {
        setStories(storiesData);
        setSummary(summaryData);
        if (rest.length > 0) {
          if (activeTab === 'traceability') setTraceability(rest[0] || []);
          else if (activeTab === 'commits') setCommits(rest[0] || []);
          else if (activeTab === 'generated') setGenerated(rest[0] || []);
        }
      })
      .catch((err) => {
        setError(err.message || t.loadError);
        setStories([]); setSummary(null);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(tab); }, [tab, t]);

  const handleSync = async () => {
    setSyncing(true); setSyncResult(null);
    try {
      const result = await syncTaigaStories();
      setSyncResult(result);
      loadData(tab);
    } catch (err) {
      setSyncResult({ success: false, message: err.message });
    } finally { setSyncing(false); }
  };

  const tabs = [
    { id: 'stories', label: t.backlog ?? 'Backlog', icon: ListChecks },
    { id: 'generated', label: t.taigaGenerated ?? 'Generadas', icon: Activity },
    { id: 'traceability', label: t.taigaTraceability ?? 'Trazabilidad', icon: GitBranch },
    { id: 'commits', label: t.taigaCommits ?? 'Commits', icon: BookOpen }
  ];

  if (loading) {
    return (
      <Section title={t.taigaTitle ?? 'Historias de Usuario'} subtitle={t.taigaSubtitle ?? 'Backlog del proyecto'}>
        <div className="loadingOverlay minimal"><Loader className="spin" size={24} /><p>{t.loading}</p></div>
      </Section>
    );
  }

  if (error) {
    return (
      <Section title={t.taigaTitle ?? 'Historias de Usuario'} subtitle={t.taigaSubtitle ?? 'Backlog del proyecto'}>
        <Panel title={t.error ?? 'Error'}>
          <p className="errorText">{error}</p>
          <p className="emptyState">{t.taigaUnavailable ?? 'Integracion con Taiga pendiente o servicio no disponible.'}</p>
        </Panel>
      </Section>
    );
  }

  return (
    <Section title={t.taigaTitle ?? 'Historias de Usuario'} subtitle={t.taigaSubtitle ?? 'Backlog del proyecto con criterios de aceptacion'}>
      {summary && (
        <div className="metrics">
          <Metric icon={<ListChecks />} label={t.totalStories ?? 'Total historias'} value={summary.totalStories ?? 0} />
          <Metric icon={<CheckCircle />} label={t.completedStories ?? 'Completadas'} value={summary.completedStories ?? 0} tone="power" />
          <Metric icon={<Target />} label={t.completion ?? 'Completitud'} value={`${summary.completionPercent ?? 0}%`} />
          {summary.taigaEnabled === false && <Metric icon={<Circle />} label="Taiga" value="No configurado" tone="danger" />}
          {summary.taigaEnabled === true && <Metric icon={<CheckCircle />} label="Taiga" value="Conectado" tone="power" />}
        </div>
      )}

      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem' }}>
        <button className="btn btn-secondary" onClick={handleSync} disabled={syncing} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <RefreshCw size={16} className={syncing ? 'spin' : ''} />
          {syncing ? (t.syncing ?? 'Sincronizando...') : (t.taigaSync ?? 'Sincronizar')}
        </button>
      </div>
      {syncResult && (
        <div className={`alert ${syncResult.success ? 'alert-success' : 'alert-error'}`} style={{ marginBottom: '1rem', padding: '0.75rem', borderRadius: '8px', background: syncResult.success ? 'rgba(0,204,122,0.1)' : 'rgba(239,68,68,0.1)', color: syncResult.success ? '#00cc7a' : '#ef4444' }}>
          {syncResult.message}
        </div>
      )}

      <div className="tabs" style={{ display: 'flex', gap: '0.25rem', marginBottom: '1rem', borderBottom: '1px solid var(--border)' }}>
        {tabs.map((tabItem) => (
          <button
            key={tabItem.id}
            className={`tab ${tab === tabItem.id ? 'active' : ''}`}
            onClick={() => setTab(tabItem.id)}
            style={{
              padding: '0.5rem 1rem', cursor: 'pointer', border: 'none',
              background: tab === tabItem.id ? 'var(--bg-card)' : 'transparent',
              color: tab === tabItem.id ? 'var(--text)' : 'var(--text-secondary)',
              borderBottom: tab === tabItem.id ? '2px solid var(--accent)' : '2px solid transparent',
              display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.9rem'
            }}
          >
            <tabItem.icon size={16} /> {tabItem.label}
          </button>
        ))}
      </div>

      {tab === 'stories' && (
        <Panel title={t.backlog ?? 'Backlog (datos reales de Taiga)'}>
          <div className="storiesList">
            {stories.length === 0 && <p className="emptyState">{t.noStories ?? 'No hay historias disponibles en Taiga'}</p>}
            {stories.map((story) => {
              const statusLabel = getStatusLabel(story);
              const colors = getStatusColor(statusLabel);
              return (
                <article key={story.id ?? story.ref} className="storyCard" style={{ borderLeftColor: colors.text }}>
                  <div className="storyHeader" onClick={() => setExpandedId(expandedId === story.id ? null : story.id)}>
                    <span className="storyEpic" style={{ color: colors.text }}>
                      {resolveNested(story, 'project_extra_info.name', 'General')}
                    </span>
                    <strong className="storyTitle">{story.subject}</strong>
                    <span className="storyStatus" style={{ background: colors.bg, color: colors.text }}>
                      {(statusLabel === 'done' || statusLabel === 'closed' || statusLabel === 'completed')
                        ? <CheckCircle size={14} /> : <Circle size={14} />}
                      {story.ref ? `#${story.ref} ` : ''}{statusLabel}
                    </span>
                  </div>
                  <p className="storyDesc">{story.description || ''}</p>
                  <div className="storyMeta" style={{ display: 'flex', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem', flexWrap: 'wrap' }}>
                    {getPriorityLabel(story) && <span>Prioridad: {getPriorityLabel(story)}</span>}
                    {getAssignedTo(story) && <span>Asignado: {getAssignedTo(story)}</span>}
                    {story.created_date && <span>Creado: {new Date(story.created_date).toLocaleDateString()}</span>}
                    {story.milestone && <span>Hito: {typeof story.milestone === 'object' ? story.milestone.name || story.milestone.id : story.milestone}</span>}
                    {story.version != null && <span>v{story.version}</span>}
                  </div>
                </article>
              );
            })}
          </div>
        </Panel>
      )}

      {tab === 'generated' && (
        <Panel title={t.taigaGenerated ?? 'Historias generadas desde codigo'}>
          <div className="storiesList">
            {generated.length === 0 && <p className="emptyState">{t.noStories ?? 'No hay historias generadas'}</p>}
            {generated.map((story) => {
              const colors = getStatusColor(story.status);
              return (
                <article key={story.subject} className="storyCard" style={{ borderLeftColor: colors.text }}>
                  <div className="storyHeader" onClick={() => setExpandedId(expandedId === story.subject ? null : story.subject)}>
                    <span className="storyEpic" style={{ color: colors.text }}>{story.epic}</span>
                    <strong className="storyTitle">{story.subject}</strong>
                    <span className="storyStatus" style={{ background: colors.bg, color: colors.text }}>
                      {story.status === 'COMPLETED' ? <CheckCircle size={14} /> : <Circle size={14} />}
                      {story.status}
                    </span>
                  </div>
                  <p className="storyDesc">{story.description}</p>
                  <div className="storyMeta" style={{ display: 'flex', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                    {story.priority && <span>Prioridad: {story.priority}</span>}
                    {Array.isArray(story.modules) && <span>Endpoints: {story.modules.length}</span>}
                  </div>
                  {expandedId === story.subject && Array.isArray(story.criteria) && story.criteria.length > 0 && (
                    <div className="storyCriteria">
                      <strong>{t.acceptanceCriteria ?? 'Criterios de aceptacion'}:</strong>
                      {story.criteria.map((c) => (
                        <span key={c.id ?? c.description} className={`criterion ${c.status === 'PASSED' ? 'passed' : ''}`}>
                          {c.status === 'PASSED' ? <CheckCircle size={14} /> : <Circle size={14} />}
                          {c.description}
                        </span>
                      ))}
                    </div>
                  )}
                </article>
              );
            })}
          </div>
        </Panel>
      )}

      {tab === 'traceability' && (
        <Panel title={t.taigaTraceability ?? 'Matriz de trazabilidad (commits ↔ historias)'}>
          <div className="storiesList">
            {traceability.length === 0 && <p className="emptyState">{t.commitsEmpty ?? 'No se encontraron registros'}</p>}
            {traceability.map((item, idx) => (
              <article key={idx} className="storyCard" style={{ borderLeftColor: '#0ea5e9' }}>
                <div className="storyHeader">
                  <span className="storyEpic">{item.userStories ?? 'N/A'}</span>
                  <span className="storyTitle">{item.message}</span>
                  <span className="storyStatus" style={{ background: 'rgba(14, 165, 233, 0.12)', color: '#0ea5e9' }}>
                    <GitBranch size={14} /> {item.type}
                  </span>
                </div>
                <div className="storyMeta" style={{ display: 'flex', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                  <span>Commit: {item.commit ? item.commit.substring(0, 7) : '-'}</span>
                  <span>Autor: {item.author ?? '-'}</span>
                  <span>Fecha: {item.date ?? '-'}</span>
                </div>
              </article>
            ))}
          </div>
        </Panel>
      )}

      {tab === 'commits' && (
        <Panel title={t.taigaCommits ?? 'Historial de commits'}>
          <div className="storiesList">
            {commits.length === 0 && <p className="emptyState">{t.commitsEmpty ?? 'No se encontraron commits'}</p>}
            {commits.map((commit, idx) => {
              const stories = commit.userStories;
              const hasStories = Array.isArray(stories) && stories.length > 0;
              return (
                <article key={idx} className="storyCard" style={{ borderLeftColor: hasStories ? '#8b5cf6' : '#9ca3af' }}>
                  <div className="storyHeader">
                    <span className="storyEpic" style={{ color: hasStories ? '#8b5cf6' : '#9ca3af' }}>
                      {commit.type ?? '-'}{commit.scope ? `(${commit.scope})` : ''}
                    </span>
                    <strong className="storyTitle">{commit.message}</strong>
                    <span className="storyStatus" style={{ background: hasStories ? 'rgba(139, 92, 246, 0.12)' : 'rgba(156, 163, 175, 0.12)', color: hasStories ? '#8b5cf6' : '#9ca3af' }}>
                      <BookOpen size={14} /> {commit.hash ? commit.hash.substring(0, 7) : '-'}
                    </span>
                  </div>
                  <div className="storyMeta" style={{ display: 'flex', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem', flexWrap: 'wrap' }}>
                    <span>Autor: {commit.author ?? '-'}</span>
                    <span>Fecha: {commit.date ?? '-'}</span>
                    {hasStories && <span>Historias: {stories.join(', ')}</span>}
                    {!hasStories && <span style={{ color: '#ef4444' }}>Sin referencia US#</span>}
                    {commit.refs && <span>Ramas: {commit.refs}</span>}
                  </div>
                </article>
              );
            })}
          </div>
        </Panel>
      )}
    </Section>
  );
}
