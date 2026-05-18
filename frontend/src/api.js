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

export async function fetchGreenhouses() {
  try {
    const response = await fetch('/api/greenhouses', { headers: { 'Accept-Language': 'es' } });
    if (!response.ok) throw new Error('API unavailable');
    return response.json();
  } catch {
    return fallbackGreenhouses;
  }
}

export async function fetchDashboard() {
  try {
    const response = await fetch('/api/dashboard', { headers: { 'Accept-Language': 'es' } });
    if (!response.ok) throw new Error('API unavailable');
    return response.json();
  } catch {
    return null;
  }
}

export function loginWithEmail(payload) {
  return sendJson('/api/auth/login', 'POST', payload);
}

export function beginGoogleOAuth() {
  window.location.assign('http://localhost:8080/oauth2/authorization/google');
}

export async function fetchCurrentOAuthUser() {
  const response = await fetch('/api/auth/me', {
    credentials: 'include',
    headers: { 'Accept-Language': 'es' }
  });
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json();
}

export async function fetchAlerts() {
  try {
    const response = await fetch('/api/alerts/open', { headers: { 'Accept-Language': 'es' } });
    if (!response.ok) throw new Error('API unavailable');
    return response.json();
  } catch {
    return fallbackAlerts;
  }
}

export async function fetchUsers() {
  try {
    const response = await fetch('/api/users', { headers: { 'Accept-Language': 'es' } });
    if (!response.ok) throw new Error('API unavailable');
    return response.json();
  } catch {
    return [
      { id: 1, email: 'admin@greenhouse.local', fullName: 'Administrador', role: 'ADMIN', provider: 'email' }
    ];
  }
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

async function sendJson(path, method, body) {
  const response = await fetch(path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Accept-Language': 'es'
    },
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json();
}

async function fetchJson(path, fallback) {
  try {
    const response = await fetch(path, { headers: { 'Accept-Language': 'es' } });
    if (!response.ok) throw new Error(`Request failed: ${response.status}`);
    return response.json();
  } catch {
    return fallback;
  }
}

async function deleteJson(path) {
  const response = await fetch(path, { method: 'DELETE', headers: { 'Accept-Language': 'es' } });
  if (!response.ok) throw new Error(`Request failed: ${response.status}`);
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
    headers: { 'Accept-Language': 'es' }
  });
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json();
}
