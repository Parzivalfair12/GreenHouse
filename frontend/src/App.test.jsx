import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { App } from './App.jsx';

function renderApp() {
  return render(<MemoryRouter initialEntries={['/']}><App /></MemoryRouter>);
}

describe('App', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.stubGlobal('fetch', vi.fn((url) => {
      if (url === '/api/auth/login') {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            token: 'jwt-token',
            email: 'admin@greenhouse.local',
            fullName: 'Administrador',
            roles: ['ROLE_ADMIN'],
            expiresIn: 86400
          })
        });
      }
      // Return empty arrays for all dashboard refresh endpoints
      if (url.startsWith('/api/')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve([])
        });
      }
      return Promise.reject(new Error('Use fallback data'));
    }));
  });

  it('renders login screen when not authenticated', async () => {
    renderApp();
    const headings = await screen.findAllByRole('heading', { name: /acceso al sistema/i });
    expect(headings.length).toBeGreaterThanOrEqual(1);
  });

  it('logs in and shows dashboard', async () => {
    renderApp();

    const emailInputs = await screen.findAllByPlaceholderText('admin@greenhouse.local');
    fireEvent.change(emailInputs[0], { target: { value: 'admin@greenhouse.local' } });

    const passwordInputs = await screen.findAllByPlaceholderText('admin1234');
    fireEvent.change(passwordInputs[0], { target: { value: 'admin1234' } });

    const buttons = await screen.findAllByRole('button', { name: /iniciar sesion/i });
    fireEvent.click(buttons[0]);

    // After login, the authenticated shell should appear (sidebar with navigation)
    await waitFor(() =>
      expect(screen.getByRole('navigation', { name: /Secciones principales/i })).toBeInTheDocument()
    );

    // Dashboard section heading should render
    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /resumen general/i })).toBeInTheDocument()
    );

    // Simulator panel should be visible (part of IoT feature)
    expect(screen.getByText(/Simulador IoT/i)).toBeInTheDocument();
  });

  it('shows error on invalid login', async () => {
    vi.stubGlobal('fetch', vi.fn(() =>
      Promise.reject(new Error('Invalid credentials'))
    ));

    renderApp();

    const emailInputs = await screen.findAllByPlaceholderText('admin@greenhouse.local');
    fireEvent.change(emailInputs[0], { target: { value: 'wrong@email.com' } });

    const passwordInputs = await screen.findAllByPlaceholderText('admin1234');
    fireEvent.change(passwordInputs[0], { target: { value: 'wrongpass' } });

    const buttons = await screen.findAllByRole('button', { name: /iniciar sesion/i });
    fireEvent.click(buttons[0]);

    await waitFor(() =>
      expect(screen.getByText(/incorrectos/i)).toBeInTheDocument()
    );
  });

  it('shows language selector on login screen', () => {
    renderApp();
    const selects = screen.getAllByDisplayValue('ES');
    expect(selects.length).toBeGreaterThanOrEqual(1);
  });
});
