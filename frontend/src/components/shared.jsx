import { useEffect, useMemo, useState } from 'react';

export function Section({ title, subtitle, children }) {
  return (
    <section className="section">
      <div className="sectionHeader">
        <h2>{title}</h2>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {children}
    </section>
  );
}

export function Panel({ title, children }) {
  return (
    <article className="panel">
      <h3>{title}</h3>
      {children}
    </article>
  );
}

export function Metric({ icon, label, value, tone = 'normal' }) {
  return (
    <article className={`metric ${tone}`}>
      <div className="metricIcon">{icon}</div>
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

export function OperationForm({ title, onSubmit, children }) {
  return (
    <form className="form operationCard" onSubmit={onSubmit}>
      <h3>{title}</h3>
      {children}
    </form>
  );
}

export function List({ title, items = [], empty }) {
  return (
    <div className="list">
      <h3>{title}</h3>
      {items.length === 0 ? <p className="emptyState">{empty}</p> : items.map((item) => <span key={item}>{item}</span>)}
    </div>
  );
}

export function usePagination(items = [], pageSize = 5) {
  const [page, setPage] = useState(1);
  const totalPages = Math.max(1, Math.ceil(items.length / pageSize));

  useEffect(() => {
    setPage(1);
  }, [items.length, pageSize]);

  useEffect(() => {
    setPage((current) => Math.min(current, totalPages));
  }, [totalPages]);

  const pagedItems = useMemo(() => {
    const start = (page - 1) * pageSize;
    return items.slice(start, start + pageSize);
  }, [items, page, pageSize]);

  return {
    page,
    pageSize,
    totalPages,
    totalItems: items.length,
    pagedItems,
    setPage,
    from: items.length === 0 ? 0 : (page - 1) * pageSize + 1,
    to: Math.min(page * pageSize, items.length)
  };
}

export function PaginationControls({ pagination, t }) {
  if (pagination.totalItems === 0) return null;

  return (
    <div className="pagination" aria-label={t.pagination}>
      <span>{t.showingRecords.replace('{from}', pagination.from).replace('{to}', pagination.to).replace('{total}', pagination.totalItems)}</span>
      <div>
        <button type="button" disabled={pagination.page === 1} onClick={() => pagination.setPage(pagination.page - 1)}>
          {t.previous}
        </button>
        <strong>{pagination.page} / {pagination.totalPages}</strong>
        <button type="button" disabled={pagination.page === pagination.totalPages} onClick={() => pagination.setPage(pagination.page + 1)}>
          {t.next}
        </button>
      </div>
    </div>
  );
}
