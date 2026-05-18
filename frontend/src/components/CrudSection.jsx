import { Pencil, Plus, Save, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { PaginationControls, Panel, Section, usePagination } from './shared.jsx';

export function CrudSection({
  title,
  formTitle,
  items,
  emptyItem,
  fields,
  columns,
  onCreate,
  onUpdate,
  onDelete,
  deleteLabel = 'Eliminar',
  t
}) {
  const [form, setForm] = useState(emptyItem);
  const [editing, setEditing] = useState(null);
  const pagination = usePagination(items, 5);

  useEffect(() => {
    if (!editing) setForm(emptyItem);
  }, [editing, emptyItem]);

  async function submit(event) {
    event.preventDefault();
    if (editing) {
      await onUpdate(editing.id, form);
      setEditing(null);
    } else {
      await onCreate(form);
    }
    setForm(emptyItem);
  }

  function startEdit(item) {
    setEditing(item);
    setForm(fields.reduce((acc, field) => ({ ...acc, [field.name]: item[field.name] ?? emptyItem[field.name] ?? '' }), {}));
  }

  return (
    <Section title={title}>
      <div className="crudGrid">
        <Panel title={editing ? t.edit : formTitle}>
          {fields.length === 0 ? <p className="emptyState">{t.readOnly}</p> : (
            <form className="form" onSubmit={submit}>
              {fields.map((field) => (
                <Field key={field.name} field={field} value={form[field.name]} onChange={(value) => setForm({ ...form, [field.name]: value })} />
              ))}
              <button type="submit">
                {editing ? <Save size={18} /> : <Plus size={18} />}
                {editing ? t.saveChanges : formTitle}
              </button>
              {editing && <button className="secondaryButton" type="button" onClick={() => setEditing(null)}>{t.cancel}</button>}
            </form>
          )}
        </Panel>

        <Panel title={t.records}>
          <div className="crudList">
            {items.length === 0 && <p className="emptyState">{t.noRecords}</p>}
            {pagination.pagedItems.map((item) => (
              <article className="crudRow" key={item.id}>
                <div className="crudValues">
                  {columns.map((column) => (
                    <span key={column.key}>
                      <strong>{column.label}</strong>
                      {column.render ? column.render(item) : item[column.key]}
                    </span>
                  ))}
                </div>
                <div className="rowActions">
                  <button type="button" onClick={() => startEdit(item)}><Pencil size={16} />{t.edit}</button>
                  {fields.length > 0 && <button className="dangerButton" type="button" onClick={() => onDelete(item)}><Trash2 size={16} />{deleteLabel}</button>}
                </div>
              </article>
            ))}
            <PaginationControls pagination={pagination} t={t} />
          </div>
        </Panel>
      </div>
    </Section>
  );
}

function Field({ field, value, onChange }) {
  if (field.type === 'select') {
    return (
      <select value={value ?? ''} onChange={(event) => onChange(field.parse ? field.parse(event.target.value) : event.target.value)} required={field.required}>
        <option value="">{field.placeholder}</option>
        {field.options.map((option) => (
          <option key={option.value} value={option.value}>{option.label}</option>
        ))}
      </select>
    );
  }

  if (field.type === 'checkbox') {
    return (
      <label className="checkLine">
        <input type="checkbox" checked={Boolean(value)} onChange={(event) => onChange(event.target.checked)} />
        {field.placeholder}
      </label>
    );
  }

  return (
    <input
      required={field.required}
      min={field.min}
      type={field.type ?? 'text'}
      placeholder={field.placeholder}
      value={value ?? ''}
      onChange={(event) => onChange(field.parse ? field.parse(event.target.value) : event.target.value)}
    />
  );
}
