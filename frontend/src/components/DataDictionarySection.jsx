import { useState } from 'react';
import { ChevronDown, ChevronRight, Key, Link2, ListChecks, Table2 } from 'lucide-react';
import { Panel, Section } from './shared.jsx';

/* Modelo de datos estático del dominio GreenHouse.
 * Cada entidad refleja una tabla de la base de datos PostgreSQL.
 * Estructura: { name, table, description, fields[], relations[] }
 *   - fields: { name, type, pk, nullable, default, enum, unique, validation, desc }
 *   - relations: lista de nombres de entidades relacionadas (FK lógicas)
 * Las entidades con field.enum = true se marcan con icono ListChecks.
 * Los fields con pk = true muestran icono Key; unique muestra "U"; validation muestra "V". */
const ENTITIES = [
  {
    name: 'Greenhouse', table: 'greenhouse', description: 'Invernadero que agrupa cultivos, sensores y riegos',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'name', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Nombre visible' },
      { name: 'location', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Ubicacion fisica' },
      { name: 'areaSquareMeters', type: 'BigDecimal', pk: false, nullable: false, validation: '@Positive', desc: 'Area en m2' },
      { name: 'active', type: 'boolean', pk: false, nullable: false, default: 'true', desc: 'Estado operativo' }
    ],
    relations: ['Crop', 'Sensor', 'Zone', 'Actuator', 'AutomationRule', 'IrrigationEvent']
  },
  {
    name: 'Crop', table: 'crop', description: 'Cultivo plantado dentro de un invernadero',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'name', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Nombre del cultivo' },
      { name: 'variety', type: 'String', pk: false, nullable: true, desc: 'Variedad del cultivo' },
      { name: 'plantedAt', type: 'LocalDate', pk: false, nullable: true, desc: 'Fecha de siembra' },
      { name: 'expectedHarvestAt', type: 'LocalDate', pk: false, nullable: true, desc: 'Fecha estimada de cosecha' },
      { name: 'status', type: 'CropStatus', pk: false, nullable: false, enum: true, default: 'GERMINATING', desc: 'Estado del cultivo' }
    ],
    relations: ['Greenhouse']
  },
  {
    name: 'Sensor', table: 'sensor', description: 'Dispositivo que captura mediciones ambientales',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'code', type: 'String', pk: false, nullable: false, unique: true, validation: '@NotBlank', desc: 'Codigo unico del sensor' },
      { name: 'type', type: 'SensorType', pk: false, nullable: false, enum: true, desc: 'Tipo de sensor' },
      { name: 'unit', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Unidad de medida' },
      { name: 'minThreshold', type: 'BigDecimal', pk: false, nullable: true, desc: 'Umbral minimo' },
      { name: 'maxThreshold', type: 'BigDecimal', pk: false, nullable: true, desc: 'Umbral maximo' }
    ],
    relations: ['Greenhouse', 'Zone', 'Reading', 'Alert']
  },
  {
    name: 'Reading', table: 'reading', description: 'Medicion capturada por un sensor',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'value', type: 'BigDecimal', pk: false, nullable: false, column: 'reading_value', desc: 'Valor de la lectura' },
      { name: 'recordedAt', type: 'LocalDateTime', pk: false, nullable: false, desc: 'Fecha y hora de la lectura' }
    ],
    relations: ['Sensor']
  },
  {
    name: 'Alert', table: 'alert', description: 'Alerta operativa cuando un sensor sale de rango',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'severity', type: 'AlertSeverity', pk: false, nullable: false, enum: true, default: 'INFO', desc: 'Severidad de la alerta' },
      { name: 'message', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Mensaje de la alerta' },
      { name: 'resolved', type: 'boolean', pk: false, nullable: false, default: 'false', desc: 'Indica si la alerta fue resuelta' },
      { name: 'createdAt', type: 'LocalDateTime', pk: false, nullable: false, default: 'now()', desc: 'Fecha de creacion' }
    ],
    relations: ['Sensor']
  },
  {
    name: 'Actuator', table: 'actuator', description: 'Dispositivo simulado activable manualmente o por reglas',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'name', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Nombre del actuador' },
      { name: 'type', type: 'ActuatorType', pk: false, nullable: false, enum: true, default: 'IRRIGATION', desc: 'Tipo de actuador' },
      { name: 'enabled', type: 'boolean', pk: false, nullable: false, default: 'false', desc: 'Estado habilitado' },
      { name: 'active', type: 'boolean', pk: false, nullable: false, default: 'true', desc: 'Estado activo' }
    ],
    relations: ['Greenhouse']
  },
  {
    name: 'AutomationRule', table: 'automation_rule', description: 'Regla de automatizacion evaluada al registrar lecturas',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'name', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Nombre de la regla' },
      { name: 'type', type: 'RuleType', pk: false, nullable: false, enum: true, desc: 'Tipo de regla' },
      { name: 'threshold', type: 'BigDecimal', pk: false, nullable: true, desc: 'Umbral de la regla' },
      { name: 'enabled', type: 'boolean', pk: false, nullable: false, default: 'true', desc: 'Estado de la regla' }
    ],
    relations: ['Greenhouse']
  },
  {
    name: 'Zone', table: 'zone', description: 'Area funcional dentro de un invernadero',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'name', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Nombre de la zona' },
      { name: 'description', type: 'String', pk: false, nullable: true, desc: 'Descripcion de la zona' },
      { name: 'active', type: 'boolean', pk: false, nullable: false, default: 'true', desc: 'Estado activo' }
    ],
    relations: ['Greenhouse']
  },
  {
    name: 'IrrigationEvent', table: 'irrigation_event', description: 'Evento de riego registrado',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'startedAt', type: 'LocalDateTime', pk: false, nullable: true, desc: 'Fecha y hora de inicio' },
      { name: 'durationMinutes', type: 'int', pk: false, nullable: false, validation: '@Positive', desc: 'Duracion en minutos' },
      { name: 'waterLiters', type: 'BigDecimal', pk: false, nullable: false, validation: '@Positive', desc: 'Litros de agua utilizados' },
      { name: 'mode', type: 'IrrigationMode', pk: false, nullable: false, enum: true, default: 'AUTOMATIC', desc: 'Modo de riego' }
    ],
    relations: ['Greenhouse']
  },
  {
    name: 'AuditLog', table: 'audit_log', description: 'Log operativo de acciones manuales y automaticas',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'action', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Accion realizada' },
      { name: 'detail', type: 'String', pk: false, nullable: true, desc: 'Detalle de la accion' },
      { name: 'createdAt', type: 'LocalDateTime', pk: false, nullable: false, default: 'now()', desc: 'Fecha de creacion' },
      { name: 'origin', type: 'ActionOrigin', pk: false, nullable: false, enum: true, default: 'MANUAL', desc: 'Origen de la accion' }
    ],
    relations: []
  },
  {
    name: 'AppUser', table: 'app_user', description: 'Usuario autenticado del sistema',
    fields: [
      { name: 'id', type: 'Long', pk: true, nullable: false, desc: 'Identificador unico' },
      { name: 'email', type: 'String', pk: false, nullable: false, unique: true, validation: '@Email @NotBlank', desc: 'Correo electronico' },
      { name: 'fullName', type: 'String', pk: false, nullable: false, validation: '@NotBlank', desc: 'Nombre completo' },
      { name: 'passwordHash', type: 'String', pk: false, nullable: false, desc: 'Hash de contrasena' },
      { name: 'provider', type: 'String', pk: false, nullable: false, default: 'email', desc: 'Proveedor de autenticacion' },
      { name: 'role', type: 'UserRole', pk: false, nullable: false, enum: true, default: 'VIEWER', desc: 'Rol del usuario' },
      { name: 'verified', type: 'boolean', pk: false, nullable: false, default: 'false', desc: 'Email verificado' }
    ],
    relations: []
  }
];

