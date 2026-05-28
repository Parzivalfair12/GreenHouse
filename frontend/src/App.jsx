import { useEffect, useMemo, useState } from 'react';
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import { Activity, Bell, BookOpen, CircleHelp, Cpu, Database, LayoutDashboard, ListChecks, Map as MapIcon, Moon, Radio, Share2, Sun, Users, Warehouse } from 'lucide-react';
import { ToastContainer, showToast } from './components/Toast.jsx';
import { ErrorBoundary } from './components/ErrorBoundary.jsx';
import { AppHeader, SidebarBrand } from './components/AppHeader.jsx';
import { ERDViewer } from './pages/ERD/ERDViewer.jsx';

import {
  addCrop,
  addIrrigation,
  addSensor,
  beginGoogleOAuth,
  createGreenhouse,
  deleteGreenhouse,
  fetchAlerts,
  fetchCurrentOAuthUser,
  fetchGreenhouses,
  fetchUsers,
  loginWithEmail,
  resolveAlert,
  createUser,
  updateUserRole,
  updateGreenhouse,
  updateCrop,
  updateSensor,
  updateIrrigation,
  fetchDashboard,
  fetchZones,
  createZone,
  updateZone,
  deleteZone,
  fetchSensors,
  updateSensorRecord,
  deleteSensor,
  fetchReadings,
  createReading,
  updateReading,
  deleteReading,
  fetchActuators,
  createActuator,
  updateActuator,
  deleteActuator,
  fetchRules,
  createRule,
  updateRule,
  deleteRule,
  fetchAuditLogs,
  fetchSimulatorStatus,
  startSimulator,
  stopSimulator,
  setOnUnauthorized,
  setOnUnverified,
  resendVerification,
  clearStoredSession,
  getStoredSession,
  getApiUrl,
} from './api.js';
import { AlertsSection } from './components/AlertsSection.jsx';
import { ArchitectureSection } from './components/ArchitectureSection.jsx';
import { CrudSection } from './components/CrudSection.jsx';
import { DashboardSection } from './components/DashboardSection.jsx';
import { DataDictionarySection } from './components/DataDictionarySection.jsx';
import { DataSection } from './components/DataSection.jsx';
import { IaSection } from './components/IaSection.jsx';
import { LogsSection } from './components/LogsSection.jsx';
import { TaigaSection } from './components/TaigaSection.jsx';
import { GreenhousesSection } from './components/GreenhousesSection.jsx';
import { LoginScreen } from './components/LoginScreen.jsx';
import { ManualSection } from './components/ManualSection.jsx';
import { Navbar } from './components/Navbar.jsx';
import { OperationsSection } from './components/OperationsSection.jsx';
import { UsersSection } from './components/UsersSection.jsx';
import { dictionary, getSavedLanguage, saveLanguage } from './i18n.js';
import './styles.css';

const emptyGreenhouse = { name: '', location: '', areaSquareMeters: 40, active: true };
const emptyCrop = { name: '', variety: '', plantedAt: '', expectedHarvestAt: '' };
const emptySensor = { code: '', type: 'TEMPERATURE', unit: 'C', minThreshold: 18, maxThreshold: 30 };
const emptyIrrigation = { durationMinutes: 10, waterLiters: 20, mode: 'MANUAL' };
const emptyUser = { fullName: '', email: '', password: '', role: 'VIEWER' };

