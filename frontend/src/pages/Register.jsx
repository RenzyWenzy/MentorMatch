import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardPathForRole } from '../components/ProtectedRoute';

const initialForm = {
  fullName: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'STUDENT',
  studentNumber: '',
  program: '',
  expertise: '',
  department: ''
};
const PROGRAM_OPTIONS = [
  'Bachelor of Science in Information Technology',
  'Bachelor of Science in Civil Engineering'
];

// NOTE: ADMIN is intentionally not offered here — admin accounts are
// provisioned separately, not through public self-registration.
export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const setRole = (role) => setForm((f) => ({ ...f, role }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setFieldErrors({});

    if (form.password !== form.confirmPassword) {
      setFieldErrors({ confirmPassword: 'Passwords do not match.' });
      return;
    }

    setSubmitting(true);
    try {
      const user = await register(form);
      navigate(dashboardPathForRole(user.role));
    } catch (err) {
      const data = err.response?.data;
      if (data?.fieldErrors) {
        setFieldErrors(data.fieldErrors);
      } else {
        setFormError(data?.message || 'Registration failed. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="centered-page">
      <div className="auth-card" style={{ maxWidth: 480 }}>
        <h1>Create your account</h1>
        <p className="auth-subtitle">Join CTMMS as a student or mentor.</p>

        {formError && <div className="form-error-banner">{formError}</div>}

        <div className="role-toggle">
          <button
            type="button"
            className={form.role === 'STUDENT' ? 'active' : ''}
            onClick={() => setRole('STUDENT')}
          >
            Student
          </button>
          <button
            type="button"
            className={form.role === 'MENTOR' ? 'active' : ''}
            onClick={() => setRole('MENTOR')}
          >
            Mentor
          </button>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="fullName">Full name</label>
            <input id="fullName" name="fullName" value={form.fullName} onChange={handleChange} required />
            {fieldErrors.fullName && <div className="field-error">{fieldErrors.fullName}</div>}
          </div>

          <div className="form-field">
            <label htmlFor="email">Email</label>
            <input id="email" name="email" type="email" value={form.email} onChange={handleChange} required />
            {fieldErrors.email && <div className="field-error">{fieldErrors.email}</div>}
          </div>

          <div className="form-field">
            <label htmlFor="password">Password</label>
            <input id="password" name="password" type="password" value={form.password} onChange={handleChange} required />
            {fieldErrors.password && <div className="field-error">{fieldErrors.password}</div>}
          </div>

          <div className="form-field">
            <label htmlFor="confirmPassword">Confirm password</label>
            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              value={form.confirmPassword}
              onChange={handleChange}
              required
            />
            {fieldErrors.confirmPassword && <div className="field-error">{fieldErrors.confirmPassword}</div>}
          </div>

          {form.role === 'STUDENT' ? (
            <>
              <div className="form-field">
                <label htmlFor="studentNumber">Student number</label>
                <input id="studentNumber" name="studentNumber" value={form.studentNumber} onChange={handleChange} />
                {fieldErrors.studentNumber && <div className="field-error">{fieldErrors.studentNumber}</div>}
              </div>
              <div className="form-field">
                <label htmlFor="program">Program</label>
                <select id="program" name="program" value={form.program} onChange={handleChange}>
                    <option value="">Select a program…</option>
                    {PROGRAM_OPTIONS.map((option) => (
                    <option key={option} value={option}>
                        {option}
                    </option>
                    ))}
                </select>
                {fieldErrors.program && <div className="field-error">{fieldErrors.program}</div>}
            </div>
            </>
          ) : (
            <>
              <div className="form-field">
                <label htmlFor="expertise">Area of expertise</label>
                <input id="expertise" name="expertise" value={form.expertise} onChange={handleChange} />
                {fieldErrors.expertise && <div className="field-error">{fieldErrors.expertise}</div>}
              </div>
              <div className="form-field">
                <label htmlFor="department">Department</label>
                <input id="department" name="department" value={form.department} onChange={handleChange} />
                {fieldErrors.department && <div className="field-error">{fieldErrors.department}</div>}
              </div>
            </>
          )}

          <button className="btn btn-primary" type="submit" disabled={submitting} style={{ width: '100%' }}>
            {submitting ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
}
