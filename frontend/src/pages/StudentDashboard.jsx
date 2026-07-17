import { useAuth } from '../context/AuthContext';

export default function StudentDashboard() {
  const { user } = useAuth();

  return (
    <main className="dashboard-main">
      <div className="dashboard-header">
        <h1>Welcome, {user.fullName}</h1>
        <p>Student dashboard — find mentors and track your tutoring sessions.</p>
      </div>

      <div className="card-grid">
        <div className="card">
          <h3>Student number</h3>
          <div className="card-value">{user.studentNumber || '—'}</div>
        </div>
        <div className="card">
          <h3>Program</h3>
          <div className="card-value">{user.program || '—'}</div>
        </div>
        <div className="card">
          <h3>Upcoming sessions</h3>
          <div className="card-value">0</div>
        </div>
      </div>
    </main>
  );
}
