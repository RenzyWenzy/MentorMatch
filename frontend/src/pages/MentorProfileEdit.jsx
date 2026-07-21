import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchMyTutorProfile, saveMyTutorProfile } from '../api/tutorProfile';
import { fetchSubjects } from '../api/subjects';
import SubjectProficiencyPicker from '../components/SubjectProficiencyPicker';

const BIO_MAX = 1000;

export default function MentorProfileEdit() {
  const navigate = useNavigate();

  const [subjects, setSubjects] = useState([]);
  const [bio, setBio] = useState('');
  const [subjectRows, setSubjectRows] = useState([]); // [{ subjectId, proficiencyLevel }]

  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [isNewProfile, setIsNewProfile] = useState(false);

  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [savedMessage, setSavedMessage] = useState('');

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
        } catch (err) {
          // No profile yet is expected for a mentor's first visit — start blank
          // instead of surfacing it as a load error.
          if (err.response?.status === 400) {
            if (cancelled) return;
            setIsNewProfile(true);
            setBio('');
            setSubjectRows([]);
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
      await saveMyTutorProfile({ bio, subjects: subjectRows });
      setIsNewProfile(false);
      setSavedMessage('Profile saved.');
    } catch (err) {
      setFormError(err.response?.data?.message || 'Could not save your profile.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <main className="dashboard-main">
        <div className="loading-state">Loading your profile…</div>
      </main>
    );
  }

  return (
    <main className="dashboard-main">
      <div className="dashboard-header">
        <h1>Edit my profile</h1>
        <p>
          {isNewProfile
            ? "You don't have a tutor profile yet — fill this out so students can find you."
            : 'Update your bio and the subjects you tutor.'}
        </p>
      </div>

      {loadError && <div className="form-error-banner">{loadError}</div>}

      {!loadError && (
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
      )}
    </main>
  );
}
