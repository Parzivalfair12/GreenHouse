export function Navbar({ sections, activeSection, onChange, alertsCount = 0, t }) {
  return (
    <nav className="navbar" aria-label={t?.navSections || 'Secciones principales'}>
      {sections.map(({ id, label, icon: Icon, action }) => (
        <button
          className={activeSection === id ? 'navItem active' : 'navItem'}
          key={id}
          type="button"
          onClick={() => action ? action() : onChange(id)}
        >
          <Icon size={18} aria-hidden="true" />
          <span>{label}</span>
          {id === 'alerts' && alertsCount > 0 && <strong className="navBadge">{alertsCount}</strong>}
        </button>
      ))}
    </nav>
  );
}
