import { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { fetchAllUsers } from '../api/auth';
import SubjectManager from '../components/SubjectManager';

const ROLE_FILTERS = ['ALL', 'STUDENT', 'MENTOR', 'ADMIN'];

export default function AdminDashboard() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetchAllUsers()
      .then((data) => {
        if (!cancelled) setUsers(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err.response?.data?.message || 'Could not load users.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const counts = useMemo(() => {
    return users.reduce(
      (acc, u) => {
        acc.total += 1;
        acc[u.role] = (acc[u.role] || 0) + 1;
        return acc;
      },
      { total: 0 }
    );
  }, [users]);

  const filteredUsers = useMemo(() => {
    if (roleFilter === 'ALL') return users;
    return users.filter((u) => u.role === roleFilter);
  }, [users, roleFilter]);

  return (
    <main className="dashboard-main">
      <div className="dashboard-header">
        <h1>Welcome, {user.fullName}</h1>
        <p>Admin dashboard — manage accounts, the subject catalog, and platform activity.</p>
      </div>

      <div className="card-grid" style={{ marginBottom: 24 }}>
        <div className="card">
          <h3>Total users</h3>
          <div className="card-value">{counts.total}</div>
        </div>
        <div className="card">
          <h3>Students</h3>
          <div className="card-value">{counts.STUDENT || 0}</div>
        </div>
        <div className="card">
          <h3>Mentors</h3>
          <div className="card-value">{counts.MENTOR || 0}</div>
        </div>
        <div className="card">
          <h3>Admins</h3>
          <div className="card-value">{counts.ADMIN || 0}</div>
        </div>
      </div>

      <section style={{ marginBottom: 32 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
          <h2 style={{ fontSize: '1.05rem', margin: 0 }}>User accounts</h2>
          <div className="role-toggle" style={{ marginBottom: 0, width: 'auto' }}>
            {ROLE_FILTERS.map((r) => (
              <button
                key={r}
                type="button"
                className={roleFilter === r ? 'active' : ''}
                onClick={() => setRoleFilter(r)}
              >
                {r}
              </button>
            ))}
          </div>
        </div>

        {loading && <div className="loading-state">Loading users…</div>}
        {error && <div className="form-error-banner">{error}</div>}

        {!loading && !error && (
          filteredUsers.length === 0 ? (
            <div className="empty-state">No users match this filter.</div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((u) => (
                  <tr key={u.id}>
                    <td>{u.fullName}</td>
                    <td>{u.email}</td>
                    <td><span className={`role-pill ${u.role}`}>{u.role}</span></td>
                    <td>
                      {u.role === 'STUDENT' && `${u.program || '—'} · ${u.studentNumber || '—'}`}
                      {u.role === 'MENTOR' && `${u.expertise || '—'} · ${u.department || '—'}`}
                      {u.role === 'ADMIN' && '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )
        )}
      </section>

      <SubjectManager />

      <section>
        <h2 style={{ fontSize: '1.05rem', marginBottom: 12 }}>Coming next</h2>
        <div className="card-grid">
          <div className="card">
            <h3>Tutor approvals</h3>
            <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
              Review and approve newly registered mentor profiles.
            </p>
          </div>
          <div className="card">
            <h3>Feedback moderation</h3>
            <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
              Review flagged feedback left on completed sessions.
            </p>
          </div>
          <div className="card">
            <h3>Activity reports</h3>
            <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
              Generate usage and matching reports for the term.
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}
