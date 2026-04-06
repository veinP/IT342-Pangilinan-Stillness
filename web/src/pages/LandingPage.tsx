import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { stillnessApi, type Quote, type Session } from '../api/stillness';
import '../styles/LandingPage.css';
import AppNav from './Appnav';

/* ── Icons ──────────────────────────────────────────────────── */
const GoogleIcon = () => (
  <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
    <path fill="#4285F4" d="M19.6 10.23c0-.68-.06-1.36-.17-2H10v3.79h5.43a4.64 4.64 0 0 1-2.01 3.05v2.53h3.25c1.9-1.75 3-4.33 3-7.37z"/>
    <path fill="#34A853" d="M10 20c2.7 0 4.97-.9 6.62-2.4l-3.25-2.53c-.9.6-2.04.96-3.37.96-2.59 0-4.78-1.75-5.56-4.1H1.09v2.6A10 10 0 0 0 10 20z"/>
    <path fill="#FBBC05" d="M4.44 11.93A6 6 0 0 1 4.12 10c0-.67.12-1.32.32-1.93V5.47H1.09A10 10 0 0 0 0 10c0 1.61.39 3.13 1.09 4.53l3.35-2.6z"/>
    <path fill="#EA4335" d="M10 3.97c1.46 0 2.77.5 3.8 1.49l2.85-2.85C14.96.9 12.7 0 10 0A10 10 0 0 0 1.09 5.47l3.35 2.6C5.22 5.72 7.41 3.97 10 3.97z"/>
  </svg>
);

const ClockIcon = () => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
    <circle cx="8" cy="8" r="6.25" stroke="#94A3B8" strokeWidth="1.5"/>
    <path d="M8 5v3.5l2.25 1.5" stroke="#94A3B8" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const SpotIcon = ({ color }: { color: string }) => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
    <rect x="1.75" y="3.25" width="12.5" height="11" rx="1.25" stroke={color} strokeWidth="1.5"/>
    <path d="M5 1.5V4.5M11 1.5V4.5M1.75 7.25H14.25" stroke={color} strokeWidth="1.5" strokeLinecap="round"/>
  </svg>
);

const ChatIcon = () => (
  <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
    <path d="M12.25 8.75A1.25 1.25 0 0 1 11 10H4.5L2 12.5V2.75A1.25 1.25 0 0 1 3.25 1.5H11A1.25 1.25 0 0 1 12.25 2.75v6z"
      stroke="#3B82F6" strokeWidth="1.35" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const RefreshIcon = () => (
  <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
    <path d="M17.5 10A7.5 7.5 0 0 1 4.39 15.61" stroke="#3B82F6" strokeWidth="1.6" strokeLinecap="round"/>
    <path d="M2.5 10A7.5 7.5 0 0 1 15.61 4.39" stroke="#3B82F6" strokeWidth="1.6" strokeLinecap="round"/>
    <path d="M15 1.5 15.75 4.75l-3.25.75" stroke="#3B82F6" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
    <path d="M5 18.5 4.25 15.25l3.25-.75" stroke="#3B82F6" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

/* ── Helpers ────────────────────────────────────────────────── */
function getSpots(session: Session) {
  const n = (session.capacity ?? 10) - (session.bookedCount ?? 0);
  const label = `${n} spot${n !== 1 ? 's' : ''} remaining`;
  if (n <= 2) return { label, color: '#EF4444' };
  if (n <= 5) return { label, color: '#F59E0B' };
  return           { label, color: '#10B981' };
}

function getTagClass(type = '') {
  const t = type.toLowerCase();
  if (t.includes('yoga'))   return 'sn-card__tag--yoga';
  if (t.includes('breath')) return 'sn-card__tag--breathwork';
  return                           'sn-card__tag--meditation';
}

function getTagLabel(type = '') {
  const t = type.toLowerCase();
  if (t.includes('yoga'))   return 'Yoga';
  if (t.includes('breath')) return 'Breathwork';
  return                           'Meditation';
}

function getThumbBg() {
  return '#BFDBFE';
}

function getThumbLabel(type = '') {
  const t = type.toLowerCase();
  if (t.includes('yoga'))   return 'Yoga Session';
  if (t.includes('breath')) return 'Breathwork Session';
  return 'Meditation Session';
}

function fmtTime(iso: string) {
  const d = new Date(iso);
  return (
    d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' }) +
    ' • ' +
    d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })
  );
}