export function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const [theme, setTheme] = useState(() => localStorage.getItem('greenhouse-theme') ?? 'dark');
  const [language, setLanguage] = useState(getSavedLanguage);
  const [session, setSession] = useStoredSession();
  const [activeSection, setActiveSection] = useState('dashboard');

  const isERDPath = location.pathname.startsWith('/erd');

  useEffect(() => {
    if (location.pathname === '/erd') setActiveSection('erd');
  }, [location.pathname]);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('greenhouse-theme', theme);
  }, [theme]);

  const [greenhouses, setGreenhouses] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [users, setUsers] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [zones, setZones] = useState([]);
  const [allSensors, setAllSensors] = useState([]);
  const [readings, setReadings] = useState([]);
  const [actuators, setActuators] = useState([]);
  const [rules, setRules] = useState([]);
  const [logs, setLogs] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [greenhouseForm, setGreenhouseForm] = useState(emptyGreenhouse);
  const [greenhouseEditForm, setGreenhouseEditForm] = useState(emptyGreenhouse);
  const [cropForm, setCropForm] = useState(emptyCrop);
  const [sensorForm, setSensorForm] = useState(emptySensor);
  const [irrigationForm, setIrrigationForm] = useState(emptyIrrigation);
  const [userForm, setUserForm] = useState(emptyUser);
  const [loginError, setLoginError] = useState('');
  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState(null);
  const [simulatorRunning, setSimulatorRunning] = useState(false);
  const [unverifiedBanner, setUnverifiedBanner] = useState(false);
  const t = dictionary[language];

  function toggleTheme() {
    setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
  }

  function handleSetLanguage(lang) {
    setLanguage(lang);
    saveLanguage(lang);
  }

  async function refresh() {
    setApiError(null);
    try {
      const results = await Promise.allSettled([
        fetchGreenhouses(),
        fetchAlerts(),
        fetchUsers(),
        fetchDashboard(),
        fetchZones(),
        fetchSensors(),
        fetchReadings(),
        fetchActuators(),
        fetchRules(),
        fetchAuditLogs()
      ]);

      const [greenhouseResult, alertResult, userResult, dashboardResult,
        zoneResult, sensorResult, readingResult, actuatorResult, ruleResult, logResult] = results;

      if (greenhouseResult.status === 'fulfilled') setGreenhouses(greenhouseResult.value);
      if (alertResult.status === 'fulfilled') setAlerts(alertResult.value);
      if (userResult.status === 'fulfilled') setUsers(userResult.value);
      if (dashboardResult.status === 'fulfilled') setDashboard(dashboardResult.value);
      if (zoneResult.status === 'fulfilled') setZones(zoneResult.value);
      if (sensorResult.status === 'fulfilled') setAllSensors(sensorResult.value);
      if (readingResult.status === 'fulfilled') setReadings(readingResult.value);
      if (actuatorResult.status === 'fulfilled') setActuators(actuatorResult.value);
      if (ruleResult.status === 'fulfilled') setRules(ruleResult.value);
      if (logResult.status === 'fulfilled') setLogs(logResult.value);

      // Use greenhouse data to select default greenhouse
      if (greenhouseResult.status === 'fulfilled') {
        setSelectedId((current) => {
          if (greenhouseResult.value.some((greenhouse) => greenhouse.id === current)) return current;
          return pickDefaultGreenhouseId(greenhouseResult.value,
            sensorResult.status === 'fulfilled' ? sensorResult.value : [],
            readingResult.status === 'fulfilled' ? readingResult.value : []);
        });
      }
    } catch (err) {
      // Only set apiError for session/auth failures (401), not permission errors (403)
      if (err.message?.includes('401') || err.message?.includes('Session expired')) {
        setApiError(err.message || t.loadError);
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('oauth') === 'google') return;
    if (session) {
      refresh();
      fetchSimulatorStatus().then((s) => setSimulatorRunning(s.running)).catch(() => {});
    }
  }, [session]);

  useEffect(() => {
    if (!session || apiError) return;
    const params = new URLSearchParams(window.location.search);
    if (params.get('oauth') === 'google') return;
    const interval = setInterval(() => {
      refresh();
    }, 5000);
    return () => clearInterval(interval);
  }, [session, apiError]);

  useEffect(() => {
    setOnUnauthorized(() => {
      clearStoredSession();
      setSession(null);
    });
    setOnUnverified(() => {
      setUnverifiedBanner(true);
    });
    return () => {
      setOnUnauthorized(null);
      setOnUnverified(null);
    };
  }, []);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('oauth') !== 'google') return;

    if (params.get('status') === 'success') {
      fetchCurrentOAuthUser()
        .then(async (user) => {
          try {
            const tokenResponse = await fetch(getApiUrl('/api/auth/refresh'), {
              method: 'POST',
              credentials: 'include',
              headers: { 'Content-Type': 'application/json' }
            });
            if (tokenResponse.ok) {
              const tokenData = await tokenResponse.json();
              user.token = tokenData.token;
              user.verified = tokenData.verified;
            }
          } catch {
            // Token refresh failed; user can still use cookie auth
          }
          saveSession(user);
          clearOAuthQuery();
        })
        .catch(() => {
          setLoginError(t.oauthError);
          clearOAuthQuery();
        });
    } else if (params.get('status') === 'error') {
      setLoginError(params.get('message') ?? t.oauthError);
      clearOAuthQuery();
    } else {
      clearOAuthQuery();
    }
  }, []);

  const selected = greenhouses.find((greenhouse) => greenhouse.id === selectedId) ?? greenhouses[0];
  const totals = useMemo(() => ({
    greenhouses: greenhouses.length,
    crops: greenhouses.reduce((sum, greenhouse) => sum + (greenhouse.cropCount ?? 0), 0),
    sensors: greenhouses.reduce((sum, greenhouse) => sum + (greenhouse.sensorCount ?? 0), 0),
    alerts: alerts.length,
    irrigation: greenhouses.reduce((sum, greenhouse) => sum + (greenhouse.irrigationEvents?.length ?? 0), 0)
  }), [greenhouses, alerts]);

  useEffect(() => {
    if (!selected) return;
    setGreenhouseEditForm({
      name: selected.name,
      location: selected.location,
      areaSquareMeters: selected.areaSquareMeters,
      active: selected.active
    });
  }, [selected?.id, selected?.name, selected?.location, selected?.areaSquareMeters, selected?.active]);

  const sections = [
    { id: 'dashboard', label: t.dashboard, icon: LayoutDashboard },
    { id: 'greenhouses', label: t.greenhouses, icon: Warehouse },
    { id: 'architecture', label: t.architecture, icon: Activity },
    { id: 'dictionary', label: t.dataDictionaryTitle, icon: Database },
    { id: 'zones', label: t.zones, icon: MapIcon },
    { id: 'sensors', label: t.sensors, icon: Radio },
    { id: 'readings', label: t.readings, icon: Activity },
    { id: 'actuators', label: t.actuators, icon: Cpu },
    { id: 'rules', label: t.rules, icon: ListChecks },
    { id: 'operations', label: t.operations, icon: Activity },
    { id: 'alerts', label: t.alerts, icon: Bell },
    { id: 'ia', label: t.iaNav || 'IA', icon: Cpu },
    { id: 'taiga', label: t.taigaNav || 'Taiga', icon: ListChecks },
    { id: 'logs', label: t.auditLog, icon: BookOpen },
    { id: 'users', label: t.users, icon: Users },
    { id: 'data', label: t.data, icon: Database },
    { id: 'manual', label: t.manual, icon: CircleHelp },
    { id: 'erd', label: t.erd, icon: Share2, action: () => navigate('/erd') }
  ];

  useEffect(() => {
    if (location.pathname.startsWith('/erd')) {
      document.documentElement.setAttribute('data-theme', theme);
    }
  }, [location.pathname, theme]);

  async function handleLogin(credentials) {
    try {
      setLoginError('');
      setUnverifiedBanner(false);
      const user = await loginWithEmail(credentials);
      saveSession(user);
    } catch (err) {
      if (err.message?.toLowerCase().includes('verif')) {
        setLoginError(t.accountNotVerified);
        setUnverifiedBanner(true);
      } else if (err.message?.toLowerCase().includes('too many requests') || err.message?.toLowerCase().includes('demasiados')) {
        setLoginError(err.message);
      } else {
        setLoginError(t.invalidLogin);
      }
    }
  }

  async function handleResendVerification() {
    try {
      await resendVerification();
      setLoginError(t.resetSent || 'Correo enviado');
    } catch (err) {
      setLoginError(err.message || 'Error al reenviar');
    }
  }

  async function handleGoogleLogin() {
    setLoginError('');
    beginGoogleOAuth();
  }

  function saveSession(user) {
    localStorage.setItem('greenhouse-session', JSON.stringify(user));
    setSession(user);
    setUnverifiedBanner(!user.verified && user.provider === 'email');
  }

  function handleLogout() {
    clearStoredSession();
    setSession(null);
  }

  async function handleCreateGreenhouse(event) {
    event.preventDefault();
    const created = await createGreenhouse({
      ...greenhouseForm,
      areaSquareMeters: Number(greenhouseForm.areaSquareMeters)
    });
      showToast(`${t.greenhouseCreated}: ${created.name}`);
    setGreenhouseForm(emptyGreenhouse);
    await refresh();
    setSelectedId(created.id);
    setActiveSection('operations');
  }

  async function handleUpdateGreenhouse(event) {
    event.preventDefault();
    if (!selected) return;
    const updated = await updateGreenhouse(selected.id, {
      ...greenhouseEditForm,
      areaSquareMeters: Number(greenhouseEditForm.areaSquareMeters)
    });
      showToast(`${t.greenhouseUpdated}: ${updated.name}`);
    await refresh();
  }

  async function handleDeleteGreenhouse() {
    if (!selected) return;
    await deleteGreenhouse(selected.id);
      showToast(`${t.greenhouseDeleted}: ${selected.name}`);
    await refresh();
  }

  function handleSelectGreenhouse(id) {
    const greenhouse = greenhouses.find((item) => item.id === id);
    setSelectedId(id);
    if (greenhouse) {
      setGreenhouseEditForm({
        name: greenhouse.name,
        location: greenhouse.location,
        areaSquareMeters: greenhouse.areaSquareMeters,
        active: greenhouse.active
      });
    }
  }

  async function handleAddCrop(event) {
    event.preventDefault();
    if (!selected) return;
    await addCrop(selected.id, cropForm);
      showToast(t.cropAdded);
    setCropForm(emptyCrop);
    await refresh();
  }

  async function handleUpdateCrop(cropId, payload) {
    if (!selected) return;
    await updateCrop(selected.id, cropId, payload);
      showToast(t.cropUpdated);
      setCropForm(emptyCrop);
      await refresh();
    }

    async function handleAddSensor(event) {
      event.preventDefault();
      if (!selected) return;
      await addSensor(selected.id, {
        ...sensorForm,
        minThreshold: Number(sensorForm.minThreshold),
        maxThreshold: Number(sensorForm.maxThreshold)
      });
      showToast(t.sensorAdded);
      setSensorForm(emptySensor);
      await refresh();
    }

    async function handleUpdateSensor(sensorId, payload) {
      if (!selected) return;
      await updateSensor(selected.id, sensorId, {
        ...payload,
        minThreshold: Number(payload.minThreshold),
        maxThreshold: Number(payload.maxThreshold)
      });
      showToast(t.sensorUpdated);
      await refresh();
    }

    async function handleAddIrrigation(event) {
      event.preventDefault();
      if (!selected) return;
      await addIrrigation(selected.id, {
        ...irrigationForm,
        durationMinutes: Number(irrigationForm.durationMinutes),
        waterLiters: Number(irrigationForm.waterLiters)
      });
      showToast(t.irrigationRegistered);
      setIrrigationForm(emptyIrrigation);
      await refresh();
    }

    async function handleUpdateIrrigation(eventId, payload) {
      if (!selected) return;
      await updateIrrigation(selected.id, eventId, {
        ...payload,
        durationMinutes: Number(payload.durationMinutes),
        waterLiters: Number(payload.waterLiters)
      });
      showToast(t.irrigationUpdated);
      await refresh();
    }

    async function handleResolveAlert(alertId) {
      await resolveAlert(alertId);
      showToast(t.alertResolved);
      await refresh();
    }

    async function handleCreateUser(event) {
      event.preventDefault();
      const created = await createUser(userForm);
      showToast(`${t.userCreatedMsg}: ${created.email}`);
      setUserForm(emptyUser);
      await refresh();
    }

    async function handleChangeRole(userId, role) {
      const updated = await updateUserRole(userId, role);
      showToast(`${t.roleUpdatedMsg}: ${updated.email}`);
      await refresh();
    }

  function exportJson() {
    const payload = JSON.stringify({ greenhouses, alerts }, null, 2);
    const blob = new Blob([payload], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = t.exportFilename;
    link.click();
    URL.revokeObjectURL(url);
  }

  if (!session) {
    return (
      <div className={`theme-${theme}`}>
        <LoginScreen
          language={language}
          setLanguage={handleSetLanguage}
          t={t}
          onLogin={handleLogin}
          onGoogleLogin={handleGoogleLogin}
          onResendVerification={handleResendVerification}
          error={loginError}
          unverified={unverifiedBanner}
        />
      </div>
    );
  }

  if (isERDPath) {
    return (
      <div className={`theme-${theme}`}>
        <Routes>
          <Route path="/erd" element={<ERDViewer t={t} />} />
        </Routes>
      </div>
    );
  }

  return (
    <ErrorBoundary t={t}>
    <main className={`appShell theme-${theme}`}>
      <aside className="sidebar">
        <SidebarBrand t={t} />
        <Navbar sections={sections} activeSection={activeSection} onChange={setActiveSection} alertsCount={alerts.length} t={t} />
        <div className="sidebarFooter">
          <button className="themeToggle" type="button" onClick={toggleTheme} aria-label={t.changeTheme}>
            {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
          </button>
          <span className="versionBadge">Version 2.1.0</span>
        </div>
      </aside>
      <section className="mainArea">
        <AppHeader language={language} setLanguage={handleSetLanguage} session={session} t={t} onLogout={handleLogout} theme={theme} onToggleTheme={toggleTheme} />
        <ToastContainer t={t} />
        {session && !session.verified && session.provider === 'email' && (
          <div className="verificationBanner" style={{ background: '#b45309', color: '#fff', padding: '10px 16px', textAlign: 'center', fontSize: '14px' }}>
            {t.accountNotVerified}{' '}
            <button type="button" onClick={handleResendVerification} style={{ background: 'transparent', border: '1px solid #fff', color: '#fff', borderRadius: '4px', padding: '2px 8px', cursor: 'pointer', marginLeft: '8px' }}>
              {t.resendVerification}
            </button>
          </div>
        )}

      {loading && (
        <div className="loadingOverlay">
          <div className="loadingSpinner large" />
          <p>{t.loading}</p>
        </div>
      )}

      {!loading && apiError && (
        <div className="errorOverlay">
          <p className="errorText">{apiError}</p>
          <button className="btnPrimary" onClick={refresh}>{t.retry}</button>
        </div>
      )}

      {!loading && !apiError && (
        <>
      {activeSection === 'dashboard' && (
        <DashboardSection
          totals={totals}
          selected={selected}
          alerts={alerts}
          dashboard={dashboard}
          readings={readings}
          sensors={allSensors}
          actuators={actuators}
          t={t}
          openAlerts={() => setActiveSection('alerts')}
          simulatorRunning={simulatorRunning}
          onStartSimulator={async () => { await startSimulator(); setSimulatorRunning(true); showToast(t.simulatorStarted, 'success'); }}
          onStopSimulator={async () => { await stopSimulator(); setSimulatorRunning(false); showToast(t.simulatorStopped, 'info'); }}
        />
      )}

      {activeSection === 'architecture' && <ArchitectureSection t={t} />}
      {activeSection === 'dictionary' && <DataDictionarySection t={t} />}
      {activeSection === 'greenhouses' && (
        <GreenhousesSection
          form={greenhouseForm}
          setForm={setGreenhouseForm}
          onSubmit={handleCreateGreenhouse}
          editForm={greenhouseEditForm}
          setEditForm={setGreenhouseEditForm}
          onUpdate={handleUpdateGreenhouse}
          onDelete={handleDeleteGreenhouse}
          greenhouses={greenhouses}
          selected={selected}
          setSelectedId={handleSelectGreenhouse}
          t={t}
        />
      )}

      {activeSection === 'zones' && (
        <CrudSection title={t.zones} formTitle={t.createZone} items={zones} emptyItem={{ name: '', description: '', active: true, greenhouseId: greenhouses[0]?.id ?? '' }} fields={zoneFields(t, greenhouses)} columns={[{ key: 'name', label: t.name }, { key: 'greenhouseName', label: t.greenhouses }, { key: 'active', label: t.status, render: (item) => item.active ? t.active : t.inactive }]} onCreate={async (payload) => { await createZone(payload); await refresh(); }} onUpdate={async (id, payload) => { await updateZone(id, payload); await refresh(); }} onDelete={async (item) => { await deleteZone(item.id); await refresh(); }} t={t} />
      )}

      {activeSection === 'sensors' && (
        <CrudSection title={t.sensors} formTitle={t.addSensor} items={allSensors} emptyItem={{ code: '', type: 'TEMPERATURE', unit: 'C', minThreshold: 0, maxThreshold: 100 }} fields={sensorFields(t)} columns={[{ key: 'code', label: t.sensorCode }, { key: 'type', label: t.sensorType }, { key: 'greenhouseName', label: t.greenhouses }, { key: 'unit', label: t.unit }]} onCreate={async (payload) => { await addSensor(greenhouses[0]?.id, payload); await refresh(); }} onUpdate={async (id, payload) => { await updateSensorRecord(id, payload); await refresh(); }} onDelete={async (item) => { await deleteSensor(item.id); await refresh(); }} t={t} />
      )}

      {activeSection === 'readings' && (
        <CrudSection title={t.readings} formTitle={t.createReading} items={readings} emptyItem={{ sensorId: allSensors[0]?.id ?? '', value: '' }} fields={readingFields(t, allSensors)} columns={[{ key: 'sensorCode', label: t.sensorCode }, { key: 'value', label: t.value, render: (item) => `${item.value} ${item.unit}` }, { key: 'recordedAt', label: t.date }]} onCreate={async (payload) => { await createReading(payload); await refresh(); }} onUpdate={async (id, payload) => { await updateReading(id, payload); await refresh(); }} onDelete={async (item) => { await deleteReading(item.id); await refresh(); }} t={t} />
      )}

      {activeSection === 'actuators' && (
        <CrudSection title={t.actuators} formTitle={t.createActuator} items={actuators} emptyItem={{ name: '', type: 'IRRIGATION', enabled: false, active: true, greenhouseId: greenhouses[0]?.id ?? '' }} fields={actuatorFields(t, greenhouses)} columns={[{ key: 'name', label: t.name }, { key: 'type', label: t.type }, { key: 'enabled', label: t.status, render: (item) => item.enabled ? t.enabled : t.disabled }]} onCreate={async (payload) => { await createActuator(payload); await refresh(); }} onUpdate={async (id, payload) => { await updateActuator(id, payload); await refresh(); }} onDelete={async (item) => { await deleteActuator(item.id); await refresh(); }} t={t} />
      )}

      {activeSection === 'rules' && (
        <CrudSection title={t.rules} formTitle={t.createRule} items={rules} emptyItem={{ name: 'Humedad baja activa riego', type: 'LOW_HUMIDITY_IRRIGATION', threshold: 45, enabled: true, greenhouseId: greenhouses[0]?.id ?? '' }} fields={ruleFields(t, greenhouses)} columns={[{ key: 'name', label: t.name }, { key: 'threshold', label: t.threshold }, { key: 'enabled', label: t.status, render: (item) => item.enabled ? t.active : t.inactive }]} onCreate={async (payload) => { await createRule(payload); await refresh(); }} onUpdate={async (id, payload) => { await updateRule(id, payload); await refresh(); }} onDelete={async (item) => { await deleteRule(item.id); await refresh(); }} t={t} />
      )}

      {activeSection === 'operations' && (
        <OperationsSection
          greenhouses={greenhouses}
          selected={selected}
          setSelectedId={setSelectedId}
          cropForm={cropForm}
          setCropForm={setCropForm}
          sensorForm={sensorForm}
          setSensorForm={setSensorForm}
          irrigationForm={irrigationForm}
          setIrrigationForm={setIrrigationForm}
          onAddCrop={handleAddCrop}
          onAddSensor={handleAddSensor}
          onAddIrrigation={handleAddIrrigation}
          onUpdateCrop={handleUpdateCrop}
          onUpdateSensor={handleUpdateSensor}
          onUpdateIrrigation={handleUpdateIrrigation}
          t={t}
        />
      )}

      {activeSection === 'alerts' && <AlertsSection alerts={alerts} onResolve={handleResolveAlert} t={t} />}
      {activeSection === 'ia' && <IaSection readings={readings} sensors={allSensors} t={t} />}
      {activeSection === 'taiga' && <TaigaSection t={t} />}
      {activeSection === 'logs' && <LogsSection t={t} />}
      {activeSection === 'users' && (
        <UsersSection
          users={users}
          form={userForm}
          setForm={setUserForm}
          onCreateUser={handleCreateUser}
          onChangeRole={handleChangeRole}
          t={t}
        />
      )}
      {activeSection === 'data' && <DataSection onExport={exportJson} t={t} />}
      {activeSection === 'manual' && <ManualSection t={t} />}
        </>
      )}
      </section>
    </main>
    </ErrorBoundary>
  );
}

function useStoredSession() {
  return useState(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('oauth') === 'google') {
      return null;
    }
    return getStoredSession();
  });
}

