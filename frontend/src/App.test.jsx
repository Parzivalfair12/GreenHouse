import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { App } from './App.jsx';

describe('App', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.stubGlobal('fetch', vi.fn((url) => {
      if (url === '/api/auth/login') {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            id: 1,
            email: 'admin@greenhouse.local',
            fullName: 'Administrador',
            role: 'ADMIN',
            provider: 'email'
          })
        });
      }

      return Promise.reject(new Error('Use fallback data'));
    }));
  });

  it('renders dashboard data from fallback API data', async () => {
    render(<App />);

    expect(screen.getByRole('heading', { name: /acceso al sistema/i })).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('admin@greenhouse.local'), {
      target: { value: 'admin@greenhouse.local' }
    });
    fireEvent.change(screen.getByPlaceholderText('admin1234'), {
      target: { value: 'admin1234' }
    });
    fireEvent.click(screen.getByRole('button', { name: /iniciar sesion/i }));

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /resumen general/i })).toBeInTheDocument()
    );
    await waitFor(() => expect(screen.getAllByText(/Invernadero Norte/i).length).toBeGreaterThan(0));
    expect(screen.getByText(/Temperatura por encima del umbral/i)).toBeInTheDocument();
  });
});
