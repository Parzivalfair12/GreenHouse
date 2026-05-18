import { ShieldCheck, UserPlus } from 'lucide-react';
import { PaginationControls, Panel, Section, usePagination } from './shared.jsx';

export function UsersSection({ users, form, setForm, onCreateUser, onChangeRole, t }) {
  const pagination = usePagination(users, 6);

  return (
    <Section title={t.usersCenter}>
      <div className="splitGrid">
        <Panel title={t.createUser}>
          <form className="form" onSubmit={onCreateUser}>
            <input required type="text" placeholder={t.fullName} value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} />
            <input required type="email" placeholder={t.email} value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
            <input required minLength="6" type="password" placeholder={t.password} value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
            <select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value })}>
              <option value="ADMIN">{t.roleAdmin}</option>
              <option value="OPERATOR">{t.roleOperator}</option>
              <option value="VIEWER">{t.roleViewer}</option>
            </select>
            <button type="submit">
              <UserPlus size={18} />
              {t.createUser}
            </button>
          </form>
        </Panel>

        <Panel title={t.rolesExplanation}>
          <div className="roleInfo">
            <p><strong>{t.roleAdmin}</strong><span>{t.roleAdminText}</span></p>
            <p><strong>{t.roleOperator}</strong><span>{t.roleOperatorText}</span></p>
            <p><strong>{t.roleViewer}</strong><span>{t.roleViewerText}</span></p>
          </div>
        </Panel>
      </div>

      <div className="usersTable">
        {pagination.pagedItems.map((user) => (
          <article className="userRow" key={user.id}>
            <span className="userIcon"><ShieldCheck size={18} /></span>
            <div>
              <strong>{user.fullName}</strong>
              <small>{user.email}</small>
            </div>
            <span className="providerTag">{user.provider}</span>
            <select value={user.role} onChange={(event) => onChangeRole(user.id, event.target.value)}>
              <option value="ADMIN">{t.roleAdmin}</option>
              <option value="OPERATOR">{t.roleOperator}</option>
              <option value="VIEWER">{t.roleViewer}</option>
            </select>
          </article>
        ))}
        <PaginationControls pagination={pagination} t={t} />
      </div>
    </Section>
  );
}
