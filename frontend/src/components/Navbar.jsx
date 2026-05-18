export function Navbar({ sections, activeSection, onChange, alertsCount = 0 }) {
  return (
    <nav className="navbar" aria-label="Secciones principales">
      {sections.map(({ id, label, icon: Icon }) => (
        <button
          className={activeSection === id ? 'navItem active' : 'navItem'}
          key={id}
          type="button"
          onClick={() => onChange(id)}
        >
          <Icon size={18} aria-hidden="true" />
          <span>{label}</span>
          {id === 'alerts' && alertsCount > 0 && <strong className="navBadge">{alertsCount}</strong>}
        </button>
      ))}
    </nav>
  );
}
