import { useEffect, useState } from 'react';
import { fetchSubjects, createSubject, updateSubject, deleteSubject } from '../api/subjects';

const emptyForm = { name: '', description: '' };

export default function SubjectManager() {
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  const loadSubjects = () => {
    setLoading(true);
    setError('');
    fetchSubjects()
      .then(setSubjects)
      .catch((err) => setError(err.response?.data?.message || 'Could not load subjects.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadSubjects();
  }, []);

  const handleChange = (e) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  };

  const startEdit = (subject) => {
    setEditingId(subject.id);
    setForm({ name: subject.name, description: subject.description || '' });
    setFormError('');
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
    setFormError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      if (editingId) {
        const updated = await updateSubject(editingId, form);
        setSubjects((prev) => prev.map((s) => (s.id === editingId ? updated : s)));
      } else {
        const created = await createSubject(form);
        setSubjects((prev) => [...prev, created]);
      }
      cancelEdit();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Could not save subject.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this subject? This cannot be undone.')) return;
    try {
      await deleteSubject(id);
      setSubjects((prev) => prev.filter((s) => s.id !== id));
      if (editingId === id) cancelEdit();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not delete subject.');
    }
  };

  return (
    <section style={{ marginBottom: 32 }}>
      <h2 style={{ fontSize: '1.05rem', marginBottom: 12 }}>Subject catalog</h2>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
        <div>
          {loading && <div className="loading-state">Loading subjects…</div>}
          {error && <div className="form-error-banner">{error}</div>}

          {!loading && !error && (
            subjects.length === 0 ? (
              <div className="empty-state">No subjects yet — add one to get started.</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {subjects.map((s) => (
                    <tr key={s.id}>
                      <td>{s.name}</td>
                      <td>{s.description || '—'}</td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          style={{ color: 'var(--color-primary)', border: '1px solid var(--color-border)', padding: '4px 10px', fontSize: '0.78rem', marginRight: 6 }}
                          onClick={() => startEdit(s)}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          style={{ color: 'var(--color-danger)', border: '1px solid var(--color-border)', padding: '4px 10px', fontSize: '0.78rem' }}
                          onClick={() => handleDelete(s.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )
          )}
        </div>

        <div className="card" style={{ alignSelf: 'start' }}>
          <h3 style={{ marginBottom: 12 }}>{editingId ? 'Edit subject' : 'Add a subject'}</h3>

          {formError && <div className="form-error-banner">{formError}</div>}

          <form onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="subject-name">Name</label>
              <input id="subject-name" name="name" value={form.name} onChange={handleChange} required />
            </div>
            <div className="form-field">
              <label htmlFor="subject-description">Description</label>
              <input id="subject-description" name="description" value={form.description} onChange={handleChange} />
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-primary" type="submit" disabled={submitting || !form.name.trim()}>
                {submitting ? 'Saving…' : editingId ? 'Save changes' : 'Add subject'}
              </button>
              {editingId && (
                <button type="button" className="btn btn-ghost" style={{ color: 'var(--color-text)', border: '1px solid var(--color-border)' }} onClick={cancelEdit}>
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>
      </div>
    </section>
  );
}
