import { getSavedLanguage } from './i18n.js';

const STORAGE_KEY = 'greenhouse-session';

// --- Session management (centralizado) ---

export function getStoredSession() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    return saved ? JSON.parse(saved) : null;
  } catch {
    return null;
  }
}

export function clearStoredSession() {
  localStorage.removeItem(STORAGE_KEY);
}

let onUnauthorizedCallback = null;
let onUnverifiedCallback = null;
let refreshAttempted = false;

export function setOnUnauthorized(callback) {
  onUnauthorizedCallback = callback;
}

export function setOnUnverified(callback) {
  onUnverifiedCallback = callback;
}

function triggerUnauthorized() {
  clearStoredSession();
  if (onUnauthorizedCallback) {
    onUnauthorizedCallback();
  }
}

function triggerUnverified() {
  if (onUnverifiedCallback) {
    onUnverifiedCallback();
  }
}

async function tryRefreshToken() {
  if (refreshAttempted) return false;
  refreshAttempted = true;
  try {
    const session = getStoredSession();
    const headers = { 'Content-Type': 'application/json' };
    // Send Bearer token if available, otherwise rely on httpOnly cookie
    if (session?.token) {
      headers['Authorization'] = `Bearer ${session.token}`;
    }
    const response = await fetch(getApiUrl('/api/auth/refresh'), {
      method: 'POST',
      headers,
      credentials: 'include'
    });
    if (!response.ok) return false;
    const data = await response.json();
    if (data.token) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        ...(session || {}),
        token: data.token,
        verified: data.verified ?? session?.verified
      }));
      refreshAttempted = false;
      return true;
    }
    return false;
  } catch {
    return false;
  } finally {
    setTimeout(() => { refreshAttempted = false; }, 10000);
  }
}

export function resetRefreshAttempt() {
  refreshAttempted = false;
}

// --- Auth header builder (centralizado) ---

function authHeaders(extra = {}) {
  const session = getStoredSession();
  if (session?.token) {
    extra['Authorization'] = `Bearer ${session.token}`;
  }
  if (!extra['Accept-Language']) {
    extra['Accept-Language'] = getSavedLanguage();
  }
  return extra;
}

async function parseError(response) {
  try {
    const data = await response.json();
    if (data.message) return data.message;
    if (data.error) return data.error;
    if (Array.isArray(data.errors)) return data.errors.map(e => e.defaultMessage || e).join(', ');
    return JSON.stringify(data);
  } catch {
    return `HTTP ${response.status}`;
  }
}

async function handleAuthErrors(response, shouldRefresh = true) {
  const status = response.status;

  // 401: try refresh once, then logout if still 401
  if (status === 401 && shouldRefresh) {
    const refreshed = await tryRefreshToken();
    if (refreshed) {
      return { retry: true };
    }
    triggerUnauthorized();
    const msg = await parseError(response);
    throw new Error(msg || `Session expired: ${status}`);
  }

  // 403: forbidden (authenticated but no permission) — NEVER logout
  if (status === 403) {
    const msg = await parseError(response);
    if (msg.toLowerCase().includes('verif')) {
      triggerUnverified();
    }
    throw new Error(msg || `Access denied: ${status}`);
  }

  // 429: rate limited
  if (status === 429) {
    const msg = await parseError(response);
    throw new Error(msg || 'Too many requests. Please try again later.');
  }

  return null;
}

// --- HTTP helpers (centralizados, con auth automatico) ---

async function sendJson(path, method, body) {
  const url = getApiUrl(path);
  let response = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    },
    credentials: 'include',
    body: JSON.stringify(body)
  });
  const authResult = await handleAuthErrors(response);
  if (authResult?.retry) {
    response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...authHeaders()
      },
      credentials: 'include',
      body: JSON.stringify(body)
    });
    const retryResult = await handleAuthErrors(response, false);
    if (retryResult?.retry) return; // should not happen
  }
  if (!response.ok) {
    const errText = await parseError(response);
    throw new Error(errText || `Request failed: ${response.status}`);
  }
  return response.json();
}

async function fetchJson(path) {
  const url = getApiUrl(path);
  let response = await fetch(url, { headers: authHeaders(), credentials: 'include' });
  const authResult = await handleAuthErrors(response);
  if (authResult?.retry) {
    response = await fetch(url, { headers: authHeaders(), credentials: 'include' });
    const retryResult = await handleAuthErrors(response, false);
    if (retryResult?.retry) return;
  }
  if (!response.ok) {
    const errText = await parseError(response);
    throw new Error(errText || `Request failed: ${response.status}`);
  }
  return response.json();
}

