import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { stillnessApi, type Quote, type Session } from '../api/stillness';
import { useAuth } from '../context/AuthContext';
import AppShell from '../components/AppShell';
import '../styles/SessionsPage.css';

const SESSION_TYPES = ['all', 'Meditation', 'Yoga', 'Breathwork'];

function getCapacityInfo(booked: number, capacity: number) {
  const remaining = Math.max(capacity - booked, 0);
  const pct = capacity > 0 ? Math.min((booked / capacity) * 100, 100) : 0;
  let tier: 'green' | 'yellow' | 'red' = 'green';
  if (remaining === 0) tier = 'red';
  else if (remaining <= 3) tier = 'red';
  else if (remaining <= 5) tier = 'yellow';
  return { remaining, pct, tier };
}

function getTypeBadgeClass(type = '') {
  const t = type.toLowerCase();
  if (t.includes('yoga')) return 'sc-card__type-badge--yoga';
  if (t.includes('breath')) return 'sc-card__type-badge--breathwork';
  return 'sc-card__type-badge--meditation';
}

function getThumbBg() {
  return '#BFDBFE';
}

function initials(name = '') {
  return name.split(' ').map((part) => part[0] ?? '').join('').slice(0, 2).toUpperCase();
}

function fmtTime(iso: string) {
  return new Date(iso).toLocaleString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

const SearchIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <circle cx="8" cy="8" r="6" stroke="#9CA3AF" strokeWidth="1.6" />
    <path d="M13 13l3 3" stroke="#9CA3AF" strokeWidth="1.6" strokeLinecap="round" />
  </svg>
);

