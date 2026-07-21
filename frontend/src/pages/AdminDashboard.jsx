import { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { fetchAllUsers } from '../api/auth';
import { activateUser, deactivateUser, removeUser } from '../api/users';
import SubjectManager from '../components/SubjectManager';
import AdminReport from '../components/AdminReport';
import TutorApprovalQueue from '../components/TutorApprovalQueue';

const ROLE_FILTERS = ['ALL', 'STUDENT', 'MENTOR', 'ADMIN'];

export default function AdminDashboard() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [actionError, setActionError] = useState('');
  const [pendingActionId, setPendingActionId] = useState(null);

  const loadUsers = () => {
    setLoading(true);
    setError('');
    fetchAllUsers()
      .then(setUsers)
      .catch((err) => setError(err.response?.data?.message || 'Could not load users.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadUsers();
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

  const handleActivate = async (id) => {
    setActionError('');
    setPendingActionId(id);
    try {
      const updated = await activateUser(id);
      setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)));
    } catch (err) {
      setActionError(err.response?.data?.message || 'Could not activate this account.');
    } finally {
      setPendingActionId(null);
    }
  };

  const handleDeactivate = async (id) => {
    if (!window.confirm('Deactivate this account? The user will no longer be able to log in.')) return;
    setActionError('');
    setPendingActionId(id);
    try {
      const updated = await deactivateUser(id);
      setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)));
    } catch (err) {
      setActionError(err.response?.data?.message || 'Could not deactivate this account.');
    } finally {
      setPendingActionId(null);
    }
  };

  const handleRemove = async (id) => {
    if (!window.confirm('Permanently remove this account? This cannot be undone.')) return;
    setActionError('');
    setPendingActionId(id);
    try {
      await removeUser(id);
      setUsers((prev) => prev.filter((u) => u.id !== id));
    } catch (err) {
      setActionError(err.response?.data?.message || 'Could not remove this account.');
    } finally {
      setPendingActionId(null);
    }
  };

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
        {actionError && <div className="form-error-banner">{actionError}</div>}

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
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((u) => {
                  const isSelf = u.id === user.id;
                  const isPending = pendingActionId === u.id;
                  return (
                    <tr key={u.id}>
                      <td>{u.fullName}{isSelf && ' (you)'}</td>
                      <td>{u.email}</td>
                      <td><span className={`role-pill ${u.role}`}>{u.role}</span></td>
                      <td>
                        {u.role === 'STUDENT' && `${u.program || '—'} · ${u.studentNumber || '—'}`}
                        {u.role === 'MENTOR' && `${u.expertise || '—'} · ${u.department || '—'}`}
                        {u.role === 'ADMIN' && '—'}
                      </td>
                      <td>
                        <span style={{ color: u.active ? 'var(--color-success, #2e7d32)' : 'var(--color-danger)' }}>
                          {u.active ? 'Active' : 'Deactivated'}
                        </span>
                      </td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        {isSelf ? (
                          '—'
                        ) : (
                          <>
                            {u.active ? (
                              <button
                                type="button"
                                className="btn btn-ghost"
                                style={{ color: 'var(--color-danger)', border: '1px solid var(--color-border)', padding: '4px 10px', fontSize: '0.78rem', marginRight: 6 }}
                                onClick={() => handleDeactivate(u.id)}
                                disabled={isPending}
                              >
                                {isPending ? 'Working…' : 'Deactivate'}
                              </button>
                            ) : (
                              <button
                                type="button"
                                className="btn btn-ghost"
                                style={{ color: 'var(--color-primary)', border: '1px solid var(--color-border)', padding: '4px 10px', fontSize: '0.78rem', marginRight: 6 }}
                                onClick={() => handleActivate(u.id)}
                                disabled={isPending}
                              >
                                {isPending ? 'Working…' : 'Activate'}
                              </button>
                            )}
                            <button
                              type="button"
                              className="btn btn-ghost"
                              style={{ color: 'var(--color-danger)', border: '1px solid var(--color-border)', padding: '4px 10px', fontSize: '0.78rem' }}
                              onClick={() => handleRemove(u.id)}
                              disabled={isPending}
                            >
                              Remove
                            </button>
                          </>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )
        )}
      </section>

      <SubjectManager />

      <TutorApprovalQueue />

      <AdminReport />

      <section>
        <h2 style={{ fontSize: '1.05rem', marginBottom: 12 }}>Coming next</h2>
        <div className="card-grid">
          <div className="card">
            <h3>Feedback moderation</h3>
            <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
              Review flagged feedback left on completed sessions.
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}
