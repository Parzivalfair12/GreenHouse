import { BookOpen } from 'lucide-react';
import { Section, Panel } from './shared.jsx';

export function AuditLogSection({ logs, t }) {
  const displayLogs = (logs || []).slice(0, 100);

  return (
    <Section title={t.auditLog} subtitle={t.records}>
      <Panel title={`${t.auditLog} (${displayLogs.length})`}>
        {displayLogs.length === 0 ? (
          <p className="emptyState">{t.noRecords}</p>
        ) : (
          <div className="auditTable">
            <div className="auditHeader">
              <span>{t.date}</span>
              <span>{t.action}</span>
              <span>{t.detail}</span>
              <span>{t.origin}</span>
            </div>
            {displayLogs.map((log, idx) => (
              <div className="auditRow" key={log.id ?? `${log.createdAt}-${log.action}-${idx}`}>
                <span className="auditTimestamp">{formatTimestamp(log.createdAt)}</span>
                <span className="auditAction">{log.action}</span>
                <span className="auditDetail">{log.detail || '-'}</span>
                <span className={log.origin === 'MANUAL' ? 'log-origin-manual' : 'log-origin-automatic'}>
                  {log.origin || 'AUTOMATIC'}
                </span>
              </div>
            ))}
          </div>
        )}
      </Panel>
    </Section>
  );
}

function formatTimestamp(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (isNaN(date.getTime())) return String(value);
  return date.toLocaleString('es-ES', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
}
