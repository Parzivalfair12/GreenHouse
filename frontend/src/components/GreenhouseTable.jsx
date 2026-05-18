import { PaginationControls, usePagination } from './shared.jsx';

export function GreenhouseTable({ greenhouses, selected, setSelectedId, t }) {
  const pagination = usePagination(greenhouses, 3);

  return (
    <div className="table" role="table" aria-label={t.greenhouses}>
      {pagination.pagedItems.map((greenhouse) => (
        <button className={greenhouse.id === selected?.id ? 'row selectedRow' : 'row'} type="button" key={greenhouse.id} onClick={() => setSelectedId(greenhouse.id)}>
          <div>
            <strong>{greenhouse.name}</strong>
            <span>{greenhouse.location}</span>
          </div>
          <div>{greenhouse.areaSquareMeters} m2</div>
          <div>{greenhouse.sensorCount} {t.sensors.toLowerCase()}</div>
          <div className={greenhouse.active ? 'pill active' : 'pill'}>{greenhouse.active ? t.active : t.inactive}</div>
        </button>
      ))}
      <PaginationControls pagination={pagination} t={t} />
    </div>
  );
}
