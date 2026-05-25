export function LoadingSpinner({ text = 'Cargando...' }) {
  return (
    <div className="loadingSpinner">
      <div className="spinner" />
      <span>{text}</span>
    </div>
  );
}
