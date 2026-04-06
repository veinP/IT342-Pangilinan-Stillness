import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { isBookingCancellable, stillnessApi, type Booking } from '../api/stillness';
import '../styles/MyBookingsPage.css';
import AppNav from './Appnav';

/* ── Icons ───────────────────────────────────────────────────── */
const CalendarIcon = () => <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><rect x="1.5" y="2.5" width="12" height="11" rx="1.5" stroke="currentColor" strokeWidth="1.3"/><path d="M5 1V4M10 1V4M1.5 6.5H13.5" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round"/></svg>;
const ClockIcon = () => <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><circle cx="7.5" cy="7.5" r="6" stroke="currentColor" strokeWidth="1.3"/><path d="M7.5 4.5v3.25L9.5 9.5" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round"/></svg>;
const PinIcon = () => <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M7.5 1.5A4.5 4.5 0 0 1 12 6c0 3-4.5 7.5-4.5 7.5S3 9 3 6a4.5 4.5 0 0 1 4.5-4.5z" stroke="currentColor" strokeWidth="1.3"/><circle cx="7.5" cy="6" r="1.5" stroke="currentColor" strokeWidth="1.2"/></svg>;

/* ── Helpers ─────────────────────────────────────────────────── */
type BookingTab = 'upcoming' | 'past';
const ITEMS_PER_PAGE = 4;

function getBadgeClass(type = '') {
  const t = type.toLowerCase();
  if (t.includes('yoga'))    return 'mb-badge--yoga';
  if (t.includes('breath'))  return 'mb-badge--breathwork';
  if (t.includes('heal'))    return 'mb-badge--healing';
  if (t.includes('meditat')) return 'mb-badge--meditation';
  return 'mb-badge--default';
}

function fmtDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
}

function fmtTimeRange(iso: string, durationMins = 60) {
  const start = new Date(iso);
  const end = new Date(start.getTime() + durationMins * 60_000);
  const fmt = (d: Date) => d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
  return `${fmt(start)} - ${fmt(end)}`;
}

function statusPill(status: string) {
  switch (status?.toUpperCase()) {
    case 'CONFIRMED': return <span className="mb-pill-confirmed">Confirmed</span>;
    case 'CANCELLED': return <span className="mb-pill-cancelled">Cancelled</span>;
    default: return <span className="mb-pill-pending">{status}</span>;
  }
}

function paymentPill(status?: string) {
  switch (status?.toUpperCase()) {
    case 'PAID': return <span className="mb-pill-confirmed">Paid</span>;
    case 'REFUNDED': return <span className="mb-pill-cancelled">Refunded</span>;
    case 'FAILED': return <span className="mb-pill-cancelled">Failed</span>;
    default: return <span className="mb-pill-pending">Pending</span>;
  }
}

function formatMoney(amount?: number, sessionPrice?: number) {
  const resolvedAmount = typeof amount === 'number' ? amount : sessionPrice;

  if (typeof resolvedAmount !== 'number') {
    return 'Included';
  }

  return resolvedAmount === 0 ? 'Free' : `$${resolvedAmount.toFixed(2)}`;
}

