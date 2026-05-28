import { useEffect, useState } from 'react';
import { X } from 'lucide-react';

let toastId = 0;
let addToastGlobal = null;

export function showToast(message, type = 'success') {
  if (addToastGlobal) {
    addToastGlobal({ id: ++toastId, message, type });
  }
}

export function ToastContainer({ t }) {
  const [toasts, setToasts] = useState([]);

  useEffect(() => {
    addToastGlobal = (toast) => {
      setToasts((prev) => [...prev, toast]);
      setTimeout(() => {
        setToasts((prev) => prev.filter((item) => item.id !== toast.id));
      }, 4000);
    };
    return () => { addToastGlobal = null; };
  }, []);

  function remove(id) {
    setToasts((prev) => prev.filter((item) => item.id !== id));
  }

  const closeLabel = t?.close ?? 'Cerrar';

  return (
    <div className="toastContainer">
      {toasts.map((toast) => (
        <div key={toast.id} className={`toast ${toast.type}`} role="status">
          <span>{toast.message}</span>
          <button type="button" onClick={() => remove(toast.id)} aria-label={closeLabel}>
            <X size={16} />
          </button>
        </div>
      ))}
    </div>
  );
}
