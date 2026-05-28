import { useState } from 'react';
import { Globe2, Leaf, LockKeyhole, LogIn, Mail, ArrowLeft, Send } from 'lucide-react';

/* Pantalla de login con soporte para autenticación por email y OAuth (Google).
 * Flujo de datos: onLogin({ email, password }) → backend JWT; onGoogleLogin → redirección OAuth.
 * Manejo de errores: prop 'error' (string) se muestra como alerta roja; 'unverified' + onResendVerification
 *   muestra enlace para reenviar verificación de email.
 * Modo forgot-password: conmutado por state 'mode' (login | forgot).
 *   La llamada a /api/auth/forgot-password es directa con fetch, no pasa por el manejador de API central.
 * Placeholders: emailPlaceholder, passwordPlaceholder desde i18n.
 * Selector de idioma: setLanguage actualiza el locale global. */
export function LoginScreen({ language, setLanguage, t, onLogin, onGoogleLogin, onResendVerification, error, unverified }) {
  /* Email pre-poblado con credencial de demo para facilitar acceso de prueba */
  const [email, setEmail] = useState('admin@greenhouse.local');
  const [password, setPassword] = useState('');
  const [mode, setMode] = useState('login'); // login | forgot
  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotMessage, setForgotMessage] = useState('');

  /* Envía credenciales al callback de autenticación del padre */
  function submitLogin(event) {
    event.preventDefault();
    onLogin({ email, password, fullName: email });
  }

  /* Llama directamente al endpoint de forgot-password (POST con email).
   * En caso de error en la respuesta o de red, muestra mensaje genérico t.resetSent. */
  async function submitForgot(event) {
    event.preventDefault();
    setForgotMessage('');
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL ?? ''}/api/auth/forgot-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: forgotEmail })
      });
      const data = await response.json().catch(() => ({}));
      setForgotMessage(data.message || t.resetSent);
    } catch {
      setForgotMessage(t.resetSent);
    }
  }

  /* Vista de forgot-password: formulario con email + botón de envío + volver al login */
  if (mode === 'forgot') {
    return (
      <main className="loginShell">
        <section className="loginPanel">
          <div className="loginBrand">
            <span className="brandMark"><Leaf size={26} /></span>
            <div>
              <p className="eyebrow">{t.brandTitle}</p>
              <h1>{t.resetPassword}</h1>
            </div>
          </div>
          <form className="loginForm" onSubmit={submitForgot}>
            <div className="formHeading">
              <h2>{t.forgotPassword}</h2>
            </div>
            <label>
              {t.email}
              <span className="inputWrap">
                <Mail size={18} />
                <input required type="email" value={forgotEmail} onChange={(e) => setForgotEmail(e.target.value)} />
              </span>
            </label>
            {forgotMessage && <p className="loginError" role="alert" style={{ color: '#4ade80' }}>{forgotMessage}</p>}
            <button type="submit">
              <Send size={18} />
              {t.sendResetLink}
            </button>
            <button className="googleButton" type="button" onClick={() => setMode('login')}>
              <ArrowLeft size={18} />
              {t.backToLogin}
            </button>
          </form>
        </section>
      </main>
    );
  }

  /* Vista principal de login: formulario con email, password, Google OAuth, selector de idioma y hint demo */
  return (
    <main className="loginShell">
      <section className="loginPanel">
        {/* Branding y badge de conexión a BD */}
        <div className="loginBrand">
          <span className="brandMark">
            <Leaf size={26} />
          </span>
          <div>
            <p className="eyebrow">{t.brandTitle}</p>
            <h1>{t.loginTitle}</h1>
            <p className="subtitle">{t.loginSubtitle}</p>
          </div>
          <div className="loginProof">
            <span>{t.apiConnected}</span>
            <strong>PostgreSQL / app_user</strong>
          </div>
        </div>
        <form className="loginForm" onSubmit={submitLogin}>
          <div className="formHeading">
            <h2>{t.signIn}</h2>
            <p>{t.loginInstruction}</p>
          </div>
          <label>
            {t.email}
            <span className="inputWrap">
              <Mail size={18} />
              <input required type="email" placeholder={t.emailPlaceholder} value={email} onChange={(event) => setEmail(event.target.value)} />
            </span>
          </label>
          <label>
            {t.password}
            <span className="inputWrap">
              <LockKeyhole size={18} />
              <input required minLength="4" type="password" placeholder={t.passwordPlaceholder} value={password} onChange={(event) => setPassword(event.target.value)} />
            </span>
          </label>
          {/* Error de autenticación desde el backend */}
          {error && <p className="loginError" role="alert">{error}</p>}
          {/* Enlace para reenviar verificación si el email no está verificado */}
          {unverified && onResendVerification && (
            <p style={{ fontSize: '13px', marginTop: '-4px', marginBottom: '8px' }}>
              <button type="button" onClick={onResendVerification} style={{ background: 'transparent', border: 'none', color: '#4ade80', textDecoration: 'underline', cursor: 'pointer', padding: 0 }}>
                {t.resendVerification}
              </button>
            </p>
          )}
          <button type="submit">
            <LogIn size={18} />
            {t.signIn}
          </button>
          {/* Botón de OAuth Google */}
          <button className="googleButton" type="button" onClick={onGoogleLogin}>
            <Globe2 size={18} />
            {t.signInGoogle}
          </button>
          {/* Enlace para cambiar a forgot-password */}
          <p style={{ textAlign: 'center', marginTop: '8px' }}>
            <button type="button" onClick={() => setMode('forgot')} style={{ background: 'transparent', border: 'none', color: '#94a3b8', textDecoration: 'underline', cursor: 'pointer', fontSize: '13px' }}>
              {t.forgotPassword}
            </button>
          </p>
          {/* Selector de idioma ES/EN */}
          <label className="loginLanguage">
            {t.language}
            <select value={language} onChange={(event) => setLanguage(event.target.value)}>
              <option value="es">ES</option>
              <option value="en">EN</option>
            </select>
          </label>
          {/* Hint de credenciales demo */}
          <p className="loginHint">{t.demoAccess}: admin@greenhouse.local / admin1234</p>
        </form>
      </section>
    </main>
  );
}