function BookingCard({ booking, featured = false, onCancel }: {
  booking: Booking;
  featured?: boolean;
  onCancel?: (bookingId: string) => void;
}) {
  const s = booking.session;
  const canCancel = booking.status === 'CONFIRMED' && isBookingCancellable(booking.cancellableUntil);
  const isFreeBooking = (booking.amount ?? s.price ?? 0) === 0;

  return (
    <article className={`mb-card${featured ? ' mb-card--featured' : ''}`}>
      <div className="mb-card__thumb">
        <span>{featured ? 'Confirmed' : 'Session'}</span>
      </div>

      <div className="mb-card__content">
        <div className="mb-card__title-row">
          <h3 className="mb-card__title">{s.title}</h3>
          <span className={`mb-badge ${getBadgeClass(s.type)}`}>{s.type}</span>
          {isFreeBooking ? <span className="mb-badge mb-badge--free">Free Session</span> : null}
        </div>

        <div className="mb-card__meta-row">
          <div className="mb-meta-item"><CalendarIcon /> {fmtDate(s.startTime)}</div>
          <div className="mb-meta-item"><ClockIcon /> {fmtTimeRange(s.startTime, s.duration ?? 60)}</div>
          <div className="mb-meta-item"><PinIcon /> {s.location ?? 'TBD'}</div>
        </div>

        <div className="mb-card__details">
          <span><strong>Booking #:</strong> {booking.bookingNumber}</span>
          <span><strong>Booked:</strong> {new Date(booking.bookedAt).toLocaleString()}</span>
          <span><strong>Amount:</strong> {formatMoney(booking.amount, s.price)}</span>
          <span><strong>Payment:</strong> {paymentPill(booking.paymentStatus)}</span>
        </div>
      </div>

      <div className="mb-card__actions">
        {statusPill(booking.status)}
        <Link to={`/sessions/${s.id}`} className="mb-view-btn">View Details</Link>
        {canCancel && onCancel ? (
          <button className="mb-cancel-btn" onClick={() => onCancel(booking.id)}>Cancel Booking</button>
        ) : null}
      </div>
    </article>
  );
}

export default function MyBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [tab, setTab] = useState<BookingTab>('upcoming');
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    setLoading(true);
    stillnessApi.getMyBookings()
      .then(data => {
        setBookings(data);
      })
      .catch(() => {
        setBookings([]);
      })
      .finally(() => setLoading(false));
  }, []);

  const handleCancelBooking = async (bookingId: string) => {
    if (window.confirm('Are you sure you want to cancel this booking?')) {
      try {
        await stillnessApi.cancelBooking(bookingId);
        setBookings(bookings.map(b => b.id === bookingId ? { ...b, status: 'CANCELLED' } : b));
      } catch (error) {
        alert('Failed to cancel booking: ' + (error instanceof Error ? error.message : 'Unknown error'));
      }
    }
  };

  const filtered = useMemo(() => {
    const now = Date.now();
    return bookings.filter(b => {
      const t = new Date(b.session.startTime).getTime();
      return tab === 'upcoming' ? t >= now : t < now;
    });
  }, [bookings, tab]);

  const featuredBooking = bookings[0] ?? null;
  const listBookings = featuredBooking
    ? filtered.filter((booking) => booking.id !== featuredBooking.id)
    : filtered;
  const paginated = listBookings.slice((currentPage - 1) * ITEMS_PER_PAGE, currentPage * ITEMS_PER_PAGE);

  return (
    <div className="mb-page">
      <AppNav />
      
      {/* Header Section */}
      <div className="mb-header-section">
        <div className="mb-header-content">
          <div className="mb-header-text">
            <h1>My Bookings</h1>
            <p className="mb-header-subtitle">Manage your wellness session bookings</p>
          </div>
        </div>
      </div>
      
      <div className="mb-header">
        <div className="mb-tabs">
          <button className={`mb-tab${tab === 'upcoming' ? ' mb-tab--active' : ''}`} onClick={() => {setTab('upcoming'); setCurrentPage(1);}}>Upcoming</button>
          <button className={`mb-tab${tab === 'past' ? ' mb-tab--active' : ''}`} onClick={() => {setTab('past'); setCurrentPage(1);}}>Past Bookings</button>
        </div>
      </div>

      {!loading && featuredBooking ? (
        <div className="mb-featured">
          <div className="mb-featured__header">
            <h2>Your latest confirmed booking</h2>
            <p>Use this card to verify the session details you just reserved.</p>
          </div>
          <BookingCard booking={featuredBooking} featured onCancel={handleCancelBooking} />
        </div>
      ) : null}

      <div className="mb-list">
        {!loading && paginated.map((booking) => (
          <BookingCard key={booking.id} booking={booking} onCancel={handleCancelBooking} />
        ))}

        {!loading && paginated.length === 0 && !featuredBooking ? (
          <div className="mb-empty-state">
            <h3>No bookings found for this tab.</h3>
            <p>Once you confirm a session, the booking card will appear here with the session, payment, and booking details.</p>
            <Link to="/sessions" className="mb-view-btn">Browse Sessions</Link>
          </div>
        ) : null}
      </div>
    </div>
  );
}