function clearOAuthQuery() {
  window.history.replaceState({}, '', window.location.pathname);
}

function greenhouseOptions(greenhouses) {
  return greenhouses.map((greenhouse) => ({ value: greenhouse.id, label: greenhouse.name }));
}

function pickDefaultGreenhouseId(greenhouses, sensors, readings) {
  const sensorById = new Map(sensors.map((sensor) => [sensor.id, sensor]));
  const latestReading = readings
    .slice()
    .sort((a, b) => new Date(a.recordedAt) - new Date(b.recordedAt))
    .at(-1);
  const greenhouseId = latestReading ? sensorById.get(latestReading.sensorId)?.greenhouseId : null;
  if (greenhouses.some((greenhouse) => greenhouse.id === greenhouseId)) return greenhouseId;
  return greenhouses.find((greenhouse) => greenhouse.active)?.id ?? greenhouses[0]?.id ?? null;
}

function zoneFields(t, greenhouses) {
  return [
    { name: 'name', placeholder: t.name, required: true },
    { name: 'description', placeholder: t.detail },
    { name: 'greenhouseId', placeholder: t.greenhouses, type: 'select', required: true, options: greenhouseOptions(greenhouses), parse: Number },
    { name: 'active', placeholder: t.active, type: 'checkbox' }
  ];
}

