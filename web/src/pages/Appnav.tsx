import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/AppNav.css';

function initials(name = '') {
  return name.split(' ').map(p => p[0] ?? '').join('').slice(0, 2).toUpperCase() || 'U';
}

export default function AppNav() {
  const location = useLocation();
  const { user, logout } = useAuth();
  const isInstructor = user?.role === 'ROLE_INSTRUCTOR';

  const isActive = (path: string) => location.pathname === path ||
    (path === '/sessions' && location.pathname.startsWith('/sessions')) ||
    (path === '/admin/sessions' && location.pathname.startsWith('/admin/sessions')) ||
    (path === '/admin/payments' && location.pathname.startsWith('/admin/payments'));

  const navLinks = !user
    ? []
    : isInstructor
      ? [
          { to: '/admin/sessions', label: 'Manage Sessions' },
          { to: '/admin/payments', label: 'Payments' },
          { to: '/sessions', label: 'Sessions' },
        ]
      : [
          { to: '/sessions', label: 'Sessions' },
          { to: '/bookings', label: 'My Bookings' },
        ];

  return (
    <header className="app-nav">
      {/* Brand word */}
      <span className="app-nav__logo">StillNess</span>

      {/* Center links */}
      <nav className="app-nav__links" aria-label="Main navigation">
        {navLinks.map((link) => (
          <Link key={link.to} to={link.to} className={`app-nav__link${isActive(link.to) ? ' app-nav__link--active' : ''}`}>
            {link.label}
          </Link>
        ))}
      </nav>

      {/* Right side — logged out: Login + Sign Up | logged in: avatar + name */}
      <div className="app-nav__right">
        {user ? (
          <div className="app-nav__user">
            <div className="app-nav__avatar">{initials(user.fullName)}</div>
            <span className="app-nav__username">
              {user.fullName ?? 'Account'}
            </span>
            <button type="button" className="app-nav__btn-signout" onClick={logout}>
              Sign Out
            </button>
          </div>
        ) : (
          <div className="app-nav__auth">
            <Link to="/login"    className="app-nav__btn-login">Login</Link>
            <Link to="/register" className="app-nav__btn-signup">Sign Up</Link>
          </div>
        )}
      </div>
    </header>
  );
}