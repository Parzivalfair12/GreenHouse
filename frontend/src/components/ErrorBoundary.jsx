import { Component } from 'react';

export class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught:', error, errorInfo);
    this.setState({ errorInfo });
  }

  render() {
    if (this.state.hasError) {
      const isDev = import.meta.env?.DEV;
      return (
        <div className="errorBoundary">
          <h2>{this.props.t?.error ?? 'Error de renderizado'}</h2>
          <p>{this.props.t?.internalError ?? 'Ha ocurrido un error inesperado en la interfaz.'}</p>
          {isDev && this.state.error && (
            <pre style={{ maxWidth: '80vw', overflow: 'auto', textAlign: 'left', fontSize: '0.75rem', padding: '12px', background: 'rgba(0,0,0,0.3)', borderRadius: '8px' }}>
              {this.state.error.toString()}
              {this.state.errorInfo?.componentStack}
            </pre>
          )}
          <div style={{ display: 'flex', gap: '12px' }}>
            <button type="button" onClick={() => window.location.reload()}>
              {this.props.t?.retry ?? 'Recargar pagina'}
            </button>
            <button type="button" className="secondaryButton" onClick={() => this.setState({ hasError: false, error: null, errorInfo: null })}>
              {this.props.t?.goBack ?? 'Intentar continuar'}
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
