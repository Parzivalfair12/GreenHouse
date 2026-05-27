import { describe, it, expect } from 'vitest';
import {
  loadModel,
  getEntity,
  getField,
  findFKFields,
  generateNodes,
  generateEdges,
  validateModel,
  getEntityFieldCount,
  getEntityCount,
  getEnumCount,
  getEntityNames,
  getEntitiesWithTables,
  exportSchemaSQL,
  exportModelJSON,
  getModel
} from '../../../config/modelParser.js';

describe('modelParser', () => {
  it('getModel returns the model', () => {
    const model = getModel();
    expect(model).toBeDefined();
    expect(model.project).toBe('GreenHouse');
  });

  it('loadModel returns the full model', async () => {
    const model = await loadModel();
    expect(model).toBeDefined();
    expect(model.project).toBe('GreenHouse');
    expect(model.entities).toBeInstanceOf(Array);
    expect(model.enums).toBeInstanceOf(Array);
  });

  it('getEntity returns correct entity', () => {
    const entity = getEntity('Greenhouse');
    expect(entity).toBeDefined();
    expect(entity.name).toBe('Greenhouse');
    expect(entity.table).toBe('greenhouse');
  });

  it('getEntity returns undefined for unknown entity', () => {
    expect(getEntity('Unknown')).toBeUndefined();
  });

  it('getField returns correct field', () => {
    const field = getField('Greenhouse', 'name');
    expect(field).toBeDefined();
    expect(field.name).toBe('name');
    expect(field.type).toBe('String');
  });

  it('findFKFields detects all foreign keys', () => {
    const fks = findFKFields();
    expect(fks.length).toBeGreaterThan(0);
    const greenhouseFK = fks.find(fk => fk.entityName === 'Sensor' && fk.fieldName === 'greenhouse_id');
    expect(greenhouseFK).toBeDefined();
    expect(greenhouseFK.entity).toBe('Greenhouse');
    expect(greenhouseFK.cardinality).toBe('N:1');
  });

  it('generateNodes creates nodes for all entities', () => {
    const nodes = generateNodes();
    expect(nodes.length).toBe(getEntityCount());
    nodes.forEach(node => {
      expect(node.id).toBeDefined();
      expect(node.type).toBe('entityNode');
      expect(node.data.entity).toBeDefined();
      expect(node.data.entity.name).toBe(node.id);
    });
  });

  it('generateEdges creates edges with cardinalities', () => {
    const edges = generateEdges();
    expect(edges.length).toBeGreaterThan(0);
    edges.forEach(edge => {
      expect(edge.id).toBeDefined();
      expect(edge.source).toBeDefined();
      expect(edge.target).toBeDefined();
      expect(edge.data.cardinality).toBeDefined();
    });
  });

  it('validateModel returns no errors for valid model', () => {
    const errors = validateModel();
    expect(Array.isArray(errors)).toBe(true);
    expect(errors.length).toBe(0);
  });

  it('findFKFields contains expected relationships', () => {
    const fks = findFKFields();
    const expectedFKs = [
      { entity: 'Sensor', field: 'greenhouse_id', target: 'Greenhouse' },
      { entity: 'Reading', field: 'sensor_id', target: 'Sensor' }
    ];
    expectedFKs.forEach(({ entity, field, target }) => {
      const found = fks.find(fk => fk.entityName === entity && fk.fieldName === field);
      expect(found).toBeDefined();
      expect(found.entity).toBe(target);
    });
  });

  it('getEntityFieldCount returns correct count', () => {
    const count = getEntityFieldCount('Greenhouse');
    expect(count).toBeGreaterThan(0);
    expect(getEntityFieldCount('Unknown')).toBe(0);
  });

  it('getEntityNames returns all entity names', () => {
    const names = getEntityNames();
    expect(names).toContain('Greenhouse');
    expect(names).toContain('Sensor');
    expect(names).toContain('Reading');
  });

  it('getEntitiesWithTables returns formatted list', () => {
    const list = getEntitiesWithTables();
    expect(list.length).toBe(getEntityCount());
    list.forEach(item => {
      expect(item.name).toBeDefined();
      expect(item.table).toBeDefined();
      expect(item.fieldCount).toBeGreaterThan(0);
    });
  });

  it('exportSchemaSQL generates valid SQL', () => {
    const sql = exportSchemaSQL();
    expect(sql).toContain('CREATE TABLE');
    expect(sql).toContain('greenhouse');
    expect(sql).toContain('sensor');
    expect(sql).toContain('PRIMARY KEY');
  });

  it('exportModelJSON returns valid JSON string', () => {
    const json = exportModelJSON();
    expect(() => JSON.parse(json)).not.toThrow();
    const parsed = JSON.parse(json);
    expect(parsed.project).toBe('GreenHouse');
  });

  it('getEnumCount returns correct count', () => {
    expect(getEnumCount()).toBeGreaterThan(0);
  });

  it('generateEdges has correct cardinalities for OneToMany', () => {
    const edges = generateEdges();
    const ghToSensor = edges.find(e => e.source === 'Greenhouse' && e.target === 'Sensor');
    if (ghToSensor) expect(ghToSensor.data.cardinality).toContain('N');
  });

  it('generateEdges has correct cardinalities for ManyToOne', () => {
    const edges = generateEdges();
    const sensorToGH = edges.find(e => e.source === 'Sensor' && e.target === 'Greenhouse');
    if (sensorToGH) expect(sensorToGH.data.cardinality).toContain('N');
  });
});
