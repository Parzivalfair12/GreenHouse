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
            token: 'jwt-token',
            email: 'admin@greenhouse.local',
            fullName: 'Administrador',
            roles: ['ROLE_ADMIN'],
            expiresIn: 86400
          })
        });
      }
      return Promise.reject(new Error('Use fallback data'));
    }));
  });

  it('renders login screen when not authenticated', async () => {
    render(<App />);
    const headings = await screen.findAllByRole('heading', { name: /acceso al sistema/i });
    expect(headings.length).toBeGreaterThanOrEqual(1);
  });

  it('logs in and shows dashboard', async () => {
    render(<App />);

    const emailInputs = await screen.findAllByPlaceholderText('admin@greenhouse.local');
    fireEvent.change(emailInputs[0], { target: { value: 'admin@greenhouse.local' } });

    const passwordInputs = await screen.findAllByPlaceholderText('admin1234');
    fireEvent.change(passwordInputs[0], { target: { value: 'admin1234' } });

    const buttons = await screen.findAllByRole('button', { name: /iniciar sesion/i });
    fireEvent.click(buttons[0]);

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /resumen general/i })).toBeInTheDocument()
    );
    await waitFor(() => expect(screen.getAllByText(/Invernadero Norte/i).length).toBeGreaterThan(0));
    expect(screen.getByText(/Temperatura por encima del umbral/i)).toBeInTheDocument();
  });

  it('shows error on invalid login', async () => {
    vi.stubGlobal('fetch', vi.fn(() =>
      Promise.reject(new Error('Invalid credentials'))
    ));

    render(<App />);

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
    render(<App />);
    const selects = screen.getAllByDisplayValue('ES');
    expect(selects.length).toBeGreaterThanOrEqual(1);
  });
});