async function deleteJson(path) {
  const url = getApiUrl(path);
  let response = await fetch(url, { method: 'DELETE', headers: authHeaders(), credentials: 'include' });
  const authResult = await handleAuthErrors(response);
  if (authResult?.retry) {
    response = await fetch(url, { method: 'DELETE', headers: authHeaders(), credentials: 'include' });
    const retryResult = await handleAuthErrors(response, false);
    if (retryResult?.retry) return;
  }
  if (!response.ok) {
    const errText = await parseError(response);
    throw new Error(errText || `Request failed: ${response.status}`);
  }
}

// --- Public API ---

export function loginWithEmail(payload) {
  return sendJson('/api/auth/login', 'POST', payload);
}

const API_BASE = import.meta.env.VITE_API_URL ?? '';

export function getApiUrl(path) {
  return `${API_BASE}${path}`;
}

export function beginGoogleOAuth() {
  // Navigate from the frontend's own domain so the OAuth callback
  // returns to the same origin. This ensures the JWT cookie is set
  // for the frontend domain and is sent on subsequent API calls.
  // nginx or Vite proxy forwards /oauth2/ to the backend.
  const base = API_BASE || window.location.origin || 'http://localhost:5173';
  const redirectUrl = `${base.replace(/\/+$/, '')}/oauth2/authorization/google`;
  window.location.assign(redirectUrl);
}

export function refreshToken() {
  return sendJson('/api/auth/refresh', 'POST', {});
}

export function forgotPassword(email) {
  return sendJson('/api/auth/forgot-password', 'POST', { email });
}

export function resetPassword(token, password) {
  return sendJson('/api/auth/reset-password', 'POST', { token, password });
}

export function verifyEmail(token) {
  return sendJson('/api/auth/verify', 'POST', { token });
}

export function verifyEmailGet(token) {
  return fetch(getApiUrl(`/api/auth/verify-email?token=${encodeURIComponent(token)}`));
}

export function resendVerification() {
  return sendJson('/api/auth/resend-verification', 'POST', {});
}

export async function fetchCurrentOAuthUser() {
  const response = await fetch(getApiUrl('/api/auth/me'), {
    credentials: 'include',
    headers: authHeaders()
  });
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json();
}

export function fetchGreenhouses() {
  return fetchJson('/api/greenhouses');
}

export function fetchDashboard() {
  return fetchJson('/api/dashboard');
}

export function fetchAlerts() {
  return fetchJson('/api/alerts/open');
}

export function fetchUsers() {
  return fetchJson('/api/users');
}

export function fetchZones() {
  return fetchJson('/api/zones');
}

export function fetchSensors() {
  return fetchJson('/api/sensors');
}

export function fetchReadings() {
  return fetchJson('/api/readings');
}

export function fetchActuators() {
  return fetchJson('/api/actuators');
}

export function fetchRules() {
  return fetchJson('/api/rules');
}

export function fetchAuditLogs() {
  return fetchJson('/api/audit-logs');
}

export function createGreenhouse(payload) {
  return sendJson('/api/greenhouses', 'POST', payload);
}

export function updateGreenhouse(greenhouseId, payload) {
  return sendJson(`/api/greenhouses/${greenhouseId}`, 'PUT', payload);
}

export function deleteGreenhouse(greenhouseId) {
  return deleteJson(`/api/greenhouses/${greenhouseId}`);
}

export function addCrop(greenhouseId, payload) {
  return sendJson(`/api/greenhouses/${greenhouseId}/crops`, 'POST', payload);
}

export function updateCrop(greenhouseId, cropId, payload) {
  return sendJson(`/api/greenhouses/${greenhouseId}/crops/${cropId}`, 'PUT', payload);
}

export function addSensor(greenhouseId, payload) {
  return sendJson(`/api/greenhouses/${greenhouseId}/sensors`, 'POST', payload);
}

export function updateSensor(greenhouseId, sensorId, payload) {
  return sendJson(`/api/greenhouses/${greenhouseId}/sensors/${sensorId}`, 'PUT', payload);
}

export function updateSensorRecord(sensorId, payload) {
  return sendJson(`/api/sensors/${sensorId}`, 'PUT', payload);
}

export function deleteSensor(sensorId) {
  return deleteJson(`/api/sensors/${sensorId}`);
}

export function addIrrigation(greenhouseId, payload) {
  return sendJson(`/api/greenhouses/${greenhouseId}/irrigation-events`, 'POST', payload);
}

