import modelo from './modelo.json';

export async function loadModel() {
  return modelo;
}

export function getModel() {
  return modelo;
}

export function getEntity(name) {
  return (modelo.entities || []).find(e => e.name === name);
}

export function getField(entityName, fieldName) {
  const entity = getEntity(entityName);
  if (!entity || !entity.fields) return undefined;
  return entity.fields.find(f => f.name === fieldName);
}

export function findFKFields() {
  const fks = [];
  for (const entity of (modelo.entities || [])) {
    if (!entity.fields) continue;
    for (const field of entity.fields) {
      if (field.fk && field.references) {
        fks.push({ entityName: entity.name, fieldName: field.name, ...field.references });
      }
    }
  }
  return fks;
}

export function generateNodes() {
  const entityCount = modelo.entities.length;
  const cols = Math.max(1, Math.ceil(Math.sqrt(entityCount)));
  const spacingX = 360;
  const spacingY = 340;
  return modelo.entities.map((entity, index) => {
    const col = index % cols;
    const row = Math.floor(index / cols);
    return {
      id: entity.name,
      type: 'entityNode',
      position: { x: 50 + col * spacingX, y: 50 + row * spacingY },
      data: { entity }
    };
  });
}

export function generateEdges() {
  const relationships = modelo.relationships || [];
  const FK_COLORS = ['#00cc7a', '#0ea5e9', '#d9a400', '#ff5b5b', '#a855f7', '#ec4899', '#06b6d4', '#22c55e'];
  const usedColors = {};
  let colorIndex = 0;
  return relationships.map(rel => {
    const edgeKey = `${rel.from}-${rel.to}-${rel.fk}`;
    if (!usedColors[edgeKey]) {
      usedColors[edgeKey] = FK_COLORS[colorIndex % FK_COLORS.length];
      colorIndex++;
    }
    let sourceCard = '1', targetCard = 'N';
    if (rel.cardinality) {
      const parts = rel.cardinality.split(':');
      sourceCard = parts[0];
      targetCard = parts[1];
    }
    return {
      id: `edge-${rel.from}-${rel.to}-${rel.fk}`,
      source: rel.from, target: rel.to,
      sourceHandle: `handle-${rel.from}-${rel.fk}`,
      targetHandle: `handle-${rel.to}-${rel.fk}`,
      type: 'smoothstep',
      style: { stroke: usedColors[edgeKey], strokeWidth: 2 },
      animated: rel.type === 'OneToMany',
      label: `${sourceCard}:${targetCard}`,
      labelStyle: { fill: usedColors[edgeKey], fontWeight: 700, fontSize: 11 },
      labelBgStyle: { fill: '#0b0d0f', fillOpacity: 0.85, rx: 4 },
      labelBgPadding: [6, 3],
      data: { fk: rel.fk, cardinality: rel.cardinality || `${sourceCard}:${targetCard}` }
    };
  });
}

const SQL_RESERVED = new Set([
  'select', 'from', 'where', 'insert', 'update', 'delete', 'drop', 'alter',
  'create', 'table', 'index', 'view', 'grant', 'revoke', 'order', 'group',
  'having', 'join', 'union', 'all', 'distinct', 'as', 'and', 'or', 'not',
  'in', 'between', 'like', 'is', 'null', 'true', 'false', 'primary', 'key',
  'foreign', 'references', 'constraint', 'default', 'check', 'unique'
]);

export function validateModel() {
  const errors = [];
  const entityNames = new Set((modelo.entities || []).map(e => e.name));
  const tableNames = new Set();
  const enumNames = new Set((modelo.enums || []).map(e => e.name));
  for (const entity of (modelo.entities || [])) {
    if (tableNames.has(entity.table)) errors.push(`Duplicate table: ${entity.table}`);
    tableNames.add(entity.table);
    if (SQL_RESERVED.has(entity.table.toLowerCase())) errors.push(`Reserved table: ${entity.table}`);
    if (!entity.fields) continue;
    const fieldNames = new Set();
    let hasPk = false;
    for (const field of entity.fields) {
      if (fieldNames.has(field.name)) errors.push(`Duplicate field '${field.name}' in ${entity.name}`);
      fieldNames.add(field.name);
      if (!field.type) errors.push(`Field '${field.name}' in ${entity.name} has no type`);
      if (field.pk) { hasPk = true; if (field.nullable) errors.push(`PK '${field.name}' cannot be nullable`); }
      if (field.fk) {
        if (!field.references) errors.push(`FK '${field.name}' has no references`);
        else {
          if (!entityNames.has(field.references.entity)) errors.push(`FK '${field.name}' references unknown '${field.references.entity}'`);
          if (field.references.cardinality && !['1:1','1:N','N:1','N:M'].includes(field.references.cardinality)) errors.push(`Invalid cardinality '${field.references.cardinality}'`);
        }
      }
      if (field.enum && field.enum.length > 0 && !entityNames.has(field.type) && !enumNames.has(field.type)) errors.push(`Enum '${field.type}' not found`);
      if (SQL_RESERVED.has(field.name.toLowerCase())) errors.push(`Reserved field: ${field.name}`);
    }
    if (!hasPk) errors.push(`Entity ${entity.name} has no primary key`);
  }
  for (const rel of (modelo.relationships || [])) {
    if (!entityNames.has(rel.from)) errors.push(`Relationship from unknown '${rel.from}'`);
    if (!entityNames.has(rel.to)) errors.push(`Relationship to unknown '${rel.to}'`);
  }
  return errors;
}

