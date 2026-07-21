import { useState } from 'react';
import { fetchAdminReport } from '../api/reports';

// NOTE: toISOString() always converts to UTC, which can be a day behind/ahead
// of the user's actual local calendar date (e.g. UTC+8 in the early morning
// local time is still "yesterday" in UTC). Build the ISO string from local
// date components instead, so "today" really means today for this user.
function toIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function isoToday() {
  return toIsoDate(new Date());
}

function firstOfMonth() {
  const d = new Date();
  return toIsoDate(new Date(d.getFullYear(), d.getMonth(), 1));
}

export default function AdminReport() {
  const [startDate, setStartDate] = useState(firstOfMonth());
  const [endDate, setEndDate] = useState(isoToday());
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleGenerate = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await fetchAdminReport(startDate, endDate);
      setReport(data);
    } catch (err) {
      setReport(null);
      setError(err.response?.data?.message || 'Could not generate report.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section style={{ marginBottom: 32 }}>
      <h2 style={{ fontSize: '1.05rem', marginBottom: 12 }}>Tutoring activity report</h2>

      <form
        onSubmit={handleGenerate}
        style={{ display: 'flex', alignItems: 'flex-end', gap: 12, marginBottom: 16, flexWrap: 'wrap' }}
      >
        <div className="form-field" style={{ marginBottom: 0 }}>
          <label htmlFor="report-start">Start date</label>
          <input
            id="report-start"
            type="date"
            value={startDate}
            max={endDate}
            onChange={(e) => setStartDate(e.target.value)}
            required
          />
        </div>
        <div className="form-field" style={{ marginBottom: 0 }}>
          <label htmlFor="report-end">End date</label>
          <input
            id="report-end"
            type="date"
            value={endDate}
            min={startDate}
            onChange={(e) => setEndDate(e.target.value)}
            required
          />
        </div>
        <button className="btn btn-primary" type="submit" disabled={loading}>
          {loading ? 'Generating…' : 'Generate report'}
        </button>
      </form>

      {error && <div className="form-error-banner">{error}</div>}

      {report && (
        <>
          <div className="card-grid" style={{ marginBottom: 16 }}>
            <div className="card">
              <h3>Total sessions</h3>
              <div className="card-value">{report.totalSessions}</div>
            </div>
            <div className="card">
              <h3>Completed</h3>
              <div className="card-value">{report.completedCount}</div>
            </div>
            <div className="card">
              <h3>Confirmed</h3>
              <div className="card-value">{report.confirmedCount}</div>
            </div>
            <div className="card">
              <h3>Active tutors</h3>
              <div className="card-value">{report.activeTutorsCount}</div>
            </div>
          </div>

          <table className="data-table">
            <tbody>
              <tr>
                <td>Date range</td>
                <td>{report.startDate} – {report.endDate}</td>
              </tr>
              <tr>
                <td>Pending requests</td>
                <td>{report.pendingCount}</td>
              </tr>
              <tr>
                <td>Declined requests</td>
                <td>{report.declinedCount}</td>
              </tr>
              <tr>
                <td>Cancelled sessions</td>
                <td>{report.cancelledCount}</td>
              </tr>
              <tr>
                <td>New reviews submitted</td>
                <td>{report.newReviewsCount}</td>
              </tr>
              <tr>
                <td>Average rating (in range)</td>
                <td>{report.averageRating != null ? report.averageRating.toFixed(1) : '—'}</td>
              </tr>
            </tbody>
          </table>
        </>
      )}

      {!report && !error && !loading && (
        <div className="empty-state">Pick a date range and generate a report to see activity stats.</div>
      )}
    </section>
  );
}
