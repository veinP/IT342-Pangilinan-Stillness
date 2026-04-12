import { useState, type FormEvent, type ChangeEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/RegisterPage.css';

/* ── Icons ──────────────────────────────────────────────────── */
const GoogleIcon = () => (
  <svg width="22" height="22" viewBox="0 0 22 22" fill="none" aria-hidden="true">
    <path fill="#4285F4" d="M21.56 11.25c0-.75-.07-1.5-.19-2.2H11v4.17h5.97a5.1 5.1 0 0 1-2.21 3.35v2.78h3.58c2.09-1.92 3.22-4.77 3.22-8.1z"/>
    <path fill="#34A853" d="M11 22c2.97 0 5.46-.98 7.28-2.66l-3.58-2.78c-.98.66-2.24 1.06-3.7 1.06-2.85 0-5.27-1.92-6.13-4.51H1.18v2.87A11 11 0 0 0 11 22z"/>
    <path fill="#FBBC05" d="M4.87 13.11A6.6 6.6 0 0 1 4.52 11c0-.73.13-1.45.35-2.11V5.97H1.18A11 11 0 0 0 0 11c0 1.77.43 3.44 1.18 4.97l3.69-2.86z"/>
    <path fill="#EA4335" d="M11 4.37c1.61 0 3.05.55 4.19 1.63l3.14-3.14C16.45 1 13.96 0 11 0A11 11 0 0 0 1.18 5.97l3.69 2.92C5.73 6.3 8.15 4.37 11 4.37z"/>
  </svg>
);

const EyeIcon = ({ open }: { open: boolean }) =>
  open ? (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
      <circle cx="12" cy="12" r="3"/>
    </svg>
  ) : (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
      <line x1="1" y1="1" x2="23" y2="23"/>
    </svg>
  );

/* ── Types ──────────────────────────────────────────────────── */
interface FormState {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: 'ROLE_USER' | 'ROLE_INSTRUCTOR';
}
interface FormErrors {
  fullName?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
  role?: string;
  general?: string;
}

/* ── Password Strength Calculation ────────────────────── */
function calculatePasswordStrength(password: string): number {
  if (!password) return 0;

  let score = 0;
  const length = password.length;

  // Length: 0-25 points
  if (length >= 8) score += 5;
  if (length >= 12) score += 5;
  if (length >= 16) score += 5;
  if (length >= 20) score += 5;
  if (length >= 24) score += 5;

  // Uppercase: 0-25 points
  if (/[A-Z]/.test(password)) score += 12.5;
  if ((password.match(/[A-Z]/g) || []).length >= 2) score += 12.5;

  // Lowercase: 0-25 points
  if (/[a-z]/.test(password)) score += 12.5;
  if ((password.match(/[a-z]/g) || []).length >= 2) score += 12.5;

  // Numbers: 0-12.5 points
  if (/\d/.test(password)) score += 12.5;

  // Special characters: 0-12.5 points
  if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>?]/.test(password)) score += 12.5;

  return Math.min(score, 100);
}

function validate(v: FormState): FormErrors {
  const e: FormErrors = {};
  if (!v.fullName.trim())       e.fullName = 'Full name is required.';
  if (!v.email.trim())          e.email = 'Email is required.';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.email))
                                e.email = 'Enter a valid email address.';
  if (!v.password)              e.password = 'Password is required.';
  else if (v.password.length < 8)
                                e.password = 'Min. 8 characters, 1 uppercase, 1 digit.';
  else if (!/[A-Z]/.test(v.password))
                                e.password = 'Must include at least 1 uppercase letter.';
  else if (!/\d/.test(v.password))
                                e.password = 'Must include at least 1 digit.';
  if (!v.confirmPassword)       e.confirmPassword = 'Please confirm your password.';
  else if (v.password !== v.confirmPassword)
                                e.confirmPassword = 'Passwords do not match.';
  if (!v.role)                  e.role = 'Please choose an account type.';
  return e;
}

