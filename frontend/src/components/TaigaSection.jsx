import { useEffect, useState } from 'react';
import { BookOpen, CheckCircle, Circle, ListChecks, Target, Loader } from 'lucide-react';
import { Metric, Panel, Section } from './shared.jsx';
import { fetchTaigaStories, fetchTaigaSummary } from '../api.js';

const STATUS_COLORS = {
  COMPLETED: { bg: 'rgba(0, 204, 122, 0.12)', text: '#00cc7a' },
  IN_PROGRESS: { bg: 'rgba(14, 165, 233, 0.12)', text: '#0ea5e9' },
  PENDING: { bg: 'rgba(156, 163, 175, 0.12)', text: '#9ca3af' }
};

/* Integración con API externa de Taiga para mostrar el backlog del proyecto.
 * Flujo de datos: al montar el componente, dispara dos fetch paralelos (fetchTaigaStories, fetchTaigaSummary).
 * Manejo de estado: loading (spinner), error (mensaje + fallback), y datos (stories + summary).
 * Sincronización con backend: las llamadas se ejecutan en Promise.all; cualquier error se captura y muestra.
 * i18n: usa valores por defecto (??) como fallback si las traducciones no están definidas.
 * Cada historia es expandible (expandedId) para mostrar sus criterios de aceptación con estado PASSED/pendiente. */
export function TaigaSection({ t }) {
  const [stories, setStories] = useState([]);
  const [summary, setSummary] = useState(null);
  const [expandedId, setExpandedId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /* Efecto de carga inicial: resetea estados y consulta ambas APIs simultáneamente */
  useEffect(() => {
    setLoading(true);
    setError(null);
    Promise.all([fetchTaigaStories(), fetchTaigaSummary()])
      .then(([storiesData, summaryData]) => {
        setStories(storiesData);
        setSummary(summaryData);
      })
      .catch((err) => {
        setError(err.message || t.loadError);
        setStories([]);
        setSummary(null);
      })
      .finally(() => setLoading(false));
  }, [t]);

  /* Estado de carga: muestra spinner minimal */
  if (loading) {
    return (
      <Section title={t.taigaTitle ?? 'Historias de Usuario'} subtitle={t.taigaSubtitle ?? 'Backlog del proyecto'}>
        <div className="loadingOverlay minimal"><Loader className="spin" size={24} /><p>{t.loading}</p></div>
      </Section>
    );
  }

  /* Estado de error: muestra mensaje y sugerencia de disponibilidad */
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
      {/* Métricas de resumen: total, completadas y porcentaje */}
      {summary && (
        <div className="metrics">
          <Metric icon={<ListChecks />} label={t.totalStories ?? 'Total historias'} value={summary.totalStories ?? 0} />
          <Metric icon={<CheckCircle />} label={t.completedStories ?? 'Completadas'} value={summary.completedStories ?? 0} tone="power" />
          <Metric icon={<Target />} label={t.completion ?? 'Completitud'} value={`${summary.completionPercent ?? 0}%`} />
        </div>
      )}

      <Panel title={t.backlog ?? 'Backlog'}>
        <div className="storiesList">
          {stories.length === 0 && <p className="emptyState">{t.noStories ?? 'No hay historias disponibles'}</p>}
          {stories.map((story) => {
            /* Asigna color según estado (COMPLETED, IN_PROGRESS, PENDING) */
            const colors = STATUS_COLORS[story.status] ?? STATUS_COLORS.PENDING;
            return (
              <article key={story.id} className="storyCard" style={{ borderLeftColor: colors.text }}>
                {/* Encabezado clickeable: expande/colapsa criterios de aceptación */}
                <div className="storyHeader" onClick={() => setExpandedId(expandedId === story.id ? null : story.id)}>
                  <span className="storyEpic" style={{ color: colors.text }}>{story.epic}</span>
                  <strong className="storyTitle">{story.title}</strong>
                  <span className="storyStatus" style={{ background: colors.bg, color: colors.text }}>
                    {story.status === 'COMPLETED' ? <CheckCircle size={14} /> : <Circle size={14} />}
                    {story.status}
                  </span>
                </div>
                <p className="storyDesc">{story.description}</p>
                {/* Criterios de aceptación visibles solo cuando la historia está expandida */}
                {expandedId === story.id && Array.isArray(story.criteria) && story.criteria.length > 0 && (
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
    </Section>
  );
}
