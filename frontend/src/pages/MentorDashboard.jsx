import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function MentorDashboard() {
  const { user } = useAuth();

  return (
    <main className="dashboard-main">
      <div className="dashboard-header" style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 16 }}>
        <div>
          <h1>Welcome, {user.fullName}</h1>
          <p>Mentor dashboard — manage your mentees and availability.</p>
        </div>
        <Link to="/dashboard/mentor/profile" className="btn btn-primary" style={{ textDecoration: 'none', whiteSpace: 'nowrap' }}>
          Edit my profile
        </Link>
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