/* Renderiza una fila de campo con indicadores visuales:
 *   - PK (Key icon), Unique (U), Enum (ListChecks), Validation (V)
 *   - Tipo, nulabilidad (✓/—), valor por defecto y descripción.
 * La descripción cae a field.validation si field.desc no existe. */
function FieldRow({ field }) {
  return (
    <tr>
      <td className="dicField">
        {field.pk && <Key size={14} className="dicIcon" title="PK" />}
        {field.unique && <span className="dicUnique" title="Unique">U</span>}
        {field.enum && <ListChecks size={14} className="dicIcon" title="Enum" />}
        {field.validation && <span className="dicValidation" title={field.validation}>V</span>}
        {field.name}
      </td>
      <td className="dicType">{field.type}</td>
      <td className="dicNull">{field.nullable ? '✓' : '—'}</td>
      <td className="dicDefault">{field.default ?? '—'}</td>
      <td className="dicDesc">{field.desc ?? field.validation ?? ''}</td>
    </tr>
  );
}

/* Tarjeta colapsable de una entidad del diccionario de datos.
 * Manejo de estado: open/close local con useState, defaultOpen para la primera entidad.
 * Renderiza cabecera (nombre, tabla, conteos), tabla de campos, y relaciones FK.
 * Las relaciones se muestran como tags con nombres de entidades relacionadas. */
