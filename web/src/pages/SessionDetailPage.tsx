import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { stillnessApi, type Session } from '../api/stillness';
import AppNav from './Appnav';
import '../styles/SessionDetailPage.css';

/* ── Icons ───────────────────────────────────────────────────── */
const ChevronRight = () => (
  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
    <path d="M5.25 3.5L8.75 7l-3.5 3.5" stroke="#D1D5DB" strokeWidth="1.5"
      strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const CalendarIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <rect x="2" y="3" width="14" height="13" rx="2" stroke="#6B7280" strokeWidth="1.4"/>
    <path d="M6 1.5V4.5M12 1.5V4.5M2 7.5h14" stroke="#6B7280" strokeWidth="1.4" strokeLinecap="round"/>
  </svg>
);

const ClockIcon = ({ color = '#6B7280' }: { color?: string }) => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <circle cx="9" cy="9" r="7" stroke={color} strokeWidth="1.4"/>
    <path d="M9 5v4.5l2.5 1.5" stroke={color} strokeWidth="1.4" strokeLinecap="round"/>
  </svg>
);

const PinIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <path d="M9 1.5A5.5 5.5 0 0 1 14.5 7c0 3.5-5.5 9.5-5.5 9.5S3.5 10.5 3.5 7A5.5 5.5 0 0 1 9 1.5z"
      stroke="#6B7280" strokeWidth="1.4"/>
    <circle cx="9" cy="7" r="2" stroke="#6B7280" strokeWidth="1.4"/>
  </svg>
);

const InfoIcon = () => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
    <circle cx="8" cy="8" r="6.5" stroke="#9CA3AF" strokeWidth="1.3"/>
    <path d="M8 7.5v4" stroke="#9CA3AF" strokeWidth="1.3" strokeLinecap="round"/>
    <circle cx="8" cy="5.5" r=".8" fill="#9CA3AF"/>
  </svg>
);

/* ── Helpers ─────────────────────────────────────────────────── */
function getThumbBg() {
  return '#BFDBFE';
}

function getBadgeClass(type = '') {
  const t = type.toLowerCase();
  if (t.includes('yoga'))   return 'sd-badge--yoga';
  if (t.includes('breath')) return 'sd-badge--breathwork';
  return 'sd-badge--meditation';
}

function getCapacity(booked: number, capacity: number) {
  const remaining = Math.max(capacity - booked, 0);
  const pct = capacity > 0 ? Math.min((booked / capacity) * 100, 100) : 0;
  let tier: 'green' | 'yellow' | 'red' = 'green';
  if (remaining === 0)     tier = 'red';
  else if (remaining <= 3) tier = 'red';
  else if (remaining <= 5) tier = 'yellow';
  return { remaining, pct, tier };
}

function initials(name = '') {
  return name.split(' ').map(p => p[0] ?? '').join('').slice(0, 2).toUpperCase() || 'IN';
}

function fmtDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-US', {
    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric',
  });
}

function fmtTime(iso: string, durationMins = 60) {
  const start = new Date(iso);
  const end   = new Date(start.getTime() + durationMins * 60_000);
  const fmt   = (d: Date) => d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
  return `${fmt(start)} - ${fmt(end)}`;
}

