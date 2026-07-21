import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchMyTutorProfile, saveMyTutorProfile } from '../api/tutorProfile';
import { fetchSubjects } from '../api/subjects';
import { replaceMyAvailability } from '../api/availability';
import SubjectProficiencyPicker from '../components/SubjectProficiencyPicker';

const BIO_MAX = 1000;
const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

/** BR-002: how each approval state should read and look to the mentor. */
const APPROVAL_STATUS_META = {
  PENDING: {
    label: "Pending review — you're not visible in search yet",
    background: '#fff8e6',
    color: '#8a6116',
    border: '#f3e1ad',
  },
  APPROVED: {
    label: 'Approved — visible to students in search',
    background: '#eaf6ee',
    color: 'var(--color-success, #2e7d32)',
    border: '#cdebd7',
  },
  REJECTED: {
    label: 'Not approved',
    background: '#fdecea',
    color: 'var(--color-danger)',
    border: '#f6cdc9',
  },
};

export default function MentorProfileEdit() {
  const navigate = useNavigate();

  const [subjects, setSubjects] = useState([]);
  const [bio, setBio] = useState('');
  const [subjectRows, setSubjectRows] = useState([]); // [{ subjectId, proficiencyLevel }]
  const [slots, setSlots] = useState([]); // [{ dayOfWeek, startTime, endTime }]

  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [isNewProfile, setIsNewProfile] = useState(false);

  const [approvalStatus, setApprovalStatus] = useState(null);
  const [rejectionReason, setRejectionReason] = useState(null);

  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [savedMessage, setSavedMessage] = useState('');

  const [savingAvailability, setSavingAvailability] = useState(false);
  const [availabilityError, setAvailabilityError] = useState('');
  const [availabilitySaved, setAvailabilitySaved] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      setLoadError('');
      try {
        const catalog = await fetchSubjects();
        if (cancelled) return;
        setSubjects(catalog);

        try {
          const profile = await fetchMyTutorProfile();
          if (cancelled) return;
          setBio(profile.bio || '');
          setSubjectRows(
            (profile.subjects || []).map((s) => ({
              subjectId: s.subjectId,
              proficiencyLevel: s.proficiencyLevel,
            }))
          );
          setSlots(
            (profile.availability || []).map((a) => ({
              dayOfWeek: a.dayOfWeek,
              startTime: a.startTime,
              endTime: a.endTime,
            }))
          );
          setApprovalStatus(profile.approvalStatus || null);
          setRejectionReason(profile.rejectionReason || null);
        } catch (err) {
          // No profile yet is expected for a mentor's first visit — start blank
          // instead of surfacing it as a load error.
          if (err.response?.status === 400) {
            if (cancelled) return;
            setIsNewProfile(true);
            setBio('');
            setSubjectRows([]);
            setSlots([]);
            setApprovalStatus(null);
            setRejectionReason(null);
          } else {
            throw err;
          }
        }
      } catch (err) {
        if (!cancelled) {
          setLoadError(err.response?.data?.message || 'Could not load your profile.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setSavedMessage('');

    if (subjectRows.length === 0) {
      setFormError('Add at least one subject before saving.');
      return;
    }

    setSubmitting(true);
    try {
      const saved = await saveMyTutorProfile({ bio, subjects: subjectRows });
      setIsNewProfile(false);
      setApprovalStatus(saved.approvalStatus || null);
      setRejectionReason(saved.rejectionReason || null);
      setSavedMessage(
        saved.approvalStatus === 'PENDING'
          ? 'Profile saved. An admin will review it before it appears in search.'
          : 'Profile saved.'
      );
    } catch (err) {
      setFormError(err.response?.data?.message || 'Could not save your profile.');
    } finally {
      setSubmitting(false);
    }
  };

  function addSlot() {
    setSlots((prev) => [...prev, { dayOfWeek: 'MONDAY', startTime: '', endTime: '' }]);
  }

  function updateSlot(index, field, value) {
    setSlots((prev) => prev.map((s, i) => (i === index ? { ...s, [field]: value } : s)));
  }

  function removeSlot(index) {
    setSlots((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleSaveAvailability() {
    setAvailabilityError('');
    setAvailabilitySaved('');

    if (isNewProfile) {
      setAvailabilityError('Save your profile (bio and subjects) before setting availability.');
      return;
    }

    setSavingAvailability(true);
    try {
      await replaceMyAvailability(slots);
      setAvailabilitySaved('Availability saved.');
    } catch (err) {
      const fieldErrors = err.response?.data?.fieldErrors;
      const firstFieldError = fieldErrors ? Object.values(fieldErrors)[0] : null;
      setAvailabilityError(firstFieldError || err.response?.data?.message || 'Could not save availability.');
    } finally {
      setSavingAvailability(false);
    }
  }

  if (loading) {
    return (
      <main className="dashboard-main">
        <div className="loading-state">Loading your profile…</div>
      </main>
    );
  }

  const statusMeta = approvalStatus ? APPROVAL_STATUS_META[approvalStatus] : null;

  return (
    <main className="dashboard-main">
      <div className="dashboard-header">
        <h1>Edit my profile</h1>
        <p>
          {isNewProfile
            ? "You don't have a tutor profile yet — fill this out so students can find you."
            : 'Update your bio, subjects, and weekly availability.'}
        </p>
      </div>

      {loadError && <div className="form-error-banner">{loadError}</div>}

      {!loadError && (
        <>
          {statusMeta && (
            <div
              style={{
                background: statusMeta.background,
                color: statusMeta.color,
                border: `1px solid ${statusMeta.border}`,
                borderRadius: 8,
                padding: '10px 14px',
                fontSize: '0.85rem',
                marginBottom: 16,
                maxWidth: 640,
              }}
            >
              <strong>{statusMeta.label}</strong>
              {approvalStatus === 'REJECTED' && rejectionReason && (
                <p style={{ margin: '6px 0 0' }}>{rejectionReason}</p>
              )}
            </div>
          )}

          <div className="card" style={{ maxWidth: 640 }}>
            {formError && <div className="form-error-banner">{formError}</div>}
            {savedMessage && (
              <div
                style={{
                  background: '#eaf6ee',
                  color: 'var(--color-success)',
                  border: '1px solid #cdebd7',
                  borderRadius: 8,
                  padding: '10px 14px',
                  fontSize: '0.85rem',
                  marginBottom: 16,
                }}
              >
                {savedMessage}
              </div>
            )}

            <form onSubmit={handleSubmit} noValidate>
              <div className="form-field">
                <label htmlFor="mentor-bio">Bio</label>
                <textarea
                  id="mentor-bio"
                  name="bio"
                  rows={5}
                  maxLength={BIO_MAX}
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  placeholder="Tell students a bit about your background and how you can help."
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    border: '1px solid var(--color-border)',
                    borderRadius: 8,
                    fontSize: '0.9rem',
                    fontFamily: 'inherit',
                    resize: 'vertical',
                  }}
                />
                <div style={{ textAlign: 'right', fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: 4 }}>
                  {bio.length}/{BIO_MAX}
                </div>
              </div>

              <div className="form-field">
                <label>Subjects &amp; proficiency</label>
                <SubjectProficiencyPicker
                  subjects={subjects}
                  value={subjectRows}
                  onChange={setSubjectRows}
                />
              </div>

              <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                <button className="btn btn-primary" type="submit" disabled={submitting}>
                  {submitting ? 'Saving…' : 'Save profile'}
                </button>
                <button
                  type="button"
                  className="btn btn-ghost"
                  style={{ color: 'var(--color-text)', border: '1px solid var(--color-border)' }}
                  onClick={() => navigate('/dashboard/mentor')}
                >
                  Back to dashboard
                </button>
              </div>
            </form>
          </div>

          <div className="card" style={{ maxWidth: 640, marginTop: 24 }}>
            <h3 style={{ marginTop: 0 }}>Weekly availability</h3>
            <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>
              Set the recurring times you're available for tutoring sessions. Students can only book within these slots.
            </p>

            {availabilityError && <div className="form-error-banner">{availabilityError}</div>}
            {availabilitySaved && (
              <div
                style={{
                  background: '#eaf6ee',
                  color: 'var(--color-success)',
                  border: '1px solid #cdebd7',
                  borderRadius: 8,
                  padding: '10px 14px',
                  fontSize: '0.85rem',
                  marginBottom: 16,
                }}
              >
                {availabilitySaved}
              </div>
            )}

            <div className="form-field">
              {slots.length === 0 && (
                <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>
                  No availability set yet. Add a time slot below.
                </p>
              )}

              <div style={{ display: 'grid', gap: 10 }}>
                {slots.map((slot, i) => (
                  <div key={i} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <select
                      value={slot.dayOfWeek}
                      onChange={(e) => updateSlot(i, 'dayOfWeek', e.target.value)}
                      style={{
                        padding: '8px 10px',
                        border: '1px solid var(--color-border)',
                        borderRadius: 8,
                        fontSize: '0.85rem',
                      }}
                    >
                      {DAYS.map((d) => (
                        <option key={d} value={d}>{d.charAt(0) + d.slice(1).toLowerCase()}</option>
                      ))}
                    </select>
                    <input
                      type="time"
                      value={slot.startTime}
                      onChange={(e) => updateSlot(i, 'startTime', e.target.value)}
                      required
                      style={{
                        padding: '8px 10px',
                        border: '1px solid var(--color-border)',
                        borderRadius: 8,
                        fontSize: '0.85rem',
                      }}
                    />
                    <span style={{ color: 'var(--color-text-muted)' }}>to</span>
                    <input
                      type="time"
                      value={slot.endTime}
                      onChange={(e) => updateSlot(i, 'endTime', e.target.value)}
                      required
                      style={{
                        padding: '8px 10px',
                        border: '1px solid var(--color-border)',
                        borderRadius: 8,
                        fontSize: '0.85rem',
                      }}
                    />
                    <button
                      type="button"
                      onClick={() => removeSlot(i)}
                      className="btn btn-ghost"
                      style={{ color: 'var(--color-text)', border: '1px solid var(--color-border)' }}
                    >
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            </div>

            <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
              <button type="button" onClick={addSlot} className="btn btn-ghost" style={{ color: 'var(--color-text)', border: '1px solid var(--color-border)' }}>
                + Add time slot
              </button>
              <button
                type="button"
                onClick={handleSaveAvailability}
                className="btn btn-primary"
                disabled={savingAvailability}
              >
                {savingAvailability ? 'Saving…' : 'Save availability'}
              </button>
            </div>
          </div>
        </>
      )}
    </main>
  );
}