/* ── Component ──────────────────────────────────────────────── */
export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8080/api/v1/oauth2/authorization/google';
  };

  const [values, setValues] = useState<FormState>({
    fullName: '', email: '', password: '', confirmPassword: '', role: 'ROLE_USER',
  });
  const [errors, setErrors]       = useState<FormErrors>({});
  const [showPw, setShowPw]       = useState(false);
  const [showCfm, setShowCfm]     = useState(false);
  const [loading, setLoading]     = useState(false);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setValues(prev => ({ ...prev, [name]: value }));
    if (errors[name as keyof FormErrors])
      setErrors(prev => ({ ...prev, [name]: undefined }));
  };

  const handleRoleSelect = (role: 'ROLE_USER' | 'ROLE_INSTRUCTOR') => {
    setValues(prev => ({ ...prev, role }));
    if (errors.role) setErrors(prev => ({ ...prev, role: undefined }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const errs = validate(values);
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setLoading(true);
    try {
      const user = await register({
        fullName: values.fullName,
        email: values.email,
        password: values.password,
        confirmPassword: values.confirmPassword,
        role: values.role,
      });
      navigate(user.role === 'ROLE_INSTRUCTOR' ? '/admin/sessions' : '/sessions', { replace: true });
    } catch (err: any) {
      // Extract user-friendly error message
      let errorMessage = 'Registration failed. Please try again.';
      
      if (err?.response?.data?.error?.message) {
        errorMessage = err.response.data.error.message;
      } else if (err instanceof Error) {
        errorMessage = err.message;
      }
      
      setErrors({
        general: errorMessage
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="reg-page">

      {/* Logo */}
      <Link to="/" className="reg-logo">StillNess</Link>

      {/* Card */}
      <div className="reg-card">
        <h1 className="reg-card__title">Create Your Account</h1>
        <p  className="reg-card__sub">Start your wellness journey today</p>

        {errors.general && (
          <div className="reg-error-banner">{errors.general}</div>
        )}

        <form onSubmit={handleSubmit} noValidate>

          {/* Full Name */}
          <div className="reg-field">
            <label className="reg-field__label" htmlFor="fullName">Full Name</label>
            <div className="reg-field__wrap">
              <input
                id="fullName" name="fullName" type="text"
                className={`reg-field__input reg-field__input--plain${errors.fullName ? ' reg-field__input--error' : ''}`}
                placeholder="Enter your full name"
                autoComplete="name"
                value={values.fullName}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
            {errors.fullName && (
              <span className="reg-field__error-msg">{errors.fullName}</span>
            )}
          </div>

          {/* Email */}
          <div className="reg-field">
            <label className="reg-field__label" htmlFor="email">Email Address</label>
            <div className="reg-field__wrap">
              <input
                id="email" name="email" type="email"
                className={`reg-field__input reg-field__input--plain${errors.email ? ' reg-field__input--error' : ''}`}
                placeholder="Enter your email address"
                autoComplete="email"
                value={values.email}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
            {errors.email && (
              <span className="reg-field__error-msg">{errors.email}</span>
            )}
          </div>

          <div className="reg-field">
            <span className="reg-field__label">Account Type</span>
            <div className="reg-role-grid" role="radiogroup" aria-label="Account type">
              <label
                className={`reg-role-card${values.role === 'ROLE_USER' ? ' reg-role-card--active' : ''}`}
                onClick={() => handleRoleSelect('ROLE_USER')}
              >
                <input
                  className="reg-role-card__input"
                  type="radio"
                  name="role"
                  value="ROLE_USER"
                  checked={values.role === 'ROLE_USER'}
                  onChange={handleChange}
                  disabled={loading}
                />
                <span className="reg-role-card__content">
                  <span className="reg-role-card__title">User</span>
                  <span className="reg-role-card__copy">Book sessions and manage your wellness schedule.</span>
                </span>
              </label>

              <label
                className={`reg-role-card${values.role === 'ROLE_INSTRUCTOR' ? ' reg-role-card--active' : ''}`}
                onClick={() => handleRoleSelect('ROLE_INSTRUCTOR')}
              >
                <input
                  className="reg-role-card__input"
                  type="radio"
                  name="role"
                  value="ROLE_INSTRUCTOR"
                  checked={values.role === 'ROLE_INSTRUCTOR'}
                  onChange={handleChange}
                  disabled={loading}
                />
                <span className="reg-role-card__content">
                  <span className="reg-role-card__title">Instructor</span>
                  <span className="reg-role-card__copy">Manage sessions, attendees, and payment records.</span>
                </span>
              </label>
            </div>
            {errors.role && (
              <span className="reg-field__error-msg">{errors.role}</span>
            )}
          </div>

          {/* Password */}
          <div className="reg-field">
            <label className="reg-field__label" htmlFor="password">Password</label>
            <div className="reg-field__wrap">
              <input
                id="password" name="password"
                type={showPw ? 'text' : 'password'}
                className={`reg-field__input${errors.password ? ' reg-field__input--error' : ''}`}
                placeholder="Enter your password"
                autoComplete="new-password"
                value={values.password}
                onChange={handleChange}
                disabled={loading}
              />
              <button
                type="button" className="reg-field__eye"
                onClick={() => setShowPw(v => !v)}
                aria-label={showPw ? 'Hide password' : 'Show password'}
              >
                <EyeIcon open={showPw} />
              </button>
            </div>
            {values.password && (
              <div className="reg-strength-meter">
                <div className="reg-strength-meter__bar-wrapper">
                  <div
                    className="reg-strength-meter__bar"
                    style={{
                      width: `${calculatePasswordStrength(values.password)}%`,
                      backgroundColor:
                        calculatePasswordStrength(values.password) > 80 ? '#22c55e' :
                        calculatePasswordStrength(values.password) > 60 ? '#eab308' :
                        calculatePasswordStrength(values.password) > 30 ? '#f97316' :
                        '#ef4444'
                    }}
                  />
                </div>
              </div>
            )}
            {errors.password
              ? <span className="reg-field__error-msg">{errors.password}</span>
              : <span className="reg-field__hint">Min. 8 characters, 1 uppercase, 1 digit</span>
            }
          </div>

          {/* Confirm Password */}
          <div className="reg-field">
            <label className="reg-field__label" htmlFor="confirmPassword">Confirm Password</label>
            <div className="reg-field__wrap">
              <input
                id="confirmPassword" name="confirmPassword"
                type={showCfm ? 'text' : 'password'}
                className={`reg-field__input${errors.confirmPassword ? ' reg-field__input--error' : ''}`}
                placeholder="Re-enter your password"
                autoComplete="new-password"
                value={values.confirmPassword}
                onChange={handleChange}
                disabled={loading}
              />
              <button
                type="button" className="reg-field__eye"
                onClick={() => setShowCfm(v => !v)}
                aria-label={showCfm ? 'Hide password' : 'Show password'}
              >
                <EyeIcon open={showCfm} />
              </button>
            </div>
            {errors.confirmPassword && (
              <span className="reg-field__error-msg">{errors.confirmPassword}</span>
            )}
          </div>

          {/* Submit */}
          <button type="submit" className="reg-submit" disabled={loading}>
            {loading ? <span className="reg-spinner" /> : 'Create Account'}
          </button>

        </form>

        {/* Divider */}
        <div className="reg-divider">
          <span className="reg-divider__line" />
          <span className="reg-divider__text">or</span>
          <span className="reg-divider__line" />
        </div>

        {/* Google */}
        <button type="button" className="reg-google" onClick={handleGoogleLogin}>
          <GoogleIcon />
          Sign in with Google
        </button>
      </div>

      {/* Footer */}
      <p className="reg-footer">
        Already have an account?
        <Link to="/login">Login</Link>
      </p>

    </div>
  );
}