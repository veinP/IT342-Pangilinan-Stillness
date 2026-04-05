import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import AppShell from '../components/AppShell';
import { stillnessApi, type Quote, type Session } from '../api/stillness';

export default function DashboardPage() {
  const [quote, setQuote] = useState<Quote | null>(null);
  const [upcomingSessions, setUpcomingSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      const [quoteData, sessionsData] = await Promise.all([
        stillnessApi.getRandomQuote(),
        stillnessApi.getSessions({ page: 1, limit: 4 }),
      ]);

      if (!mounted) return;
      setQuote(quoteData);
      setUpcomingSessions(sessionsData.sessions.slice(0, 4));
      setLoading(false);
    };

    load();

    return () => {
      mounted = false;
    };
  }, []);

  return (
    <AppShell title="Find Your Stillness" subtitle="Book meditation and wellness sessions that fit your life.">
      <section className="dashboard-hero">
        <div className="dashboard-hero-content">
          <h2>Welcome to your wellness journey</h2>
          <p>Discover peaceful moments and transform your daily routine with our curated collection of meditation, yoga, and breathwork sessions.</p>
          <div className="dashboard-actions">
            <Link to="/sessions" className="action-btn btn-primary">Browse Sessions</Link>
            <button type="button" className="action-btn btn-secondary">Learn More</button>
          </div>
        </div>
      </section>

      <section className="quote-section">
        <div className="quote-card">
          <div className="quote-header">
            <p className="quote-label">Fresh Wellness Quote</p>
            <button 
              type="button" 
              className="quote-refresh"
              onClick={async () => setQuote(await stillnessApi.getRandomQuote())}
              title="Get a new quote"
            >
              🔄
            </button>
          </div>
          <p className="quote-text">"{quote?.text ?? 'Loading your inspiration...'}"</p>
          <p className="quote-author">— {quote?.author ?? 'StillNess'}</p>
        </div>
      </section>

      <section className="sessions-section">
        <div className="sessions-section-header">
          <div>
            <h2>Upcoming Sessions</h2>
            <p className="section-subtitle">Start your wellness journey today</p>
          </div>
          <Link to="/sessions" className="view-all-link">View All →</Link>
        </div>

        {loading ? (
          <p className="muted-copy">Loading sessions...</p>
        ) : upcomingSessions.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">🧘</div>
            <h3>No sessions available</h3>
            <p>Check back soon for our upcoming wellness sessions</p>
          </div>
        ) : (
          <div className="sessions-grid-dashboard">
            {upcomingSessions.map((session) => (
              <article key={session.id} className="session-card-dashboard">
                <div className="session-card-header">
                  <div className="session-type-badge">{session.type}</div>
                  <div className="session-price">{session.price > 0 ? `$${session.price.toFixed(2)}` : 'Free'}</div>
                </div>
                <div className="session-card-content">
                  <h3>{session.title}</h3>
                  <p className="instructor-name">with <strong>{session.instructor.fullName}</strong></p>
                  <div className="session-meta">
                    <span className="meta-item">📅 {new Date(session.startTime).toLocaleDateString()}</span>
                    <span className="meta-item">🕐 {new Date(session.startTime).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})}</span>
                  </div>
                  <p className="location-info">📍 {session.location}</p>
                  <Link to={`/sessions/${session.id}/checkout`} className="reserve-btn">Reserve Spot</Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <style>{`
        .dashboard-hero {
          background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
          border: 1px solid #bae6fd;
          border-radius: 16px;
          padding: 3rem 2rem;
          margin-bottom: 2rem;
          text-align: center;
        }

        .dashboard-hero-content h2 {
          font-size: 2rem;
          color: #1e3a8a;
          margin-bottom: 1rem;
        }

        .dashboard-hero-content p {
          font-size: 1.05rem;
          color: #475569;
          margin-bottom: 1.5rem;
          max-width: 600px;
          margin-left: auto;
          margin-right: auto;
        }

        .dashboard-actions {
          display: flex;
          gap: 1rem;
          justify-content: center;
          flex-wrap: wrap;
        }

        .btn-primary {
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          color: white;
        }

        .btn-primary:hover {
          background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
          transform: translateY(-2px);
        }

        .btn-secondary {
          background: white;
          color: #3b82f6;
          border: 2px solid #3b82f6;
        }

        .btn-secondary:hover {
          background: #eff6ff;
        }

        .quote-section {
          margin-bottom: 2rem;
        }

        .quote-card {
          background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
          border-radius: 16px;
          padding: 2rem;
          color: white;
          box-shadow: 0 4px 20px rgba(30, 58, 138, 0.2);
        }

        .quote-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 1rem;
        }

        .quote-label {
          font-size: 0.9rem;
          font-weight: 600;
          text-transform: uppercase;
          letter-spacing: 0.05em;
          opacity: 0.8;
          margin: 0;
        }

        .quote-refresh {
          background: rgba(255, 255, 255, 0.1);
          border: none;
          border-radius: 8px;
          width: 40px;
          height: 40px;
          font-size: 1.2rem;
          cursor: pointer;
          transition: all 0.2s;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .quote-refresh:hover {
          background: rgba(255, 255, 255, 0.2);
          transform: rotate(180deg);
        }

        .quote-text {
          font-size: 1.5rem;
          font-weight: 600;
          margin-bottom: 1rem;
          line-height: 1.6;
        }

        .quote-author {
          font-size: 0.95rem;
          opacity: 0.8;
          margin: 0;
        }

        .sessions-section {
          margin-bottom: 2rem;
        }

        .sessions-section-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 1.5rem;
          gap: 1rem;
        }

        .sessions-section-header h2 {
          font-size: 1.8rem;
          color: #1e3a8a;
          margin: 0;
        }

        .section-subtitle {
          font-size: 0.95rem;
          color: #64748b;
          margin: 0.5rem 0 0 0;
        }

        .view-all-link {
          color: #3b82f6;
          text-decoration: none;
          font-weight: 600;
          transition: color 0.2s;
          white-space: nowrap;
        }

        .view-all-link:hover {
          color: #2563eb;
        }

        .empty-state {
          text-align: center;
          padding: 3rem 2rem;
          background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
          border: 2px dashed #cbd5e1;
          border-radius: 12px;
          color: #64748b;
        }

        .empty-state-icon {
          font-size: 3.5rem;
          margin-bottom: 1rem;
          display: block;
        }

        .empty-state h3 {
          margin: 0 0 0.5rem 0;
          font-size: 1.5rem;
          color: #334155;
        }

        .empty-state p {
          margin: 0;
          font-size: 1rem;
          color: #64748b;
        }

        .sessions-grid-dashboard {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 1.5rem;
        }

        .session-card-dashboard {
          background: white;
          border: 1px solid #e5e7eb;
          border-radius: 12px;
          overflow: hidden;
          box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
          transition: all 0.3s ease;
          display: flex;
          flex-direction: column;
        }

        .session-card-dashboard:hover {
          transform: translateY(-4px);
          box-shadow: 0 8px 16px rgba(15, 23, 42, 0.12);
          border-color: #bae6fd;
        }

        .session-card-header {
          background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
          padding: 1rem;
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .session-type-badge {
          background: #dbeafe;
          color: #1e40af;
          padding: 0.35rem 0.75rem;
          border-radius: 6px;
          font-size: 0.8rem;
          font-weight: 600;
        }

        .session-price {
          font-size: 1.2rem;
          font-weight: 700;
          color: #0284c7;
        }

        .session-card-content {
          padding: 1.25rem;
          flex: 1;
          display: flex;
          flex-direction: column;
        }

        .session-card-content h3 {
          margin: 0 0 0.5rem 0;
          font-size: 1.1rem;
          font-weight: 700;
          color: #1e293b;
          line-height: 1.4;
        }

        .instructor-name {
          font-size: 0.9rem;
          color: #64748b;
          margin-bottom: 0.75rem;
        }

        .session-meta {
          display: flex;
          flex-direction: column;
          gap: 0.4rem;
          font-size: 0.85rem;
          color: #64748b;
          margin-bottom: 0.75rem;
        }

        .meta-item {
          display: flex;
          align-items: center;
          gap: 0.35rem;
        }

        .location-info {
          font-size: 0.85rem;
          color: #64748b;
          margin-bottom: 1rem;
        }

        .reserve-btn {
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          color: white;
          padding: 0.75rem 1.25rem;
          border-radius: 8px;
          text-decoration: none;
          font-weight: 600;
          border: none;
          cursor: pointer;
          transition: all 0.2s;
          text-align: center;
          margin-top: auto;
        }

        .reserve-btn:hover {
          background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
          transform: translateY(-1px);
        }

        @media (max-width: 640px) {
          .dashboard-hero {
            padding: 2rem 1.5rem;
          }

          .dashboard-hero-content h2 {
            font-size: 1.5rem;
          }

          .dashboard-actions {
            flex-direction: column;
          }

          .btn-primary, .btn-secondary {
            width: 100%;
          }

          .sessions-grid-dashboard {
            grid-template-columns: 1fr;
          }

          .sessions-section-header {
            flex-direction: column;
            align-items: flex-start;
          }
        }
      `}</style>
    </AppShell>
  );
}
