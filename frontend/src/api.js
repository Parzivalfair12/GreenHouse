const STORAGE_KEY = 'greenhouse-session';

const fallbackGreenhouses = [
  {
    id: 1,
    name: 'Invernadero Norte',
    location: 'Campus principal',
    areaSquareMeters: 120.5,
    active: true,
    cropCount: 1,
    sensorCount: 1,
    crops: [{ id: 1, name: 'Tomate chonto', variety: 'Santa Clara', status: 'GERMINATING' }],
    sensors: [{ id: 1, code: 'TEMP-001', type: 'TEMPERATURE', unit: 'C' }],
    irrigationEvents: [{ id: 1, durationMinutes: 18, waterLiters: 32.4, mode: 'AUTOMATIC' }]
  }
];

const fallbackAlerts = [
  {
    id: 1,
    severity: 'WARNING',
    message: 'Temperatura por encima del umbral',
    resolved: false,
    sensorCode: 'TEMP-001'
  }
];

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

export function setOnUnauthorized(callback) {
  onUnauthorizedCallback = callback;
}

function triggerUnauthorized() {
  clearStoredSession();
  if (onUnauthorizedCallback) {
    onUnauthorizedCallback();
  }
}

// --- Auth header builder (centralizado) ---

function authHeaders(extra = {}) {
  const session = getStoredSession();
  if (session?.token) {
    extra['Authorization'] = `Bearer ${session.token}`;
  }
  if (!extra['Accept-Language']) {
    extra['Accept-Language'] = 'es';
  }
  return extra;
}

// --- HTTP helpers (centralizados, con auth automático) ---

async function sendJson(path, method, body) {
  const response = await fetch(path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    },
    body: JSON.stringify(body)
  });
  if (response.status === 401 || response.status === 403) {
    triggerUnauthorized();
    throw new Error(`Request failed: ${response.status}`);
  }
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json();
}

async function fetchJson(path, fallback) {
  try {
    const response = await fetch(path, { headers: authHeaders() });
    if (response.status === 401 || response.status === 403) {
      triggerUnauthorized();
      throw new Error(`Session expired: ${response.status}`);
    }
    if (!response.ok) throw new Error(`Request failed: ${response.status}`);
    return response.json();
  } catch {
    return fallback;
  }
}

async function deleteJson(path) {
  const response = await fetch(path, { method: 'DELETE', headers: authHeaders() });
  if (response.status === 401 || response.status === 403) {
    triggerUnauthorized();
    throw new Error(`Session expired: ${response.status}`);
  }
  if (!response.ok) throw new Error(`Request failed: ${response.status}`);
}

// --- Public API ---

export function loginWithEmail(payload) {
  return sendJson('/api/auth/login', 'POST', payload);
}

export function beginGoogleOAuth() {
  window.location.assign('http://localhost:8080/oauth2/authorization/google');
}

export async function fetchCurrentOAuthUser() {
  const response = await fetch('/api/auth/me', {
    credentials: 'include',
    headers: authHeaders()
  });
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json();
}

export function fetchGreenhouses() {
  return fetchJson('/api/greenhouses', fallbackGreenhouses);
}

export function fetchDashboard() {
  return fetchJson('/api/dashboard', null);
}

export function fetchAlerts() {
  return fetchJson('/api/alerts/open', fallbackAlerts);
}

export function fetchUsers() {
  return fetchJson('/api/users', [
    { id: 1, email: 'admin@greenhouse.local', fullName: 'Administrador', role: 'ADMIN', provider: 'email' }
  ]);
}

export function fetchZones() {
  return fetchJson('/api/zones', []);
}

export function fetchSensors() {
  return fetchJson('/api/sensors', []);
}

export function fetchReadings() {
  return fetchJson('/api/readings', []);
}

export function fetchActuators() {
  return fetchJson('/api/actuators', []);
}

export function fetchRules() {
  return fetchJson('/api/rules', []);
}

export function fetchAuditLogs() {
  return fetchJson('/api/audit-logs', []);
}

export function createGreenhouse(payload) {
  return sendJson('/api/greenhouses', 'POST', payload);
}

export function updateGreenhouse(greenhouseId, payload) {
  return sendJson(`/api/greenhouses/${greenhouseId}`, 'PUT', payload);
}

export function deleteGreenhouse(greenhouseId) {
  return updateGreenhouse(greenhouseId, { active: false, name: 'Invernadero desactivado', location: 'Sin uso', areaSquareMeters: 1 });
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
  const response = await fetch(`/api/alerts/${alertId}/resolve`, {
    method: 'PATCH',
    headers: authHeaders()
  });
  if (response.status === 401 || response.status === 403) {
    triggerUnauthorized();
    throw new Error(`Session expired: ${response.status}`);
  }
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json();
}
