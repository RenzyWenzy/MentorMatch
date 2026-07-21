import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  fetchOwnBookingsAsMentor,
  confirmBooking,
  declineBooking,
  completeBooking,
} from '../api/bookings';
import { fetchMyTutorProfile } from '../api/tutorProfile';
import { fetchTutorReviews } from '../api/reviews';

/** BR-002: only PENDING/REJECTED need a banner — APPROVED is the quiet default. */
const APPROVAL_BANNER_META = {
  PENDING: {
    text: "Your tutor profile is pending admin review. Students won't see you in search until it's approved.",
    background: '#fff8e6',
    color: '#8a6116',
    border: '#f3e1ad',
  },
  REJECTED: {
    text: 'Your tutor profile was not approved. Update it and it will go back into the review queue.',
    background: '#fdecea',
    color: 'var(--color-danger)',
    border: '#f6cdc9',
  },
};

export default function MentorDashboard() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [profile, setProfile] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadBookings();
    loadRatingInfo();
  }, []);

  async function loadBookings() {
    setLoading(true);
    setError('');
    try {
      const data = await fetchOwnBookingsAsMentor();
      setBookings(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load bookings.');
    } finally {
      setLoading(false);
    }
  }

  async function loadRatingInfo() {
    try {
      const own = await fetchMyTutorProfile();
      setProfile(own);
      const ownReviews = await fetchTutorReviews(own.id);
      setReviews(ownReviews);
    } catch {
      // No tutor profile yet (e.g. mentor hasn't set one up) — leave rating section empty.
      setProfile(null);
    }
  }

  async function handleAction(actionFn, id) {
    setError('');
    try {
      await actionFn(id);
      await loadBookings();
    } catch (err) {
      setError(err.response?.data?.message || 'Action failed.');
    }
  }

  const activeCount = bookings.filter((b) => b.status === 'CONFIRMED').length;
  const ratingLabel = profile?.averageRating != null
    ? `${profile.averageRating.toFixed(1)}★ (${profile.reviewCount})`
    : 'No ratings yet';
  const approvalBanner = profile ? APPROVAL_BANNER_META[profile.approvalStatus] : null;

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

      {approvalBanner && (
        <div
          style={{
            background: approvalBanner.background,
            color: approvalBanner.color,
            border: `1px solid ${approvalBanner.border}`,
            borderRadius: 8,
            padding: '10px 14px',
            fontSize: '0.85rem',
            marginBottom: 20,
          }}
        >
          {approvalBanner.text}
        </div>
      )}

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
          <div className="card-value">{activeCount}</div>
        </div>
        <div className="card">
          <h3>Average rating</h3>
          <div className="card-value">{ratingLabel}</div>
        </div>
      </div>

      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      <section style={{ marginTop: 32 }}>
        <h2>Session requests</h2>
        {loading ? (
          <p>Loading…</p>
        ) : bookings.length === 0 ? (
          <p>No booking requests yet.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={{ textAlign: 'left' }}>Student</th>
                <th style={{ textAlign: 'left' }}>Subject</th>
                <th style={{ textAlign: 'left' }}>Date</th>
                <th style={{ textAlign: 'left' }}>Time</th>
                <th style={{ textAlign: 'left' }}>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.id}>
                  <td>{b.studentName}</td>
                  <td>{b.subjectName}</td>
                  <td>{b.sessionDate}</td>
                  <td>{b.startTime}–{b.endTime}</td>
                  <td>{b.status}</td>
                  <td style={{ display: 'flex', gap: 8 }}>
                    {b.status === 'PENDING' && (
                      <>
                        <button onClick={() => handleAction(confirmBooking, b.id)} className="btn btn-primary">Confirm</button>
                        <button onClick={() => handleAction(declineBooking, b.id)} className="btn">Decline</button>
                      </>
                    )}
                    {b.status === 'CONFIRMED' && (
                      <button onClick={() => handleAction(completeBooking, b.id)} className="btn">Mark completed</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {profile && (
        <section style={{ marginTop: 32 }}>
          <h2>My reviews</h2>
          {reviews.length === 0 ? (
            <p>No reviews yet.</p>
          ) : (
            <ul style={{ listStyle: 'none', padding: 0, display: 'grid', gap: 12 }}>
              {reviews.map((r) => (
                <li key={r.id} className="card">
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <strong>{r.studentName}</strong>
                    <span>{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
                  </div>
                  {r.comment && <p style={{ margin: '8px 0 0' }}>{r.comment}</p>}
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
    </main>
  );
}
