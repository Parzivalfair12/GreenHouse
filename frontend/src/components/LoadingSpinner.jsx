export function LoadingSpinner({ text }) {
  return (
    <div className="loadingSpinner">
      <div className="spinner" />
      <span>{text ?? '...'}</span>
    </div>
  );
}
