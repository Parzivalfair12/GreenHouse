import { AlertTriangle, Bell, ClipboardList, Cpu, Database, Gauge, Leaf, ListChecks, Map, Radio, Users } from 'lucide-react';
import { Panel, Section } from './shared.jsx';

const manualIcons = {
  dashboard: Gauge,
  greenhouses: Leaf,
  zones: Map,
  sensors: Radio,
  readings: ClipboardList,
  actuators: Cpu,
  rules: ListChecks,
  operations: Gauge,
  alerts: Bell,
  logs: Database,
  users: Users
};

/* Manual de usuario interactivo del sistema.
 * Flujo de datos: todo el contenido (módulos, pasos, mapa de relaciones) viene de las traducciones i18n (t).
 *   No hay datos de backend; es completamente estático.
 * Iconos: mapeo manualIcons asocia cada module.id a un icono de lucide-react.
 *   Si no hay mapeo, usa ClipboardList como fallback.
 * Estructura: hero (propósito), grid de módulos (cada uno con título, descripción y relación),
 *   split con flujo de operación (ordenado) y mapa relacional del sistema. */
export function ManualSection({ t }) {
  return (
    <Section title={t.manualTitle} subtitle={t.manualSubtitle}>
      {/* Hero: label + título + texto de propósito */}
      <div className="manualHero">
        <div>
          <span className="statusDot"><AlertTriangle size={16} />{t.manualPurposeLabel}</span>
          <h3>{t.manualPurposeTitle}</h3>
          <p>{t.manualPurposeText}</p>
        </div>
      </div>

      {/* Grid de módulos del sistema, cada uno con icono, título, descripción y relación */}
      <div className="manualGrid">
        {t.manualModules.map((module) => {
          const Icon = manualIcons[module.id] ?? ClipboardList;
          return (
            <article className="manualCard" key={module.id}>
              <span><Icon size={20} /></span>
              <div>
                <h3>{module.title}</h3>
                <p>{module.description}</p>
                <strong>{t.manualRelation}</strong>
                <small>{module.relation}</small>
              </div>
            </article>
          );
        })}
      </div>

      {/* Panel dividido: flujo de operación y mapa de relaciones del sistema */}
      <div className="manualSplit">
        <Panel title={t.manualFlowTitle}>
          <ol className="manualSteps">
            {t.manualFlow.map((step) => <li key={step}>{step}</li>)}
          </ol>
        </Panel>
        <Panel title={t.manualMapTitle}>
          <div className="relationMap">
            {t.manualMap.map((item, index) => (
              <span key={item}>
                {item}
                {/* Flecha entre elementos: no se muestra después del último */}
                {index < t.manualMap.length - 1 && <strong>→</strong>}
              </span>
            ))}
          </div>
        </Panel>
      </div>
    </Section>
  );
}
