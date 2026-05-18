import { Download } from 'lucide-react';
import { Panel, Section } from './shared.jsx';

export function DataSection({ onExport, t }) {
  return (
    <Section title={t.dataCenter}>
      <div className="dataGrid">
        <Panel title="PostgreSQL">
          <div className="connectionBox">
            <span>Host: localhost</span>
            <span>Port: 5432</span>
            <span>Database: greenhouse</span>
            <span>User: greenhouse_user</span>
          </div>
        </Panel>
        <Panel title={t.exportJson}>
          <button className="downloadButton" type="button" onClick={onExport}>
            <Download size={18} />
            {t.exportJson}
          </button>
        </Panel>
      </div>
    </Section>
  );
}
