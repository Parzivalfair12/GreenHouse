import { Section } from './shared.jsx';

/* Listado de alertas abiertas del sistema.
 * Flujo de datos: 'alerts' llega desde el backend filtrado por no resueltas.
 * Resolución: onResolve(alert.id) dispara una llamada PATCH al backend para marcar como resuelta.
 * Manejo de estados vacíos: si alerts.length === 0 muestra t.noAlerts.
 * i18n: textos de propósito, severidad, código de sensor y botón resolver. */
export function AlertsSection({ alerts, onResolve, t }) {
  return (
    <Section title={t.alerts}>
      {/* Banda explicativa del propósito de las alertas en el sistema */}
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