function sensorFields(t) {
  return [
    { name: 'code', placeholder: t.sensorCode, required: true },
    { name: 'type', placeholder: t.sensorType, type: 'select', required: true, options: ['TEMPERATURE', 'HUMIDITY', 'SOIL_MOISTURE', 'LIGHT'].map((value) => ({ value, label: value })) },
    { name: 'unit', placeholder: t.unit, required: true },
    { name: 'minThreshold', placeholder: 'Min', type: 'number', parse: Number },
    { name: 'maxThreshold', placeholder: 'Max', type: 'number', parse: Number }
  ];
}

function readingFields(t, sensors) {
  return [
    { name: 'sensorId', placeholder: t.sensors, type: 'select', required: true, options: sensors.map((sensor) => ({ value: sensor.id, label: sensor.code })), parse: Number },
    { name: 'value', placeholder: t.value, type: 'number', required: true, parse: Number }
  ];
}

function actuatorFields(t, greenhouses) {
  return [
    { name: 'name', placeholder: t.name, required: true },
    { name: 'type', placeholder: t.type, type: 'select', required: true, options: ['IRRIGATION', 'FAN', 'HEATER', 'LIGHT'].map((value) => ({ value, label: value })) },
    { name: 'greenhouseId', placeholder: t.greenhouses, type: 'select', required: true, options: greenhouseOptions(greenhouses), parse: Number },
    { name: 'enabled', placeholder: t.enabled, type: 'checkbox' },
    { name: 'active', placeholder: t.active, type: 'checkbox' }
  ];
}

function ruleFields(t, greenhouses) {
  return [
    { name: 'name', placeholder: t.name, required: true },
    { name: 'type', placeholder: t.type, type: 'select', required: true, options: [{ value: 'LOW_HUMIDITY_IRRIGATION', label: t.lowHumidityRule }] },
    { name: 'threshold', placeholder: t.threshold, type: 'number', required: true, parse: Number },
    { name: 'greenhouseId', placeholder: t.greenhouses, type: 'select', required: true, options: greenhouseOptions(greenhouses), parse: Number },
    { name: 'enabled', placeholder: t.enabled, type: 'checkbox' }
  ];
}
