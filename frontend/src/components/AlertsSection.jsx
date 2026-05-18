import { Section } from './shared.jsx';

export function AlertsSection({ alerts, onResolve, t }) {
  return (
    <Section title={t.alerts}>
      <div className="explainBand">
        <strong>{t.alertsPurposeTitle}</strong>
        <p>{t.alertsPurposeText}</p>
      </div>
      <div className="alertsGrid">
        {alerts.length === 0 && <p className="emptyState">{t.noAlerts}</p>}
        {alerts.map((alert) => (
          <article className="alertCard" key={alert.id}>
            <span>{alert.severity}</span>
            <strong>{alert.message}</strong>
            <small>{t.sensorCode}: {alert.sensorCode}</small>
            <p>{t.alertActionHint}</p>
            <button type="button" onClick={() => onResolve(alert.id)}>{t.resolve}</button>
          </article>
        ))}
      </div>
    </Section>
  );
}
