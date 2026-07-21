import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { fetchAllTutorProfiles, searchTutorProfiles } from '../api/tutorProfile';
import { createBooking, fetchOwnBookingsAsStudent, cancelBooking } from '../api/bookings';
import { submitReview } from '../api/reviews';
import { fetchSubjects } from '../api/subjects';

const DAYS_OF_WEEK = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

export default function StudentDashboard() {
  const { user } = useAuth();

  const [tutors, setTutors] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Tutor search (FR-004): filter by subject and/or a weekly time slot.
  // searchResults is null until the student runs a search; an empty array
  // is a real "no matches" result and is displayed differently.
  const [searchForm, setSearchForm] = useState({
    subjectId: '', dayOfWeek: '', startTime: '', endTime: '',
  });
  const [searchResults, setSearchResults] = useState(null);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState('');

  const [form, setForm] = useState({
    tutorProfileId: '',
    subjectId: '',
    sessionDate: '',
    startTime: '',
    endTime: '',
  });

  // Review workflow state (FR-009): which booking's review form is open,
  // its draft values, and the set of bookings already reviewed this session
  // (the backend still enforces one-review-per-booking either way).
  const [reviewingId, setReviewingId] = useState(null);
  const [reviewForm, setReviewForm] = useState({ rating: '5', comment: '' });
  const [reviewSubmitting, setReviewSubmitting] = useState(false);
  const [reviewError, setReviewError] = useState('');
  const [reviewedIds, setReviewedIds] = useState(() => new Set());

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError('');
    try {
      const [tutorList, myBookings, subjectList] = await Promise.all([
        fetchAllTutorProfiles(),
        fetchOwnBookingsAsStudent(),
        fetchSubjects(),
      ]);
      setTutors(tutorList);
      setBookings(myBookings);
      setSubjects(subjectList);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load data.');
    } finally {
      setLoading(false);
    }
  }

  const selectedTutor = tutors.find((t) => String(t.id) === String(form.tutorProfileId));

  function updateForm(field, value) {
    setForm((prev) => ({
      ...prev,
      [field]: value,
      // Reset subject choice whenever the tutor changes, since their subject list differs.
      ...(field === 'tutorProfileId' ? { subjectId: '' } : {}),
    }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await createBooking({
        tutorProfileId: Number(form.tutorProfileId),
        subjectId: Number(form.subjectId),
        sessionDate: form.sessionDate,
        startTime: form.startTime,
        endTime: form.endTime,
      });
      setForm({ tutorProfileId: '', subjectId: '', sessionDate: '', startTime: '', endTime: '' });
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit booking request.');
    } finally {
      setSubmitting(false);
    }
  }

  function updateSearchForm(field, value) {
    setSearchForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSearch(e) {
    e.preventDefault();
    setSearchError('');
    setSearching(true);
    try {
      const results = await searchTutorProfiles({
        subjectId: searchForm.subjectId || undefined,
        dayOfWeek: searchForm.dayOfWeek || undefined,
        startTime: searchForm.startTime || undefined,
        endTime: searchForm.endTime || undefined,
      });
      setSearchResults(results);
    } catch (err) {
      setSearchError(err.response?.data?.message || 'Search failed.');
    } finally {
      setSearching(false);
    }
  }

  function clearSearch() {
    setSearchForm({ subjectId: '', dayOfWeek: '', startTime: '', endTime: '' });
    setSearchResults(null);
    setSearchError('');
  }

  /** Jumps a search result straight into the "Request a session" form below. */
  function selectTutorForBooking(tutorProfileId) {
    setForm((prev) => ({ ...prev, tutorProfileId: String(tutorProfileId), subjectId: '' }));
    document.getElementById('booking-form-section')?.scrollIntoView({ behavior: 'smooth' });
  }

  async function handleCancel(id) {
    setError('');
    try {
      await cancelBooking(id);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to cancel booking.');
    }
  }

  function openReviewForm(bookingId) {
    setReviewError('');
    setReviewForm({ rating: '5', comment: '' });
    setReviewingId(bookingId);
  }

  function closeReviewForm() {
    setReviewingId(null);
    setReviewError('');
  }

  async function handleReviewSubmit(e, bookingId) {
    e.preventDefault();
    setReviewError('');
    setReviewSubmitting(true);
    try {
      await submitReview(bookingId, {
        rating: Number(reviewForm.rating),
        comment: reviewForm.comment.trim() || null,
      });
      setReviewedIds((prev) => new Set(prev).add(bookingId));
      setReviewingId(null);
    } catch (err) {
      setReviewError(err.response?.data?.message || 'Failed to submit review.');
    } finally {
      setReviewSubmitting(false);
    }
  }

  const upcomingCount = bookings.filter((b) => b.status === 'CONFIRMED').length;

  function formatRating(tutor) {
    return tutor.averageRating != null
      ? `${tutor.averageRating.toFixed(1)}★ (${tutor.reviewCount})`
      : 'No ratings yet';
  }

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
          <div className="card-value">{upcomingCount}</div>
        </div>
      </div>

      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      <section style={{ marginTop: 32 }}>
        <h2>Find a tutor</h2>
        <form onSubmit={handleSearch} style={{ display: 'grid', gap: 12, maxWidth: 480 }}>
          <label>
            Subject
            <select
              value={searchForm.subjectId}
              onChange={(e) => updateSearchForm('subjectId', e.target.value)}
            >
              <option value="">Any subject</option>
              {subjects.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </label>

          <label>
            Day
            <select
              value={searchForm.dayOfWeek}
              onChange={(e) => updateSearchForm('dayOfWeek', e.target.value)}
            >
              <option value="">Any day</option>
              {DAYS_OF_WEEK.map((d) => (
                <option key={d} value={d}>{d.charAt(0) + d.slice(1).toLowerCase()}</option>
              ))}
            </select>
          </label>

          <div style={{ display: 'flex', gap: 12 }}>
            <label style={{ flex: 1 }}>
              Start time
              <input
                type="time"
                value={searchForm.startTime}
                onChange={(e) => updateSearchForm('startTime', e.target.value)}
              />
            </label>
            <label style={{ flex: 1 }}>
              End time
              <input
                type="time"
                value={searchForm.endTime}
                onChange={(e) => updateSearchForm('endTime', e.target.value)}
              />
            </label>
          </div>

          <div style={{ display: 'flex', gap: 8 }}>
            <button type="submit" className="btn btn-primary" disabled={searching}>
              {searching ? 'Searching…' : 'Search'}
            </button>
            {searchResults !== null && (
              <button type="button" onClick={clearSearch} className="btn">Clear</button>
            )}
          </div>
        </form>

        {searchError && <p style={{ color: 'crimson' }}>{searchError}</p>}

        {searchResults !== null && (
          searchResults.length === 0 ? (
            <p style={{ marginTop: 16 }}>No tutors match those filters.</p>
          ) : (
            <ul style={{ listStyle: 'none', padding: 0, display: 'grid', gap: 12, marginTop: 16 }}>
              {searchResults.map((t) => (
                <li key={t.id} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 16 }}>
                  <div>
                    <strong>{t.fullName}</strong> — {t.department || 'N/A'} · {formatRating(t)}
                    <div style={{ color: '#555', fontSize: 14 }}>
                      {t.subjects.map((s) => s.subjectName).join(', ')}
                    </div>
                  </div>
                  <button onClick={() => selectTutorForBooking(t.id)} className="btn btn-primary" style={{ whiteSpace: 'nowrap' }}>
                    Book this tutor
                  </button>
                </li>
              ))}
            </ul>
          )
        )}
      </section>

      <section id="booking-form-section" style={{ marginTop: 32 }}>
        <h2>Request a session</h2>
        <form onSubmit={handleSubmit} style={{ display: 'grid', gap: 12, maxWidth: 480 }}>
          <label>
            Tutor
            <select
              value={form.tutorProfileId}
              onChange={(e) => updateForm('tutorProfileId', e.target.value)}
              required
            >
              <option value="" disabled>Select a tutor</option>
              {tutors.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.fullName} — {t.department || 'N/A'} · {formatRating(t)}
                </option>
              ))}
            </select>
          </label>

          {selectedTutor && (
            <p style={{ margin: 0, color: '#555' }}>
              Rating: {formatRating(selectedTutor)}
            </p>
          )}

          <label>
            Subject
            <select
              value={form.subjectId}
              onChange={(e) => updateForm('subjectId', e.target.value)}
              required
              disabled={!selectedTutor}
            >
              <option value="" disabled>Select a subject</option>
              {selectedTutor?.subjects.map((s) => (
                <option key={s.subjectId} value={s.subjectId}>
                  {s.subjectName} ({s.proficiencyLevel})
                </option>
              ))}
            </select>
          </label>

          <label>
            Date
            <input
              type="date"
              value={form.sessionDate}
              onChange={(e) => updateForm('sessionDate', e.target.value)}
              required
            />
          </label>

          <div style={{ display: 'flex', gap: 12 }}>
            <label style={{ flex: 1 }}>
              Start time
              <input
                type="time"
                value={form.startTime}
                onChange={(e) => updateForm('startTime', e.target.value)}
                required
              />
            </label>
            <label style={{ flex: 1 }}>
              End time
              <input
                type="time"
                value={form.endTime}
                onChange={(e) => updateForm('endTime', e.target.value)}
                required
              />
            </label>
          </div>

          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? 'Submitting…' : 'Request session'}
          </button>
        </form>
      </section>

      <section style={{ marginTop: 32 }}>
        <h2>My bookings</h2>
        {loading ? (
          <p>Loading…</p>
        ) : bookings.length === 0 ? (
          <p>No bookings yet.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={{ textAlign: 'left' }}>Tutor</th>
                <th style={{ textAlign: 'left' }}>Subject</th>
                <th style={{ textAlign: 'left' }}>Date</th>
                <th style={{ textAlign: 'left' }}>Time</th>
                <th style={{ textAlign: 'left' }}>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <>
                  <tr key={b.id}>
                    <td>{b.tutorName}</td>
                    <td>{b.subjectName}</td>
                    <td>{b.sessionDate}</td>
                    <td>{b.startTime}–{b.endTime}</td>
                    <td>{b.status}</td>
                    <td style={{ display: 'flex', gap: 8 }}>
                      {(b.status === 'PENDING' || b.status === 'CONFIRMED') && (
                        <button onClick={() => handleCancel(b.id)} className="btn">Cancel</button>
                      )}
                      {b.status === 'COMPLETED' && !reviewedIds.has(b.id) && (
                        reviewingId === b.id ? (
                          <button onClick={closeReviewForm} className="btn">Close</button>
                        ) : (
                          <button onClick={() => openReviewForm(b.id)} className="btn btn-primary">
                            Leave a review
                          </button>
                        )
                      )}
                      {b.status === 'COMPLETED' && reviewedIds.has(b.id) && (
                        <span style={{ color: '#555' }}>Reviewed ✓</span>
                      )}
                    </td>
                  </tr>
                  {reviewingId === b.id && (
                    <tr key={`${b.id}-review`}>
                      <td colSpan={6}>
                        <form
                          onSubmit={(e) => handleReviewSubmit(e, b.id)}
                          style={{ display: 'grid', gap: 8, maxWidth: 420, margin: '8px 0 16px' }}
                        >
                          <label>
                            Rating
                            <select
                              value={reviewForm.rating}
                              onChange={(e) => setReviewForm((p) => ({ ...p, rating: e.target.value }))}
                            >
                              {[5, 4, 3, 2, 1].map((n) => (
                                <option key={n} value={n}>{n} — {'★'.repeat(n)}{'☆'.repeat(5 - n)}</option>
                              ))}
                            </select>
                          </label>
                          <label>
                            Feedback (optional)
                            <textarea
                              value={reviewForm.comment}
                              onChange={(e) => setReviewForm((p) => ({ ...p, comment: e.target.value }))}
                              rows={3}
                              maxLength={1000}
                            />
                          </label>
                          {reviewError && <p style={{ color: 'crimson', margin: 0 }}>{reviewError}</p>}
                          <button type="submit" className="btn btn-primary" disabled={reviewSubmitting}>
                            {reviewSubmitting ? 'Submitting…' : 'Submit review'}
                          </button>
                        </form>
                      </td>
                    </tr>
                  )}
                </>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </main>
  );
}