/* ── Component ───────────────────────────────────────────────── */
export default function SessionDetailPage() {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();

  const [session, setSession]   = useState<Session | null>(null);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState<string | null>(null);

  useEffect(() => {
    if (!sessionId) { setError('Session not found.'); setLoading(false); return; }
    let live = true;
    stillnessApi.getSessionById(sessionId)
      .then((s) => { if (live) setSession(s); })
      .catch(err => { if (live) setError(err?.message ?? 'Failed to load session.'); })
      .finally(() => { if (live) setLoading(false); });
    return () => { live = false; };
  }, [sessionId]);

  const handleReserve = () => {
    if (!session) return;
    navigate(`/sessions/${session.id}/checkout`);
  };

  if (loading) return (
    <div className="sd-page">
      <AppNav />
      <p className="sd-state">Loading session…</p>
    </div>
  );

  if (error || !session) return (
    <div className="sd-page">
      <AppNav />
      <p className="sd-state sd-state--error">{error ?? 'Session not found.'}</p>
    </div>
  );

  const { remaining, pct, tier } = getCapacity(session.bookedCount ?? 0, session.capacity ?? 0);
  const available = remaining > 0;
  const duration  = session.duration ?? 60;

  return (
    <div className="sd-page">

      {/* ══ NAVBAR ════════════════════════════════════════════════ */}
      <AppNav />

      {/* ══ BREADCRUMB ════════════════════════════════════════════ */}
      <div className="sd-breadcrumb">
        <Link to="/sessions" className="sd-breadcrumb__link">Sessions</Link>
        <span className="sd-breadcrumb__sep"><ChevronRight /></span>
        <span className="sd-breadcrumb__current">{session.title}</span>
      </div>

      {/* ══ MAIN TWO-COLUMN ═══════════════════════════════════════ */}
      <div className="sd-main">

        {/* ── LEFT COLUMN ─────────────────────────────────────── */}
        <div className="sd-left">

          {/* Hero */}
          <div className="sd-hero" style={{ background: getThumbBg() }}>
            <span className={`sd-badge ${getBadgeClass(session.type)}`}>
              {session.type}
            </span>
            <h1 className="sd-hero__title">{session.title}</h1>
            <div /> {/* spacer for flex justify-content: space-between */}
          </div>

          {/* Large title below hero */}
          <h2 className="sd-page-title">{session.title}</h2>

          {/* About */}
          <div className="sd-section sd-section--card">
            <h3 className="sd-section__heading">About This Session</h3>
            <p className="sd-section__body">
              {session.description ??
                `Start your day with clarity and intention. This guided ${(session.type ?? 'meditation').toLowerCase()} session focuses on breath awareness, body scanning, and mindful presence. Perfect for beginners and experienced practitioners alike. We'll explore techniques to calm the mind, reduce stress, and cultivate a sense of inner peace that carries throughout your day. Each session includes gentle stretching, seated meditation, and time for reflection. Bring a journal if you'd like to capture insights from your practice.`}
            </p>
          </div>

          {/* Instructor */}
          <div className="sd-section sd-section--card">
            <h3 className="sd-section__heading">Your Instructor</h3>
            <div className="sd-instructor">
              <div className="sd-instructor__name-row">
                <div className="sd-instructor__avatar">
                  {initials(session.instructor?.fullName)}
                </div>
                <span className="sd-instructor__name">
                  {session.instructor?.fullName ?? 'Instructor'}
                </span>
              </div>

              <p className="sd-instructor__bio">
                {session.instructor?.bio ??
                  `${session.instructor?.fullName ?? 'Your instructor'} has been teaching meditation and mindfulness practices for over 8 years. They combine traditional Buddhist meditation techniques with modern neuroscience to create accessible, transformative experiences. Their warm, grounding presence helps students feel safe to explore their inner landscape.`}
              </p>

              <p className="sd-instructor__exp">
                <ClockIcon color="#6B7280" />
                {session.instructor?.yearsExperience ?? 8} years experience
              </p>

              <p className="sd-instructor__cert-label">Certifications</p>
              <div className="sd-instructor__certs">
                {(session.instructor?.certifications?.length
                  ? session.instructor.certifications
                  : ['Certified Mindfulness Teacher', 'Yoga Alliance RYT-500', 'MBSR Instructor']
                ).map((c: string) => (
                  <span key={c} className="sd-instructor__cert-tag">{c}</span>
                ))}
              </div>
            </div>
          </div>

        </div>

        {/* ── RIGHT COLUMN — sticky details card ──────────────── */}
        <aside className="sd-details-card">
          <h3 className="sd-details-card__title">Session Details</h3>

          {/* Date */}
          <div className="sd-detail-row">
            <span className="sd-detail-row__icon"><CalendarIcon /></span>
            <div>
              <p className="sd-detail-row__label">Date</p>
              <p className="sd-detail-row__value">{fmtDate(session.startTime)}</p>
            </div>
          </div>

          {/* Time */}
          <div className="sd-detail-row">
            <span className="sd-detail-row__icon"><ClockIcon /></span>
            <div>
              <p className="sd-detail-row__label">Time</p>
              <p className="sd-detail-row__value">{fmtTime(session.startTime, duration)}</p>
            </div>
          </div>

          {/* Location */}
          <div className="sd-detail-row">
            <span className="sd-detail-row__icon"><PinIcon /></span>
            <div>
              <p className="sd-detail-row__label">Location</p>
              <p className="sd-detail-row__value">{session.location ?? 'Studio A - Downtown'}</p>
              <p className="sd-detail-row__sub">{session.address ?? '123 Wellness Way, Suite 100'}</p>
            </div>
          </div>

          {/* Duration */}
          <div className="sd-detail-row">
            <span className="sd-detail-row__icon"><ClockIcon /></span>
            <div>
              <p className="sd-detail-row__label">Duration</p>
              <p className="sd-detail-row__value">{duration} minutes</p>
            </div>
          </div>

          {/* Capacity */}
          <div className="sd-capacity">
            <div className="sd-capacity__header">
              <span className="sd-capacity__label">Capacity</span>
              <span className="sd-capacity__ratio">
                {session.bookedCount ?? 0} of {session.capacity ?? 0} spots filled
              </span>
            </div>
            <div className="sd-capacity__track">
              <div
                className={`sd-capacity__fill sd-capacity__fill--${tier}`}
                style={{ width: `${pct}%` }}
              />
            </div>
            <p className={`sd-capacity__spots sd-capacity__spots--${tier}`}>
              {remaining} spot{remaining !== 1 ? 's' : ''} remaining
            </p>
          </div>

          {/* Divider */}
          <div className="sd-divider" />

          {/* Price */}
          <div className="sd-price-row">
            <span className="sd-price-row__label">Price</span>
            <span className="sd-price-row__value">
              {(session.price ?? 0) > 0 ? `$${session.price.toFixed(2)}` : 'Free'}
            </span>
          </div>

          {/* Reserve / Full */}
          {available ? (
            <button
              type="button"
              className="sd-reserve-btn"
              onClick={handleReserve}
            >
              Reserve Spot
            </button>
          ) : (
            <div className="sd-full-btn">Session Full</div>
          )}

          {/* Cancellation notice */}
          <div className="sd-cancel-notice">
            <InfoIcon />
            <span>Cancellation allowed up to 2 hours before session</span>
          </div>
        </aside>

      </div>

      {/* ══ FOOTER ════════════════════════════════════════════════ */}
      <footer className="sd-footer">
        <span className="sd-footer__copy">© 2026 StillNess. All rights reserved.</span>
        <div className="sd-footer__links">
          <a href="#">Privacy Policy</a>
          <a href="#">Terms</a>
          <a href="#">Contact</a>
        </div>
      </footer>

    </div>
  );
}