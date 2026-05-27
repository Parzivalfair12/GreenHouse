import { useState, useEffect } from 'react';
import { Database, Download, FileText, Search } from 'lucide-react';
import { loadModel, getEntitiesWithTables, getEntityCount, getEnumCount, exportModelJSON, exportSchemaSQL } from '../../config/modelParser.js';

export function ERDSidebar({ activeEntity, onSelectEntity }) {
  const [search, setSearch] = useState('');
  const [entities, setEntities] = useState([]);
  const [model, setModel] = useState(null);
  const [stats, setStats] = useState({ entities: 0, enums: 0 });

  useEffect(() => {
    Promise.all([
      loadModel(),
      getEntitiesWithTables(),
      getEntityCount(),
      getEnumCount()
    ]).then(([m, e, ec, enc]) => {
      setModel(m);
      setEntities(e);
      setStats({ entities: ec, enums: enc });
    });
  }, []);

  const filtered = entities.filter(e =>
    e.name.toLowerCase().includes(search.toLowerCase()) ||
    e.table.toLowerCase().includes(search.toLowerCase())
  );

  function handleExportJSON() {
    const json = exportModelJSON();
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'modelo.json';
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 100);
  }

  function handleExportSQL() {
    const sql = exportSchemaSQL();
    const blob = new Blob([sql], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'schema.sql';
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 100);
  }

  if (!model) return <div className="erdSidebar"><div style={{ padding: 20, color: 'var(--muted)', fontSize: '0.82rem' }}>Loading...</div></div>;

  return (
    <div className="erdSidebar">
      <div className="erdSidebarHeader">
        <h3>Entities</h3>
        <p>Data model · {model.project}</p>
        <div className="erdStats">
          <span className="erdStat highlight">{stats.entities} entities</span>
          <span className="erdStat">{stats.enums} enums</span>
        </div>
      </div>

      <div>
        <div style={{ position: 'relative', margin: '8px 16px' }}>
          <Search size={14} style={{ position: 'absolute', left: 10, top: 10, color: 'var(--muted)' }} />
          <input
            className="erdSearch"
            placeholder="Search entities..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{ paddingLeft: 30 }}
          />
        </div>
      </div>

      <div className="erdEntityList">
        {filtered.map((e) => (
          <div
            key={e.name}
            className={`erdEntityItem ${activeEntity === e.name ? 'active' : ''}`}
            onClick={() => onSelectEntity(e.name)}
          >
            <span className="erdEntityItemName">
              <Database size={14} style={{ color: 'var(--accent)', flexShrink: 0 }} />
              {e.name}
            </span>
            <span className="erdEntityItemBadge">{e.fieldCount}</span>
          </div>
        ))}
      </div>

      <div className="erdSidebarFooter">
        <button className="erdExportBtn" onClick={handleExportJSON}>
          <Download size={14} /> Export JSON
        </button>
        <button className="erdExportBtn" onClick={handleExportSQL}>
          <FileText size={14} /> Export SQL
        </button>
      </div>
    </div>
  );
}
