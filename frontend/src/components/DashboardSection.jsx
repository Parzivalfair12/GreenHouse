import { AlertTriangle, Bell, FlaskConical, Leaf, Plus, Power, Radio, Sprout } from 'lucide-react';
import { Metric, Panel, Section } from './shared.jsx';
import { IaPreview } from './IaSection.jsx';

export function DashboardSection({ totals, selected, alerts, dashboard, readings, sensors, t, openAlerts }) {
  return (
    <Section title={t.overview} subtitle={t.dashboardSubtitle}>
      <section className="metrics" aria-label="Metricas">
        <Metric icon={<Leaf />} label={t.greenhouses} value={dashboard?.greenhouses ?? totals.greenhouses} />
        <Metric icon={<Sprout />} label={t.crops} value={totals.crops} />
        <Metric icon={<Radio />} label={t.sensors} value={dashboard?.sensors ?? totals.sensors} />
        <Metric icon={<Power />} label={t.actuatorsOn} value={dashboard?.actuatorsEnabled ?? totals.irrigation} tone="power" />
        <Metric icon={<AlertTriangle />} label={t.pending} value={dashboard?.openAlerts ?? totals.alerts} tone="warning" />
        <Metric icon={<FlaskConical />} label={'IA'} value={<IaPreview readings={readings} sensors={sensors} />} />
      </section>
      <div className="dashboardGrid">
        <SensorChart dashboard={dashboard} selected={selected} readings={readings} sensors={sensors} t={t} />
        <AlertPreview alerts={alerts} t={t} onOpen={openAlerts} />
      </div>
      <OverviewPanel selected={selected} dashboard={dashboard} t={t} />
    </Section>
  );
}

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
            <span className="pill active">{dashboard?.globalStatus ?? 'ESTABLE'}</span>
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

function latestByType(readings, sensorById, ...types) {
  return readings
    .filter((reading) => types.includes(sensorById.get(reading.sensorId)?.type))
    .at(-1);
}

function latestByCode(readings, pattern) {
  return readings.filter((reading) => pattern.test(reading.sensorCode ?? '')).at(-1);
}

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

function statusLabel(reading, sensorById) {
  if (!reading) return 'Sin datos';
  const sensor = sensorById.get(reading.sensorId);
  const value = Number(reading.value);
  if (sensor?.minThreshold !== null && sensor?.minThreshold !== undefined && value < Number(sensor.minThreshold)) return 'Bajo';
  if (sensor?.maxThreshold !== null && sensor?.maxThreshold !== undefined && value > Number(sensor.maxThreshold)) return 'Alerta';
  return 'Normal';
}

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