export function getEntityCount() { return modelo.entities.length; }
export function getEnumCount() { return (modelo.enums || []).length; }
export function getEntityFieldCount(name) { const e = getEntity(name); return e && e.fields ? e.fields.length : 0; }
export function getEntityNames() { return modelo.entities.map(e => e.name); }

export function getEntitiesWithTables() {
  return modelo.entities.map(e => ({
    name: e.name, table: e.table, description: e.description,
    fieldCount: e.fields ? e.fields.length : 0
  }));
}

export function exportModelJSON() { return JSON.stringify(modelo, null, 2); }

const SQL_TYPE_MAP = {
  'Long':'BIGSERIAL','String':'VARCHAR(255)','BigDecimal':'DECIMAL(19,2)',
  'int':'INTEGER','boolean':'BOOLEAN','LocalDate':'DATE',
  'LocalDateTime':'TIMESTAMP','Instant':'TIMESTAMPTZ',
  'CropStatus':'VARCHAR(20)','SensorType':'VARCHAR(20)',
  'AlertSeverity':'VARCHAR(20)','IrrigationMode':'VARCHAR(20)',
  'ActuatorType':'VARCHAR(20)','RuleType':'VARCHAR(30)',
  'ActionOrigin':'VARCHAR(20)','UserRole':'VARCHAR(20)',
  'Text':'TEXT','UUID':'UUID'
};

function quoteIdent(name) { return name ? '"' + name.replace(/"/g,'""') + '"' : '""'; }
function formatDefault(def) {
  if (!def) return '';
  if (def === 'now()') return 'DEFAULT NOW()';
  if (def === 'true' || def === 'false') return 'DEFAULT ' + def;
  if (/^-?\d+(\.\d+)?$/.test(def)) return 'DEFAULT ' + def;
  return "DEFAULT '" + def.replace(/'/g,"''") + "'";
}

export function exportSchemaSQL() {
  let sql = '-- GreenHouse Schema\n-- Generated from modelo.json\n\n';
  for (const entity of (modelo.entities || [])) {
    if (!entity.fields) continue;
    const tn = quoteIdent(entity.table);
    sql += `CREATE TABLE ${tn} (\n`;
    const lines = [];
    for (const f of entity.fields) {
      if (!f.name) continue;
      let l = `  ${quoteIdent(f.name)} ${SQL_TYPE_MAP[f.type]||'VARCHAR(255)'}`;
      if (f.pk) l += ' PRIMARY KEY';
      if (!f.nullable) l += ' NOT NULL';
      if (f.default) l += ' ' + formatDefault(f.default);
      if (f.unique && !f.pk) l += ' UNIQUE';
      lines.push(l);
    }
    for (const f of entity.fields) {
      if (!f.fk || !f.references) continue;
      const ref = (modelo.entities||[]).find(e => e.name === f.references.entity);
      if (ref) lines.push(`  FOREIGN KEY (${quoteIdent(f.name)}) REFERENCES ${quoteIdent(ref.table)}(${quoteIdent(f.references.field||'id')})`);
    }
    sql += lines.join(',\n') + '\n);\n';
    for (const f of entity.fields) {
      if (f.unique && !f.pk && f.name) sql += `CREATE UNIQUE INDEX ${quoteIdent('uq_'+entity.table+'_'+f.name)} ON ${tn}(${quoteIdent(f.name)});\n`;
    }
    if (entity.indexes) for (const idx of entity.indexes) {
      if (!idx.name || !idx.columns || !idx.columns.length) continue;
      sql += `${idx.unique?'CREATE UNIQUE INDEX':'CREATE INDEX'} ${quoteIdent(idx.name)} ON ${tn} (${idx.columns.map(c=>quoteIdent(c)).join(', ')});\n`;
    }
    sql += '\n';
  }
  return sql;
}

export function exportSchemaSQLEntity(entityName) {
  const entity = (modelo.entities||[]).find(e => e.name === entityName);
  if (!entity) return `-- Entity not found: ${entityName}`;
  if (!entity.fields) return `-- ${entityName}: no fields defined`;
  const tn = quoteIdent(entity.table);
  let sql = `CREATE TABLE ${tn} (\n`;
  const lines = [];
  for (const f of entity.fields) {
    if (!f.name) continue;
    let l = `  ${quoteIdent(f.name)} ${SQL_TYPE_MAP[f.type]||'VARCHAR(255)'}`;
    if (f.pk) l += ' PRIMARY KEY';
    if (!f.nullable) l += ' NOT NULL';
    if (f.default) l += ' ' + formatDefault(f.default);
    if (f.unique && !f.pk) l += ' UNIQUE';
    lines.push(l);
  }
  for (const f of entity.fields) {
    if (!f.fk || !f.references) continue;
    const ref = (modelo.entities||[]).find(e => e.name === f.references.entity);
    if (ref) lines.push(`  FOREIGN KEY (${quoteIdent(f.name)}) REFERENCES ${quoteIdent(ref.table)}(${quoteIdent(f.references.field||'id')})`);
  }
  sql += lines.join(',\n') + '\n);\n';
  return sql;
}
