import { Bell, ChevronDown, Database, Globe2, Leaf, Moon, Sun, UserRound } from 'lucide-react';

export function AppHeader({ language, setLanguage, session, t, onLogout, theme, onToggleTheme }) {
  return (
    <header className="hero">
      <span className="statusDot"><Database size={16} />{t.apiConnected}</span>
      <div className="headerTools">
        <button className="themeToggle" type="button" onClick={onToggleTheme} aria-label={t.language}>
          {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
        </button>
        <label className="language">
          <Globe2 size={18} aria-hidden="true" />
          <span>{t.language}</span>
          <select value={language} onChange={(event) => setLanguage(event.target.value)} aria-label={t.language}>
            <option value="es">ES</option>
            <option value="en">EN</option>
          </select>
        </label>
        <span className="notificationDot"><Bell size={18} /></span>
        <span className="userAvatar"><UserRound size={18} /></span>
        <button className="userBadge" type="button" onClick={onLogout}>
          {session.email}
          <ChevronDown size={16} />
        </button>
      </div>
    </header>
  );
}

export function SidebarBrand({ t }) {
  return (
    <div className="sidebarBrand">
      <span className="brandIcon"><Leaf size={28} /></span>
      <div>
        <strong>{t.brandName}</strong>
        <span>{t.title}</span>
      </div>
    </div>
  );
}
