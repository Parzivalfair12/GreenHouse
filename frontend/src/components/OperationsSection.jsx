import { useState } from 'react';
import { Pencil, Plus, Save } from 'lucide-react';
import { OperationForm, PaginationControls, Section, usePagination } from './shared.jsx';

export function OperationsSection({
  greenhouses,
  selected,
  setSelectedId,
  cropForm,
  setCropForm,
  sensorForm,
  setSensorForm,
  irrigationForm,
  setIrrigationForm,
  onAddCrop,
  onAddSensor,
  onAddIrrigation,
  onUpdateCrop,
  onUpdateSensor,
  onUpdateIrrigation,
  t
}) {
  const [editingCrop, setEditingCrop] = useState(null);
  const [editingSensor, setEditingSensor] = useState(null);
  const [editingIrrigation, setEditingIrrigation] = useState(null);

  return (
    <Section title={t.operationsCenter} subtitle={t.operationsSubtitle}>
      <div className="sectionToolbar">
        <label>
          {t.greenhouses}
          <select value={selected?.id ?? ''} onChange={(event) => setSelectedId(Number(event.target.value))}>
            {greenhouses.map((greenhouse) => (
              <option key={greenhouse.id} value={greenhouse.id}>{greenhouse.name}</option>
            ))}
          </select>
        </label>
      </div>
      {selected && (
        <>
          <div className="operationGrid">
            <OperationForm title={t.addCrop} onSubmit={onAddCrop}>
              <input required placeholder={t.cropName} value={cropForm.name} onChange={(event) => setCropForm({ ...cropForm, name: event.target.value })} />
              <input placeholder={t.variety} value={cropForm.variety} onChange={(event) => setCropForm({ ...cropForm, variety: event.target.value })} />
              <input type="date" value={cropForm.plantedAt} onChange={(event) => setCropForm({ ...cropForm, plantedAt: event.target.value })} />
              <button type="submit"><Plus size={18} />{t.addCrop}</button>
            </OperationForm>
            <OperationForm title={t.addSensor} onSubmit={onAddSensor}>
              <input required placeholder={t.sensorCode} value={sensorForm.code} onChange={(event) => setSensorForm({ ...sensorForm, code: event.target.value })} />
              <select value={sensorForm.type} onChange={(event) => setSensorForm({ ...sensorForm, type: event.target.value })}>
                <option value="TEMPERATURE">{t.sensorTypes.TEMPERATURE}</option>
                <option value="HUMIDITY">{t.sensorTypes.HUMIDITY}</option>
                <option value="SOIL_MOISTURE">{t.sensorTypes.SOIL_MOISTURE}</option>
                <option value="LIGHT">{t.sensorTypes.LIGHT}</option>
              </select>
              <input required placeholder={t.unit} value={sensorForm.unit} onChange={(event) => setSensorForm({ ...sensorForm, unit: event.target.value })} />
              <button type="submit"><Plus size={18} />{t.addSensor}</button>
            </OperationForm>
            <OperationForm title={t.addIrrigation} onSubmit={onAddIrrigation}>
              <input required min="1" type="number" placeholder={t.duration} value={irrigationForm.durationMinutes} onChange={(event) => setIrrigationForm({ ...irrigationForm, durationMinutes: event.target.value })} />
              <input required min="1" type="number" placeholder={t.water} value={irrigationForm.waterLiters} onChange={(event) => setIrrigationForm({ ...irrigationForm, waterLiters: event.target.value })} />
              <select value={irrigationForm.mode} onChange={(event) => setIrrigationForm({ ...irrigationForm, mode: event.target.value })}>
                <option value="MANUAL">{t.irrigationModes.MANUAL}</option>
                <option value="AUTOMATIC">{t.irrigationModes.AUTOMATIC}</option>
              </select>
              <button type="submit"><Plus size={18} />{t.addIrrigation}</button>
            </OperationForm>
          </div>
          <DetailLists
            selected={selected}
            t={t}
            onEditCrop={setEditingCrop}
            onEditSensor={setEditingSensor}
            onEditIrrigation={setEditingIrrigation}
          />
          <EditForms
            crop={editingCrop}
            setCrop={setEditingCrop}
            sensor={editingSensor}
            setSensor={setEditingSensor}
            irrigation={editingIrrigation}
            setIrrigation={setEditingIrrigation}
            onUpdateCrop={onUpdateCrop}
            onUpdateSensor={onUpdateSensor}
            onUpdateIrrigation={onUpdateIrrigation}
            t={t}
          />
        </>
      )}
    </Section>
  );
}

