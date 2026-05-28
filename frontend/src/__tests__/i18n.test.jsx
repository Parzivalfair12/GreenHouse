import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { dictionary, getSavedLanguage, saveLanguage, translate } from '../i18n.js';
import { App } from '../App.jsx';

function renderApp() {
  return render(<MemoryRouter initialEntries={['/']}><App /></MemoryRouter>);
}

function clearLangStorage() {
  try { localStorage.removeItem('greenhouse-language'); } catch { /* ignore */ }
}

function mockFetch() {
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
    if (url.startsWith('/api/')) {
      return Promise.resolve({ ok: true, json: () => Promise.resolve([]) });
    }
    return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
  }));
}

describe('i18n core', () => {
  beforeEach(clearLangStorage);
  afterEach(clearLangStorage);

  it('getSavedLanguage defaults to es when no localStorage entry', () => {
    expect(getSavedLanguage()).toBe('es');
  });

  it('saveLanguage persists to localStorage', () => {
    saveLanguage('en');
    expect(getSavedLanguage()).toBe('en');
  });

  it('translate returns correct Spanish value', () => {
    expect(translate('es', 'loginTitle')).toBe(dictionary.es.loginTitle);
  });

  it('translate returns correct English value', () => {
    expect(translate('en', 'loginTitle')).toBe(dictionary.en.loginTitle);
  });

  it('translate falls back to path when key missing', () => {
    expect(translate('es', 'nonexistent.key')).toBe('nonexistent.key');
  });

  it('translate uses custom fallback when provided', () => {
    expect(translate('es', 'nonexistent.key', 'fallback')).toBe('fallback');
  });

  it('all es keys have corresponding en keys', () => {
    const esKeys = Object.keys(dictionary.es);
    const enKeys = Object.keys(dictionary.en);
    const missing = esKeys.filter((k) => !enKeys.includes(k));
    expect(missing).toEqual([]);
  });

  it('all en keys have corresponding es keys', () => {
    const esKeys = Object.keys(dictionary.es);
    const enKeys = Object.keys(dictionary.en);
    const missing = enKeys.filter((k) => !esKeys.includes(k));
    expect(missing).toEqual([]);
  });
});

describe('i18n UI integration', () => {
  beforeEach(() => {
    clearLangStorage();
    mockFetch();
    window.history.replaceState({}, '', '/');
  });

  afterEach(() => {
    clearLangStorage();
    vi.unstubAllGlobals();
  });

  it('renders login in Spanish by default', async () => {
    renderApp();
    expect(await screen.findByText(dictionary.es.loginTitle)).toBeInTheDocument();
  });

  it('renders login in English after language switch', async () => {
    renderApp();
    const selects = await screen.findAllByLabelText(/Idioma|Language/i);
    fireEvent.change(selects[0], { target: { value: 'en' } });
    await waitFor(() => {
      expect(screen.getByText(dictionary.en.loginTitle)).toBeInTheDocument();
    });
  });

  it('persists language selection in localStorage', async () => {
    renderApp();
    const selects = await screen.findAllByLabelText(/Idioma|Language/i);
    fireEvent.change(selects[0], { target: { value: 'en' } });
    await waitFor(() => expect(getSavedLanguage()).toBe('en'));
  });
});
