import { useAuth } from '../context/AuthContext';

export default function MentorDashboard() {
  const { user } = useAuth();

  return (
    <main className="dashboard-main">
      <div className="dashboard-header">
        <h1>Welcome, {user.fullName}</h1>
        <p>Mentor dashboard — manage your mentees and availability.</p>
      </div>

      <div className="card-grid">
        <div className="card">
          <h3>Expertise</h3>
          <div className="card-value">{user.expertise || '—'}</div>
        </div>
        <div className="card">
          <h3>Department</h3>
          <div className="card-value">{user.department || '—'}</div>
        </div>
        <div className="card">
          <h3>Active mentees</h3>
          <div className="card-value">0</div>
        </div>
      </div>
    </main>
  );
}
