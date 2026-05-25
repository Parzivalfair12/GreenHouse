import { Component } from 'react';

export class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="errorBoundary">
          <h2>{this.props.t?.error ?? 'Error'}</h2>
          <p>{this.props.t?.internalError ?? 'Ha ocurrido un error inesperado.'}</p>
          <button type="button" onClick={() => window.location.reload()}>
            {this.props.t?.retry ?? 'Reintentar'}
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