export function updateIrrigation(greenhouseId, eventId, payload) {
  return sendJson(`/api/greenhouses/${greenhouseId}/irrigation-events/${eventId}`, 'PUT', payload);
}

export function createZone(payload) {
  return sendJson('/api/zones', 'POST', payload);
}

export function updateZone(zoneId, payload) {
  return sendJson(`/api/zones/${zoneId}`, 'PUT', payload);
}

export function deleteZone(zoneId) {
  return deleteJson(`/api/zones/${zoneId}`);
}

export function createReading(payload) {
  return sendJson('/api/readings', 'POST', payload);
}

export function updateReading(readingId, payload) {
  return sendJson(`/api/readings/${readingId}`, 'PUT', payload);
}

export function deleteReading(readingId) {
  return deleteJson(`/api/readings/${readingId}`);
}

export function createActuator(payload) {
  return sendJson('/api/actuators', 'POST', payload);
}

export function updateActuator(actuatorId, payload) {
  return sendJson(`/api/actuators/${actuatorId}`, 'PUT', payload);
}

export function deleteActuator(actuatorId) {
  return deleteJson(`/api/actuators/${actuatorId}`);
}

export function createRule(payload) {
  return sendJson('/api/rules', 'POST', payload);
}

export function updateRule(ruleId, payload) {
  return sendJson(`/api/rules/${ruleId}`, 'PUT', payload);
}

export function deleteRule(ruleId) {
  return deleteJson(`/api/rules/${ruleId}`);
}

export function createUser(payload) {
  return sendJson('/api/users', 'POST', payload);
}

export function updateUserRole(userId, role) {
  return sendJson(`/api/users/${userId}/role`, 'PATCH', { role });
}

export async function resolveAlert(alertId) {
  const url = getApiUrl(`/api/alerts/${alertId}/resolve`);
  let response = await fetch(url, {
    method: 'PATCH',
    headers: authHeaders(),
    credentials: 'include'
  });
  const authResult = await handleAuthErrors(response);
  if (authResult?.retry) {
    response = await fetch(url, {
      method: 'PATCH',
      headers: authHeaders(),
      credentials: 'include'
    });
    await handleAuthErrors(response, false);
  }
  if (!response.ok) {
    const errText = await parseError(response);
    throw new Error(errText || `Request failed: ${response.status}`);
  }
  return response.json();
}

// --- IA endpoints ---

export async function fetchAiPrediction() {
  return fetchJson('/api/ai/prediction');
}

export async function fetchLogs() {
  return fetchJson('/api/audit-logs');
}

export async function fetchIaHealth() {
  const response = await fetch(getApiUrl('/api/ia/health'), {
    headers: authHeaders(),
    credentials: 'include'
  });
  if (!response.ok) throw new Error(`IA health failed: ${response.status}`);
  return response.json();
}

export async function fetchIaPrediction(temperatures, humidities) {
  const response = await fetch(getApiUrl('/api/ia/predict'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    credentials: 'include',
    body: JSON.stringify({ temperature: temperatures, humidity: humidities })
  });
  if (!response.ok) throw new Error(`IA predict failed: ${response.status}`);
  return response.json();
}

export async function fetchIaRecommendation(tempPred, humPred, riskLevel) {
  const response = await fetch(getApiUrl('/api/ia/recommend'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    credentials: 'include',
    body: JSON.stringify({ predictedTemperature: tempPred, predictedHumidity: humPred, riskLevel })
  });
  if (!response.ok) throw new Error(`IA recommend failed: ${response.status}`);
  return response.json();
}

// --- Taiga endpoints ---

export async function fetchTaigaStories() {
  const response = await fetch(getApiUrl('/api/taiga/stories'), {
    headers: authHeaders(),
    credentials: 'include'
  });
  if (!response.ok) throw new Error(`Taiga stories failed: ${response.status}`);
  return response.json();
}

export async function fetchTaigaSummary() {
  const response = await fetch(getApiUrl('/api/taiga/summary'), {
    headers: authHeaders(),
    credentials: 'include'
  });
  if (!response.ok) throw new Error(`Taiga summary failed: ${response.status}`);
  return response.json();
}

// --- Simulator control ---

export async function startSimulator() {
  return sendJson('/api/simulator/start', 'POST', {});
}

export async function stopSimulator() {
  return sendJson('/api/simulator/stop', 'POST', {});
}

export async function fetchSimulatorStatus() {
  return fetchJson('/api/simulator/status');
}