/* ── Component ──────────────────────────────────────────────── */
export default function LandingPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [quote, setQuote]       = useState<Quote | null>(null);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [spinning, setSpinning] = useState(false);
  const refreshRef              = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (user?.role === 'ROLE_INSTRUCTOR') {
      navigate('/admin/sessions', { replace: true });
    }
  }, [user, navigate]);

  useEffect(() => {
    let live = true;
    Promise.all([
      stillnessApi.getRandomQuote(),
      stillnessApi.getSessions({ page: 0, limit: 3 }),
    ]).then(([q, s]) => {
      if (!live) return;
      setQuote(q);
      setSessions(s.sessions.slice(0, 3));
    });
    return () => { live = false; };
  }, []);

  const handleRefresh = async () => {
    if (spinning) return;
    setSpinning(true);
    // Refresh gets a new random quote (bypassing daily cache)
    const q = await stillnessApi.getRandomQuote();
    setQuote(q);
    setTimeout(() => setSpinning(false), 500);
  };

  const handleGoogleLogin = () => {
    window.location.href = '/api/v1/oauth2/authorization/google';
  };

  return (
    <div className="sn-page">

      <AppNav />

      {/* ══ SHARED NAVBAR ════════════════════════════════════════ */}

      {/* ══ HERO ═════════════════════════════════════════════════ */}
      <section className="sn-hero">
        <h1 className="sn-hero__title">Find Your Stillness</h1>
        <p  className="sn-hero__sub">Book meditation and wellness sessions that fit your life.</p>
        {!user && (
          <div className="sn-hero__cta">
            <Link to="/login" className="sn-btn-browse">Browse Sessions</Link>
            <button type="button" className="sn-btn-google" onClick={handleGoogleLogin}>
              <GoogleIcon />
              Sign In with Google
            </button>
          </div>
        )}
      </section>

      {/* ══ QUOTE ════════════════════════════════════════════════ */}
      <div className="sn-quote">
        <div>
          <p className="sn-quote__label">
            <ChatIcon />
            Fresh Wellness Quote
          </p>
          <p className="sn-quote__text">
            "{quote?.text ?? 'Loading your quote…'}"
          </p>
          <p className="sn-quote__attr">— {quote?.author ?? 'StillNess'}</p>
        </div>
        <button
          ref={refreshRef}
          type="button"
          className="sn-quote__refresh"
          onClick={handleRefresh}
          aria-label="Refresh quote"
          style={{
            transition: spinning ? 'transform 0.5s ease' : 'none',
            transform:  spinning ? 'rotate(360deg)' : 'rotate(0deg)',
          }}
        >
          <RefreshIcon />
        </button>
      </div>

      {/* ══ SESSIONS ═════════════════════════════════════════════ */}
      <section className="sn-sessions">
        <h2 className="sn-sessions__title">Upcoming Sessions</h2>

        <div className="sn-grid">
          {sessions.map((s) => {
            const spots = getSpots(s);
            return (
              <article key={s.id} className="sn-card">

                {/* Thumbnail */}
                <div className="sn-card__thumb" style={{ background: getThumbBg() }}>
                  <span className="sn-card__thumb-text">{getThumbLabel(s.type)}</span>
                </div>

                {/* Body */}
                <div className="sn-card__body">
                  <div className="sn-card__top-row">
                    <h3 className="sn-card__name">{s.title}</h3>
                    <span className={`sn-card__tag ${getTagClass(s.type)}`}>
                      {getTagLabel(s.type)}
                    </span>
                  </div>

                  <p className="sn-card__instructor">
                    with {s.instructor?.fullName ?? 'Instructor'}
                  </p>

                  <p className="sn-card__time">
                    <ClockIcon />
                    {fmtTime(s.startTime)}
                  </p>

                  <p className="sn-card__spots" style={{ color: spots.color }}>
                    <SpotIcon color={spots.color} />
                    {spots.label}
                  </p>

                  <Link to={user ? `/sessions/${s.id}` : '/login'} className="sn-card__reserve">
                    Reserve Spot
                  </Link>
                </div>

              </article>
            );
          })}
        </div>
      </section>

      {/* ══ FOOTER ═══════════════════════════════════════════════ */}
      <footer className="sn-footer">
        <span className="sn-footer__copy">© 2026 StillNess. All rights reserved.</span>
        <div className="sn-footer__links">
          <a href="#">Privacy Policy</a>
          <a href="#">Terms</a>
          <a href="#">Contact</a>
        </div>
      </footer>

    </div>
  );
}