const ChevronIcon = ({ size = 14 }: { size?: number }) => (
  <svg width={size} height={size} viewBox="0 0 14 14" fill="none">
    <path d="M3.5 5.25L7 8.75l3.5-3.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

const ClockIcon = () => (
  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
    <circle cx="7" cy="7" r="5.5" stroke="#94A3B8" strokeWidth="1.3" />
    <path d="M7 4.5v3l1.75 1.25" stroke="#94A3B8" strokeWidth="1.3" strokeLinecap="round" />
  </svg>
);

const PinIcon = () => (
  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
    <path d="M7 1.5A4 4 0 0 1 11 5.5c0 2.5-4 7-4 7s-4-4.5-4-7a4 4 0 0 1 4-4z" stroke="#94A3B8" strokeWidth="1.3" />
    <circle cx="7" cy="5.5" r="1.3" stroke="#94A3B8" strokeWidth="1.2" />
  </svg>
);

const RefreshIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <path d="M15.75 9A6.75 6.75 0 0 1 4.04 13.96" stroke="#3B82F6" strokeWidth="1.5" strokeLinecap="round" />
    <path d="M2.25 9A6.75 6.75 0 0 1 13.96 4.04" stroke="#3B82F6" strokeWidth="1.5" strokeLinecap="round" />
    <path d="M13.5 1.5 14.25 4.5l-3 .75" stroke="#3B82F6" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    <path d="M4.5 16.5 3.75 13.5l3-.75" stroke="#3B82F6" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

export default function SessionsPage() {
  const { user } = useAuth();
  const [sessions, setSessions] = useState<Session[]>([]);
  const [quote, setQuote] = useState<Quote | null>(null);
  const [query, setQuery] = useState('');
  const [liveQuery, setLiveQuery] = useState('');
  const [selectedType, setSelectedType] = useState('all');
  const [instructorFilter, setInstructorFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshingQuote, setRefreshingQuote] = useState(false);
  const [refreshingSessions, setRefreshingSessions] = useState(false);

  const instructors = useMemo(
    () => Array.from(new Set(sessions.map((session) => session.instructor.fullName))).sort(),
    [sessions],
  );

  useEffect(() => {
    let live = true;
    setLoading(true);
    setError(null);

    Promise.all([
      stillnessApi.getSessions({
        type: selectedType,
        query,
        instructor: instructorFilter !== 'all' ? instructorFilter : undefined,
      }),
      stillnessApi.getRandomQuote(),
    ])
      .then(([res, q]) => {
        if (live) {
          setSessions(res.sessions);
          setQuote(q);
        }
      })
      .catch((err) => {
        if (live) setError(err instanceof Error ? err.message : 'Failed to load sessions.');
      })
      .finally(() => {
        if (live) setLoading(false);
      });

    return () => {
      live = false;
    };
  }, [selectedType, query, instructorFilter]);

  const handleSearch = () => setQuery(liveQuery);
  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter') handleSearch();
  };

  const handleRefreshQuote = async () => {
    setRefreshingQuote(true);
    try {
      setQuote(await stillnessApi.getRandomQuote());
    } finally {
      setRefreshingQuote(false);
    }
  };

  const handleRefreshSessions = async () => {
    setRefreshingSessions(true);
    setError(null);
    try {
      const res = await stillnessApi.getSessions({
        type: selectedType,
        query,
        instructor: instructorFilter !== 'all' ? instructorFilter : undefined,
      });
      setSessions(res.sessions);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to refresh sessions.');
    } finally {
      setRefreshingSessions(false);
    }
  };

  const greetingName = user?.fullName?.split(' ')[0] ?? 'there';

  return (
    <AppShell>
      <section className="sc-header">
        <div className="sc-header-content">
          <div className="sc-header-text">
            <h1>Hi, {greetingName}</h1>
            <p className="sc-header-subtitle">Discover and book meditation, yoga, and breathwork sessions</p>
          </div>
          <button type="button" className="sc-search-btn" onClick={handleRefreshSessions} disabled={refreshingSessions}>
            {refreshingSessions ? 'Refreshing...' : 'Refresh Sessions'}
          </button>
        </div>
      </section>

      <section className="sc-quote">
        <div>
          <p className="sc-quote__text">{quote ? `"${quote.text}"` : 'Loading quote…'}</p>
          {quote ? <p className="sc-quote__attr">— {quote.author}</p> : null}
        </div>
        <button
          type="button"
          className="sc-quote__refresh"
          onClick={handleRefreshQuote}
          aria-label="Refresh quote"
          style={{
            transform: refreshingQuote ? 'rotate(360deg)' : 'none',
            transition: refreshingQuote ? 'transform .5s ease' : 'none',
          }}
        >
          <RefreshIcon />
        </button>
      </section>

      <section className="sc-controls">
        <div className="sc-search-row">
          <span className="sc-search-icon"><SearchIcon /></span>
          <input
            type="text"
            className="sc-search-input"
            placeholder="Search sessions..."
            value={liveQuery}
            onChange={(event) => setLiveQuery(event.target.value)}
            onKeyDown={handleKeyDown}
          />
          <span className="sc-search-divider" />
          <div className="sc-select-wrap">
            <select className="sc-select" value={selectedType} onChange={(event) => setSelectedType(event.target.value)}>
              <option value="all">All Types</option>
              {SESSION_TYPES.filter((type) => type !== 'all').map((type) => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
            <span className="sc-select-chevron"><ChevronIcon /></span>
          </div>
          <span className="sc-search-divider" />
          <div className="sc-select-wrap">
            <select className="sc-select" value={instructorFilter} onChange={(event) => setInstructorFilter(event.target.value)}>
              <option value="all">All Instructors</option>
              {instructors.map((name) => (
                <option key={name} value={name}>{name}</option>
              ))}
            </select>
            <span className="sc-select-chevron"><ChevronIcon /></span>
          </div>
          <button type="button" className="sc-search-btn" onClick={handleSearch}>Search</button>
        </div>
      </section>

      <section className="sc-grid-wrap">
        {loading ? <p className="sc-state">Loading sessions…</p> : null}
        {error ? <p className="sc-state sc-state--error">{error}</p> : null}

        {!loading && !error ? (
          <div className="sc-grid">
            {sessions.map((session) => {
              const { remaining, pct, tier } = getCapacityInfo(session.bookedCount ?? 0, session.capacity ?? 0);
              const available = remaining > 0;

              return (
                <article key={session.id} className="sc-card">
                  <div className="sc-card__thumb" style={{ background: getThumbBg() }}>
                    <span className={`sc-card__type-badge ${getTypeBadgeClass(session.type)}`}>
                      {session.type}
                    </span>
                    <h3 className="sc-card__thumb-title">{session.title}</h3>
                  </div>

                  <div className="sc-card__body">
                    <p className="sc-card__name">{session.title}</p>
                    <div className="sc-card__instructor-row">
                      <div className="sc-card__avatar">{initials(session.instructor?.fullName)}</div>
                      <span className="sc-card__instructor-name">with {session.instructor?.fullName ?? 'Instructor'}</span>
                    </div>
                    <p className="sc-card__meta-row"><ClockIcon />{fmtTime(session.startTime)}</p>
                    <p className="sc-card__meta-row"><PinIcon />{session.location ?? 'TBD'}</p>
                    <div className="sc-card__capacity-row">
                      <span className={`sc-card__spots sc-card__spots--${tier}`}>
                        {remaining} spot{remaining !== 1 ? 's' : ''} remaining
                      </span>
                      <span className="sc-card__ratio">{session.bookedCount ?? 0}/{session.capacity ?? 0}</span>
                    </div>
                    <div className="sc-card__bar-track">
                      <div className={`sc-card__bar-fill sc-card__bar-fill--${tier}`} style={{ width: `${pct}%` }} />
                    </div>
                  </div>

                  <div className="sc-card__footer">
                    <span className="sc-card__price">
                      {(session.price ?? 0) > 0 ? `$${session.price.toFixed(2)}` : 'Free'}
                    </span>
                    {available ? (
                      <Link to={`/sessions/${session.id}`} className="sc-card__reserve">Reserve Spot</Link>
                    ) : (
                      <span className="sc-card__full">Session Full</span>
                    )}
                  </div>
                </article>
              );
            })}
          </div>
        ) : null}
      </section>
    </AppShell>
  );
}