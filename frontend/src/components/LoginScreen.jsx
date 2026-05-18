import { useState } from 'react';
import { Globe2, Leaf, LockKeyhole, LogIn, Mail } from 'lucide-react';

export function LoginScreen({ language, setLanguage, t, onLogin, onGoogleLogin, error }) {
  const [email, setEmail] = useState('admin@greenhouse.local');
  const [password, setPassword] = useState('');

  function submitLogin(event) {
    event.preventDefault();
    onLogin({ email, password, fullName: email });
  }

  return (
    <main className="loginShell">
      <section className="loginPanel">
        <div className="loginBrand">
          <span className="brandMark">
            <Leaf size={26} />
          </span>
          <div>
            <p className="eyebrow">Greenhouse Manager</p>
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
              <input required type="email" placeholder="admin@greenhouse.local" value={email} onChange={(event) => setEmail(event.target.value)} />
            </span>
          </label>
          <label>
            {t.password}
            <span className="inputWrap">
              <LockKeyhole size={18} />
              <input required minLength="4" type="password" placeholder="admin1234" value={password} onChange={(event) => setPassword(event.target.value)} />
            </span>
          </label>
          {error && <p className="loginError" role="alert">{error}</p>}
          <button type="submit">
            <LogIn size={18} />
            {t.signIn}
          </button>
          <button className="googleButton" type="button" onClick={onGoogleLogin}>
            <Globe2 size={18} />
            {t.signInGoogle}
          </button>
          <label className="loginLanguage">
            {t.language}
            <select value={language} onChange={(event) => setLanguage(event.target.value)}>
              <option value="es">ES</option>
              <option value="en">EN</option>
            </select>
          </label>
          <p className="loginHint">{t.demoAccess}: admin@greenhouse.local / admin1234</p>
        </form>
      </section>
    </main>
  );
}