function DetailLists({ selected, t, onEditCrop, onEditSensor, onEditIrrigation }) {
  return (
    <div className="lists">
      <EditableList title={t.crops} items={selected.crops} empty={t.noRecords} render={(crop) => `${crop.name} - ${crop.status}`} onEdit={onEditCrop} t={t} />
      <EditableList title={t.sensors} items={selected.sensors} empty={t.noRecords} render={(sensor) => `${sensor.code} - ${sensor.type} (${sensor.unit})`} onEdit={onEditSensor} t={t} />
      <EditableList title={t.irrigation} items={selected.irrigationEvents} empty={t.noRecords} render={(event) => `${event.waterLiters} L / ${event.durationMinutes} min / ${event.mode}`} onEdit={onEditIrrigation} t={t} />
    </div>
  );
}

function EditableList({ title, items = [], empty, render, onEdit, t }) {
  const pagination = usePagination(items, 4);

  return (
    <div className="list">
      <h3>{title}</h3>
      {items.length === 0 ? <p className="emptyState">{empty}</p> : pagination.pagedItems.map((item) => (
        <div className="editableItem" key={item.id}>
          <span>{render(item)}</span>
            <button type="button" onClick={() => onEdit(item)} aria-label={t.edit}>
            <Pencil size={16} />
          </button>
        </div>
      ))}
      <PaginationControls pagination={pagination} t={t} />
    </div>
  );
}

function EditForms({
  crop,
  setCrop,
  sensor,
  setSensor,
  irrigation,
  setIrrigation,
  onUpdateCrop,
  onUpdateSensor,
  onUpdateIrrigation,
  t
}) {
  return (
    <div className="operationGrid editGrid">
      {crop && (
        <OperationForm title={`${t.edit}: ${t.crops}`} onSubmit={(event) => {
          event.preventDefault();
          onUpdateCrop(crop.id, crop);
          setCrop(null);
        }}>
          <input required placeholder={t.cropName} value={crop.name} onChange={(event) => setCrop({ ...crop, name: event.target.value })} />
          <input placeholder={t.variety} value={crop.variety ?? ''} onChange={(event) => setCrop({ ...crop, variety: event.target.value })} />
          <input type="date" value={crop.plantedAt ?? ''} onChange={(event) => setCrop({ ...crop, plantedAt: event.target.value })} />
          <input type="date" value={crop.expectedHarvestAt ?? ''} onChange={(event) => setCrop({ ...crop, expectedHarvestAt: event.target.value })} />
          <button type="submit"><Save size={18} />{t.saveChanges}</button>
        </OperationForm>
      )}

      {sensor && (
        <OperationForm title={`${t.edit}: ${t.sensors}`} onSubmit={(event) => {
          event.preventDefault();
          onUpdateSensor(sensor.id, sensor);
          setSensor(null);
        }}>
          <input required placeholder={t.sensorCode} value={sensor.code} onChange={(event) => setSensor({ ...sensor, code: event.target.value })} />
          <select value={sensor.type} onChange={(event) => setSensor({ ...sensor, type: event.target.value })}>
            <option value="TEMPERATURE">{t.sensorTypes.TEMPERATURE}</option>
            <option value="HUMIDITY">{t.sensorTypes.HUMIDITY}</option>
            <option value="SOIL_MOISTURE">{t.sensorTypes.SOIL_MOISTURE}</option>
            <option value="LIGHT">{t.sensorTypes.LIGHT}</option>
          </select>
          <input required placeholder={t.unit} value={sensor.unit} onChange={(event) => setSensor({ ...sensor, unit: event.target.value })} />
          <input type="number" placeholder={t.min} value={sensor.minThreshold ?? ''} onChange={(event) => setSensor({ ...sensor, minThreshold: event.target.value })} />
          <input type="number" placeholder={t.max} value={sensor.maxThreshold ?? ''} onChange={(event) => setSensor({ ...sensor, maxThreshold: event.target.value })} />
          <button type="submit"><Save size={18} />{t.saveChanges}</button>
        </OperationForm>
      )}

      {irrigation && (
        <OperationForm title={`${t.edit}: ${t.irrigation}`} onSubmit={(event) => {
          event.preventDefault();
          onUpdateIrrigation(irrigation.id, irrigation);
          setIrrigation(null);
        }}>
          <input required min="1" type="number" placeholder={t.duration} value={irrigation.durationMinutes} onChange={(event) => setIrrigation({ ...irrigation, durationMinutes: event.target.value })} />
          <input required min="1" type="number" placeholder={t.water} value={irrigation.waterLiters} onChange={(event) => setIrrigation({ ...irrigation, waterLiters: event.target.value })} />
          <select value={irrigation.mode} onChange={(event) => setIrrigation({ ...irrigation, mode: event.target.value })}>
            <option value="MANUAL">{t.irrigationModes.MANUAL}</option>
            <option value="AUTOMATIC">{t.irrigationModes.AUTOMATIC}</option>
          </select>
          <button type="submit"><Save size={18} />{t.saveChanges}</button>
        </OperationForm>
      )}
    </div>
  );
}
