import { useEffect, useState } from 'react';
import {
  fetchPendingTutorProfiles,
  approveTutorProfile,
  rejectTutorProfile,
} from '../api/tutorProfile';

export default function TutorApprovalQueue() {
  const [profiles, setProfiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [pendingActionId, setPendingActionId] = useState(null);
  const [reasonDraftId, setReasonDraftId] = useState(null);
  const [reasonText, setReasonText] = useState('');

  const load = () => {
    setLoading(true);
    setError('');
    fetchPendingTutorProfiles()
      .then(setProfiles)
      .catch((err) => setError(err.response?.data?.message || 'Could not load pending tutor profiles.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const handleApprove = async (id) => {
    setActionError('');
    setPendingActionId(id);
    try {
      await approveTutorProfile(id);
      setProfiles((prev) => prev.filter((p) => p.id !== id));
    } catch (err) {
      setActionError(err.response?.data?.message || 'Could not approve this profile.');
    } finally {
      setPendingActionId(null);
    }
  };

  const openRejectDraft = (id) => {
    setActionError('');
    setReasonDraftId(id);
    setReasonText('');
  };

  const cancelRejectDraft = () => {
    setReasonDraftId(null);
    setReasonText('');
  };

  const confirmReject = async (id) => {
    setActionError('');
    setPendingActionId(id);
    try {
      await rejectTutorProfile(id, reasonText.trim() || null);
      setProfiles((prev) => prev.filter((p) => p.id !== id));
      setReasonDraftId(null);
      setReasonText('');
    } catch (err) {
      setActionError(err.response?.data?.message || 'Could not reject this profile.');
    } finally {
      setPendingActionId(null);
    }
  };

  return (
    <section style={{ marginBottom: 32 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
        <h2 style={{ fontSize: '1.05rem', margin: 0 }}>Tutor profile approvals</h2>
        {profiles.length > 0 && (
          <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
            {profiles.length} awaiting review
          </span>
        )}
      </div>

      {loading && <div className="loading-state">Loading pending profiles…</div>}
      {error && <div className="form-error-banner">{error}</div>}
      {actionError && <div className="form-error-banner">{actionError}</div>}

      {!loading && !error && (
        profiles.length === 0 ? (
          <div className="empty-state">No tutor profiles are waiting for review.</div>
        ) : (
          <div style={{ display: 'grid', gap: 12 }}>
            {profiles.map((p) => {
              const isBusy = pendingActionId === p.id;
              const isDraftingReason = reasonDraftId === p.id;
              return (
                <div key={p.id} className="card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, flexWrap: 'wrap' }}>
                    <div>
                      <strong>{p.fullName}</strong>
                      <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
                        {p.email} · {p.department || '—'}
                      </div>
                    </div>
                    {!isDraftingReason && (
                      <div style={{ display: 'flex', gap: 8, alignSelf: 'flex-start' }}>
                        <button
                          type="button"
                          className="btn btn-primary"
                          onClick={() => handleApprove(p.id)}
                          disabled={isBusy}
                        >
                          {isBusy ? 'Working…' : 'Approve'}
                        </button>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          style={{ color: 'var(--color-danger)', border: '1px solid var(--color-border)' }}
                          onClick={() => openRejectDraft(p.id)}
                          disabled={isBusy}
                        >
                          Reject
                        </button>
                      </div>
                    )}
                  </div>

                  {p.bio && (
                    <p style={{ margin: '10px 0 0', fontSize: '0.85rem' }}>{p.bio}</p>
                  )}

                  {p.subjects?.length > 0 && (
                    <div style={{ marginTop: 10, display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                      {p.subjects.map((s) => (
                        <span
                          key={s.subjectId}
                          style={{
                            fontSize: '0.75rem',
                            padding: '3px 8px',
                            borderRadius: 999,
                            border: '1px solid var(--color-border)',
                            color: 'var(--color-text-muted)',
                          }}
                        >
                          {s.subjectName} · {s.proficiencyLevel.charAt(0) + s.proficiencyLevel.slice(1).toLowerCase()}
                        </span>
                      ))}
                    </div>
                  )}

                  {isDraftingReason && (
                    <div style={{ marginTop: 12, borderTop: '1px solid var(--color-border)', paddingTop: 12 }}>
                      <label htmlFor={`reject-reason-${p.id}`} style={{ fontSize: '0.8rem', display: 'block', marginBottom: 6 }}>
                        Reason (optional, shared with the mentor)
                      </label>
                      <textarea
                        id={`reject-reason-${p.id}`}
                        rows={2}
                        value={reasonText}
                        onChange={(e) => setReasonText(e.target.value)}
                        placeholder="e.g. Bio needs more detail about your teaching background."
                        style={{
                          width: '100%',
                          padding: '8px 10px',
                          border: '1px solid var(--color-border)',
                          borderRadius: 8,
                          fontSize: '0.85rem',
                          fontFamily: 'inherit',
                          resize: 'vertical',
                        }}
                      />
                      <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          style={{ color: 'var(--color-danger)', border: '1px solid var(--color-border)' }}
                          onClick={() => confirmReject(p.id)}
                          disabled={isBusy}
                        >
                          {isBusy ? 'Working…' : 'Confirm reject'}
                        </button>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          style={{ color: 'var(--color-text)', border: '1px solid var(--color-border)' }}
                          onClick={cancelRejectDraft}
                          disabled={isBusy}
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )
      )}
    </section>
  );
}
