import { AlertTriangle, Bell, FlaskConical, Leaf, Pause, Play, Plus, Power, Radio, Sprout } from 'lucide-react';
import { Metric, Panel, Section } from './shared.jsx';
import { IaPreview } from './IaSection.jsx';

/* Componente principal del Dashboard.
 * Flujo de datos: recibe props agregadas desde el layout padre que consolida
 * respuestas de múltiples endpoints (totals, dashboard, readings, alerts, sensors, actuators).
 * i18n: todas las etiquetas llegan via prop 't' (objeto de traducciones).
 * Simulador: botón de inicio/parada vinculado a callbacks onStartSimulator/onStopSimulator. */
export function DashboardSection({ totals, selected, alerts, dashboard, readings, sensors, actuators, t, openAlerts, simulatorRunning, onStartSimulator, onStopSimulator }) {
  /* Cuenta actuadores habilitados para la métrica "actuatorsOn" */
  const activeActuators = actuators?.filter((a) => a.enabled).length ?? 0;

  return (
    <Section title={t.overview} subtitle={t.dashboardSubtitle}>
      {/* Panel de estado del simulador: muestra si está activo y botón de toggle */}
      <div className="simulatorPanel">
        <div className="simulatorStatus">
          <span className={`pulseDot ${simulatorRunning ? 'active' : ''}`} />
          <span>{simulatorRunning ? t.simulatorActive : t.simulatorInactive}</span>
        </div>
        <button
          type="button"
          className={`simulatorBtn ${simulatorRunning ? 'stop' : 'start'}`}
          onClick={simulatorRunning ? onStopSimulator : onStartSimulator}
        >
          {simulatorRunning ? <><Pause size={16} /> {t.stopSimulation}</> : <><Play size={16} /> {t.startSimulation}</>}
        </button>
      </div>
      {/* Métricas generales: invernaderos, cultivos, sensores, actuadores, alertas, IA */}
      <section className="metrics" aria-label="Metricas">
        <Metric icon={<Leaf />} label={t.greenhouses} value={dashboard?.greenhouses ?? totals.greenhouses} />
        <Metric icon={<Sprout />} label={t.crops} value={totals.crops} />
        <Metric icon={<Radio />} label={t.sensors} value={dashboard?.sensors ?? totals.sensors} />
        <Metric icon={<Power />} label={t.actuatorsOn} value={activeActuators} tone={activeActuators > 0 ? 'power' : undefined} />
        <Metric icon={<AlertTriangle />} label={t.pending} value={dashboard?.openAlerts ?? totals.alerts} tone="warning" />
        <Metric icon={<FlaskConical />} label={'IA'} value={<IaPreview readings={readings} sensors={sensors} />} />
      </section>
      <div className="dashboardGrid">
        <SensorChart dashboard={dashboard} selected={selected} readings={readings} sensors={sensors} t={t} />
        <AlertPreview alerts={alerts} t={t} onOpen={openAlerts} />
      </div>
      <LatestReadings readings={readings} sensors={sensors} t={t} />
      <OverviewPanel selected={selected} dashboard={dashboard} t={t} />
    </Section>
  );
}

/* Muestra las últimas 6 lecturas ordenadas por fecha descendente.
 * Lógica de negocio: clona el array para no mutar el original, ordena por recordedAt descendente,
 * toma 6 y resuelve el nombre del sensor via sensorMap. */
function LatestReadings({ readings, sensors, t }) {
  if (!readings || readings.length === 0) return null;
  const sensorMap = new Map(sensors.map((s) => [s.id, s]));
  const latest = readings.slice().sort((a, b) => new Date(b.recordedAt) - new Date(a.recordedAt)).slice(0, 6);
  return (
    <Panel title={t.latestReadings}>
      <div className="latestReadingsGrid">
        {latest.map((r) => {
          const sensor = sensorMap.get(r.sensorId);
          return (
            <div key={r.id} className="readingCard">
              <strong>{sensor?.code ?? r.sensorId}</strong>
              <span className="readingValue">{Number(r.value).toFixed(1)} {sensor?.unit ?? ''}</span>
              <span className="readingTime">{new Date(r.recordedAt).toLocaleTimeString()}</span>
            </div>
          );
        })}
      </div>
    </Panel>
  );
}

