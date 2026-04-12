import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/LoginPage.css';

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

/* ── Validation ────────────────────────────────────────────── */
interface FormErrors {
  email?: string;
  password?: string;
  general?: string;
}

function validate(email: string, password: string): FormErrors {
  const e: FormErrors = {};
  if (!email.trim())            e.email = 'Email is required.';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
                                e.email = 'Enter a valid email address.';
  if (!password)                e.password = 'Password is required.';
  return e;
}

/* ── Component ──────────────────────────────────────────────── */
export default function LoginPage() {
  const navigate       = useNavigate();
  const { login }      = useAuth();

  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw]     = useState(false);
  const [loading, setLoading]   = useState(false);
  const [errors, setErrors]     = useState<FormErrors>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (name === 'email') setEmail(value);
    else if (name === 'password') setPassword(value);
    if (errors[name as keyof FormErrors])
      setErrors(prev => ({ ...prev, [name]: undefined }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs = validate(email, password);
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }
    setLoading(true);
    try {
      const user = await login({ email, password });
      navigate(user.role === 'ROLE_INSTRUCTOR' ? '/admin/sessions' : '/sessions');
    } catch {
      setErrors({
        general: 'Incorrect email or password.'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8080/api/v1/oauth2/authorization/google';
  };

  return (
    <div className="ln-page">

      {/* Logo */}
      <Link to="/" className="ln-logo">StillNess</Link>

      {/* Card */}
      <div className="ln-card">
        <h1 className="ln-card__title">Welcome Back</h1>
        <p  className="ln-card__sub">Sign in to continue your practice</p>

        {/* Error banner */}
        {errors.general && (
          <div className="ln-error-banner">{errors.general}</div>
        )}

        <form onSubmit={handleSubmit} noValidate>

          {/* Email */}
          <div className="ln-field">
            <label className="ln-field__label" htmlFor="email">
              Email Address
            </label>
            <div className="ln-field__wrap">
              <input
                id="email"
                name="email"
                className={`ln-field__input ln-field__input--plain${errors.email ? ' ln-field__input--error' : ''}`}
                type="email"
                placeholder="Enter your email address"
                autoComplete="email"
                value={email}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
            {errors.email && (
              <span className="ln-field__error-msg">{errors.email}</span>
            )}
          </div>

          {/* Password */}
          <div className="ln-field">
            <label className="ln-field__label" htmlFor="password">
              Password
            </label>
            <div className="ln-field__wrap">
              <input
                id="password"
                name="password"
                className={`ln-field__input${errors.password ? ' ln-field__input--error' : ''}`}
                type={showPw ? 'text' : 'password'}
                placeholder="Enter your password"
                autoComplete="current-password"
                value={password}
                onChange={handleChange}
                disabled={loading}
              />
              <button
                type="button"
                className="ln-field__eye"
                onClick={() => setShowPw(p => !p)}
                aria-label={showPw ? 'Hide password' : 'Show password'}
                disabled={loading}
              >
                <EyeIcon open={showPw} />
              </button>
            </div>
            {errors.password && (
              <span className="ln-field__error-msg">{errors.password}</span>
            )}
          </div>

          {/* Forgot password */}
          <div className="ln-forgot">
            <a href="#">Forgot Password?</a>
          </div>

          {/* Submit */}
          <button
            type="submit"
            className="ln-btn-submit"
            disabled={loading}
          >
            {loading ? 'Signing in…' : 'Login'}
          </button>

        </form>

        {/* Divider */}
        <div className="ln-divider">
          <span className="ln-divider__line" />
          <span className="ln-divider__text">or</span>
          <span className="ln-divider__line" />
        </div>

        {/* Google */}
        <button type="button" className="ln-btn-google" onClick={handleGoogleLogin}>
          <GoogleIcon />
          Sign in with Google
        </button>
      </div>

      {/* Sign-up prompt */}
      <p className="ln-footer">
        Don't have an account?<Link to="/register">Sign Up</Link>
      </p>

    </div>
  );
}