import { Pencil, Plus, Save, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { PaginationControls, Panel, Section, usePagination } from './shared.jsx';

/* Componente CRUD genérico reutilizable.
 * Props configurables:
 *   - fields: define los campos del formulario (tipo, placeholder, options, parse, required)
 *   - columns: define las columnas de la tabla (label, key, render opcional)
 *   - items: arreglo de registros a mostrar
 *   - emptyItem: objeto base para resetear el formulario
 *   - onCreate/onUpdate/onDelete: callbacks async hacia el backend
 * Manejo de estado local: form (valores del formulario), editing (ID del registro en edición).
 * Paginación local via hook usePagination (5 items por página). */
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
  deleteLabel,
  t
}) {
  const effectiveDeleteLabel = deleteLabel || t?.deleteItem || 'Delete';
  const [form, setForm] = useState(emptyItem);
  const [editing, setEditing] = useState(null);
  const pagination = usePagination(items, 5);

  /* Sincronización: al salir del modo edición, resetea el formulario a emptyItem */
  useEffect(() => {
    if (!editing) setForm(emptyItem);
  }, [editing, emptyItem]);

  /* Envía el formulario: llama a onUpdate (edición) u onCreate (nuevo), luego resetea */
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

  /* Inicia edición: copia los valores del item al form, mapeando solo los campos definidos en 'fields' */
  function startEdit(item) {
    setEditing(item);
    setForm(fields.reduce((acc, field) => ({ ...acc, [field.name]: item[field.name] ?? emptyItem[field.name] ?? '' }), {}));
  }

  return (
    <Section title={title}>
      <div className="crudGrid">
        {/* Panel del formulario: cambia título según modo edición/creación */}
        <Panel title={editing ? t.edit : formTitle}>
          {/* Si fields está vacío, el CRUD es solo de lectura (oculta formulario) */}
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

        {/* Panel de listado con paginación local */}
        <Panel title={t.records}>
          <div className="crudList">
            {items.length === 0 && <p className="emptyState">{t.noRecords}</p>}
            {pagination.pagedItems.map((item) => (
              <article className="crudRow" key={item.id}>
                <div className="crudValues">
                  {columns.map((column) => (
                    <span key={column.key}>
                      <strong>{column.label}</strong>
                      {/* Si column.render existe, lo usa para renderizado personalizado; si no, muestra item[column.key] */}
                      {column.render ? column.render(item) : item[column.key]}
                    </span>
                  ))}
                </div>
                <div className="rowActions">
                  <button type="button" onClick={() => startEdit(item)}><Pencil size={16} />{t.edit}</button>
                  {/* Botón eliminar oculto si el CRUD es de solo lectura (fields.length === 0) */}
                  {fields.length > 0 && <button className="dangerButton" type="button" onClick={() => onDelete(item)}><Trash2 size={16} />{effectiveDeleteLabel}</button>}
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

/* Componente de campo dinámico: renderiza input según field.type.
 * Soporta: 'select' (con options y placeholder), 'checkbox', y cualquier type HTML nativo (text, number, etc.).
 * Transformación opcional via field.parse (ej. convertir string a número). */
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