/* Panel de detalle del invernadero seleccionado.
 * Flujo de datos: 'selected' se establece desde el layout padre al hacer clic en un invernadero.
 * Si no hay selección, muestra estado vacío con noRecords. */
function OverviewPanel({ selected, dashboard, t }) {
  if (!selected) {
    return <Panel title={t.selected}><p className="emptyState">{t.noRecords}</p></Panel>;
  }
  return (
    <section className="operativeBlock">
      <div className="sectionHeader inlineHeader">
        <h2>{t.selected}</h2>
        <button className="linkButton" type="button">{t.viewAll}</button>
      </div>
      <div className="operativeGrid">
        <Panel title={selected.name}>
          <div className="overviewStats">
            <span className="pill active">{dashboard?.globalStatus ?? t.stable}</span>
            <span>{selected.location}</span>
            <strong>{selected.areaSquareMeters} m2</strong>
            <span>{selected.cropCount} {t.crops.toLowerCase()}</span>
            <span>{selected.sensorCount} {t.sensors.toLowerCase()}</span>
            <span>{t.lastReading}: {dashboard?.lastReading ? `${dashboard.lastReading.sensorCode} = ${dashboard.lastReading.value} ${dashboard.lastReading.unit}` : t.noRecords}</span>
          </div>
        </Panel>
        <article className="addGreenhouseCard">
          <span><Plus size={28} /></span>
          <strong>{t.addGreenhouse}</strong>
        </article>
      </div>
    </section>
  );
}

/* Vista previa de alertas: muestra conteo + primeras 2 alertas.
 * Botón "alerts" dispara onOpen para navegar a la sección de alertas completa.
 * Manejo de estado vacío: cuando no hay alertas muestra t.noAlerts. */
function AlertPreview({ alerts, t, onOpen }) {
  return (
    <Panel title={t.alerts}>
      {alerts.length === 0 ? <p className="emptyState">{t.noAlerts}</p> : (
        <div className="miniAlerts">
          <span className="countBubble">{alerts.length}</span>
          {alerts.slice(0, 2).map((alert) => <span className="alertPreviewItem" key={alert.id}><Bell size={16} />{alert.message}</span>)}
          <button type="button" onClick={onOpen}>{t.alerts}</button>
        </div>
      )}
    </Panel>
  );
}

/* Gráfico de lecturas de sensores (temperatura y humedad).
 * Flujo de datos: filtra readings por los sensores del invernadero seleccionado (o todos si no hay selección).
 * Ordena lecturas ascendentemente por fecha para la línea del gráfico.
 * Lógica de negocio: busca el último valor de temperatura/humedad via latestByType o latestByCode (fallback por patrón).
 * Genera series de datos para renderizar paths SVG con buildLinePath. */