function EntityCard({ entity, defaultOpen, t }) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div className="dicCard">
      {/* Header clickeable: expande/colapsa */}
      <div className="dicHeader" onClick={() => setOpen(!open)}>
        <span className="dicHeaderLeft">
          {open ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
          <Table2 size={18} />
          <strong>{entity.name}</strong>
          <code className="dicTable">{entity.table}</code>
          <span className="dicFieldsCount">{entity.fields.length} {t.fieldsCount}</span>
          {entity.relations?.length > 0 && (
            <span className="dicRelCount">{entity.relations.length} {t.relationsCount}</span>
          )}
        </span>
        {entity.description && <span className="dicDesc">{entity.description}</span>}
      </div>
      {open && (
        <div className="dicBody">
          {/* Tabla de campos con tipo, nulabilidad, default y descripción */}
          <table className="dicTable">
            <thead>
              <tr>
                <th>{t.field}</th>
                <th>{t.fieldType}</th>
                <th>{t.nullable}</th>
                <th>{t.defaultValue}</th>
                <th>{t.description}</th>
              </tr>
            </thead>
            <tbody>
              {entity.fields.map((f) => <FieldRow key={f.name} field={f} />)}
            </tbody>
          </table>
          {/* Relaciones FK con otras entidades */}
          {entity.relations && entity.relations.length > 0 && (
            <div className="dicRelations">
              <Link2 size={16} />
              <span>{t.relations}: </span>
              {entity.relations.map((r, i) => (
                <span key={r} className="dicRelTag">{r}{i < entity.relations.length - 1 ? ', ' : ''}</span>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

/* Sección de diccionario de datos.
 * Renderiza el modelo ENTITIES (estático, parseado del modelo JPA) como tarjetas colapsables.
 * Generación de tabla: cada entidad → tabla con campos, tipos, restricciones y descripciones.
 * Relaciones FK: se listan como tags al final de cada entidad.
 * Enums: los campos con enum: true se marcan con icono ListChecks en la leyenda y en FieldRow.
 * i18n: todos los encabezados y leyendas via prop 't'. */
export function DataDictionarySection({ t }) {
  return (
    <Section title={t.dataDictionaryTitle} subtitle={t.dataDictionarySubtitle}>
      {/* Leyenda de iconos: PK, Unique, Enum, Validation */}
      <div className="dicLegend">
        <span><Key size={14} /> {t.primaryKey}</span>
        <span><span className="dicUniqueBadge">U</span> {t.uniqueConstraint}</span>
        <span><ListChecks size={14} /> {t.enumLabel}</span>
        <span><span className="dicValidationBadge">V</span> {t.validationLabel}</span>
      </div>
      {/* Entidades del modelo: primera abierta por defecto (defaultOpen={i === 0}) */}
      {ENTITIES.map((e, i) => <EntityCard key={e.name} entity={e} defaultOpen={i === 0} t={t} />)}
    </Section>
  );
}
