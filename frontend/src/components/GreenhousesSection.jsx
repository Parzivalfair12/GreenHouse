import { Plus, Save, Trash2 } from 'lucide-react';
import { GreenhouseTable } from './GreenhouseTable.jsx';
import { Panel, Section } from './shared.jsx';

export function GreenhousesSection({
  form,
  setForm,
  onSubmit,
  editForm,
  setEditForm,
  onUpdate,
  onDelete,
  greenhouses,
  selected,
  setSelectedId,
  t
}) {
  return (
    <Section title={t.greenhouseManagement}>
      <div className="splitGrid">
        <Panel title={t.addGreenhouse}>
          <form className="form" onSubmit={onSubmit}>
            <input required placeholder={t.name} value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
            <input required placeholder={t.location} value={form.location} onChange={(event) => setForm({ ...form, location: event.target.value })} />
            <input required min="1" type="number" placeholder={t.area} value={form.areaSquareMeters} onChange={(event) => setForm({ ...form, areaSquareMeters: event.target.value })} />
            <button type="submit"><Plus size={18} />{t.addGreenhouse}</button>
          </form>
        </Panel>
        <Panel title={t.editGreenhouse}>
          {selected ? (
            <form className="form" onSubmit={onUpdate}>
              <input required placeholder={t.name} value={editForm.name} onChange={(event) => setEditForm({ ...editForm, name: event.target.value })} />
              <input required placeholder={t.location} value={editForm.location} onChange={(event) => setEditForm({ ...editForm, location: event.target.value })} />
              <input required min="1" type="number" placeholder={t.area} value={editForm.areaSquareMeters} onChange={(event) => setEditForm({ ...editForm, areaSquareMeters: event.target.value })} />
              <label className="checkLine">
                <input type="checkbox" checked={editForm.active} onChange={(event) => setEditForm({ ...editForm, active: event.target.checked })} />
                {t.active}
              </label>
              <button type="submit"><Save size={18} />{t.saveChanges}</button>
              <button className="dangerButton" type="button" onClick={onDelete}><Trash2 size={18} />{t.deleteGreenhouse}</button>
            </form>
          ) : (
            <p className="emptyState">{t.noRecords}</p>
          )}
        </Panel>
        <Panel title={t.greenhouses}>
          <GreenhouseTable greenhouses={greenhouses} selected={selected} setSelectedId={setSelectedId} t={t} />
        </Panel>
      </div>
    </Section>
  );
}