function SensorChart({ dashboard, selected, readings = [], sensors = [], t }) {
  const selectedSensorIds = new Set(
    sensors
      .filter((sensor) => !selected || sensor.greenhouseId === selected.id)
      .map((sensor) => sensor.id)
  );
  const sensorById = new Map(sensors.map((sensor) => [sensor.id, sensor]));
  const scopedReadings = readings
    .filter((reading) => selectedSensorIds.size === 0 || selectedSensorIds.has(reading.sensorId))
    .slice()
    .sort((a, b) => new Date(a.recordedAt) - new Date(b.recordedAt));
  const temperature = latestByType(scopedReadings, sensorById, 'TEMPERATURE') ?? latestByCode(scopedReadings, /TEMP/i);
  const humidity = latestByType(scopedReadings, sensorById, 'HUMIDITY') ?? latestByType(scopedReadings, sensorById, 'SOIL_MOISTURE') ?? latestByCode(scopedReadings, /HUM|MOIST/i) ?? dashboard?.lastReading;
  const temperatureSeries = seriesByType(scopedReadings, sensorById, 'TEMPERATURE');
  const humiditySeries = seriesByType(scopedReadings, sensorById, 'HUMIDITY', 'SOIL_MOISTURE');
  const hasChartData = temperatureSeries.length > 1 || humiditySeries.length > 1;

  return (
    <Panel title={t.sensorReadings}>
      <div className="chartHeader">
        <strong>{formatValue(temperature?.value)} <small>{temperature?.unit ?? 'C'}</small></strong>
        <span>{statusLabel(temperature, sensorById)}</span>
        <strong>{formatValue(humidity?.value)} <small>{humidity?.unit ?? '%'}</small></strong>
        <span>{statusLabel(humidity, sensorById)}</span>
      </div>
      <div className="chartLegend">
        <span className="dot temp"></span>{t.temperature}
        <span className="dot humidity"></span>{t.humidity}
      </div>
      {hasChartData ? (
        <div className="fakeChart" aria-hidden="true">
          <svg viewBox="0 0 720 210" preserveAspectRatio="none">
            <defs>
              <linearGradient id="humidityFill" x1="0" x2="0" y1="0" y2="1">
                <stop offset="0%" stopColor="#06b6d4" stopOpacity="0.35" />
                <stop offset="100%" stopColor="#06b6d4" stopOpacity="0" />
              </linearGradient>
            </defs>
            <path className="gridLine" d="M0 42 H720 M0 84 H720 M0 126 H720 M0 168 H720" />
            {humiditySeries.length > 1 && <path className="humidityFill" d={`${buildLinePath(humiditySeries)} V210 H0 Z`} />}
            {humiditySeries.length > 1 && <path className="humidityLine" d={buildLinePath(humiditySeries)} />}
            {temperatureSeries.length > 1 && <path className="tempLine" d={buildLinePath(temperatureSeries)} />}
          </svg>
        </div>
      ) : (
        <div className="chartEmpty">{t.noRecords}</div>
      )}
      <p className="chartCaption">{t.last24Hours}{selected ? ` - ${selected.name}` : ''}</p>
    </Panel>
  );
}

/* Busca la última lectura (at(-1)) que coincida con uno de los tipos dados.
 * Asume que readings ya viene ordenado ascendentemente por fecha. */
function latestByType(readings, sensorById, ...types) {
  return readings
    .filter((reading) => types.includes(sensorById.get(reading.sensorId)?.type))
    .at(-1);
}

/* Fallback: busca la última lectura cuyo sensorCode coincida con un patrón regex.
 * Útil cuando el sensor no tiene type definido. */
function latestByCode(readings, pattern) {
  return readings.filter((reading) => pattern.test(reading.sensorCode ?? '')).at(-1);
}

/* Extrae los últimos 12 valores numéricos de un tipo de sensor para la serie del gráfico. */
function seriesByType(readings, sensorById, ...types) {
  return readings
    .filter((reading) => types.includes(sensorById.get(reading.sensorId)?.type))
    .slice(-12)
    .map((reading) => Number(reading.value));
}

function formatValue(value) {
  if (value === undefined || value === null) return '--';
  return Number(value).toFixed(1).replace('.0', '');
}

/* Etiqueta de estado basada en umbrales min/max del sensor.
 * Lógica de negocio: si el valor está fuera del rango → "Bajo" o "Alerta"; si no → "Normal". */
function statusLabel(reading, sensorById) {
  if (!reading) return 'Sin datos';
  const sensor = sensorById.get(reading.sensorId);
  const value = Number(reading.value);
  if (sensor?.minThreshold !== null && sensor?.minThreshold !== undefined && value < Number(sensor.minThreshold)) return 'Bajo';
  if (sensor?.maxThreshold !== null && sensor?.maxThreshold !== undefined && value > Number(sensor.maxThreshold)) return 'Alerta';
  return 'Normal';
}

/* Construye un path SVG 'M... L...' normalizado al viewBox 720x180.
 * Mapea valores a coordenadas Y invertidas (top = min, bottom = max). */
function buildLinePath(values) {
  const width = 720;
  const height = 180;
  const top = 18;
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = max - min || 1;
  return values
    .map((value, index) => {
      const x = values.length === 1 ? 0 : (index / (values.length - 1)) * width;
      const y = top + height - ((value - min) / span) * height;
      return `${index === 0 ? 'M' : 'L'}${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(' ');
}
