import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { stillnessApi, type Session } from '../api/stillness';
import AppNav from './Appnav';
import '../styles/BookingCheckoutPage.css';

const ChevronRight = () => (
  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
    <path d="M5.25 3.5L8.75 7l-3.5 3.5" stroke="#D1D5DB" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

const CalendarIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <rect x="2" y="3" width="14" height="13" rx="2" stroke="#6B7280" strokeWidth="1.4" />
    <path d="M6 1.5V4.5M12 1.5V4.5M2 7.5h14" stroke="#6B7280" strokeWidth="1.4" strokeLinecap="round" />
  </svg>
);

const ClockIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <circle cx="9" cy="9" r="7" stroke="#6B7280" strokeWidth="1.4" />
    <path d="M9 5v4.5l2.5 1.5" stroke="#6B7280" strokeWidth="1.4" strokeLinecap="round" />
  </svg>
);

const PinIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <path d="M9 1.5A5.5 5.5 0 0 1 14.5 7c0 3.5-5.5 9.5-5.5 9.5S3.5 10.5 3.5 7A5.5 5.5 0 0 1 9 1.5z" stroke="#6B7280" strokeWidth="1.4" />
    <circle cx="9" cy="7" r="2" stroke="#6B7280" strokeWidth="1.4" />
  </svg>
);

const InfoIcon = () => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
    <circle cx="8" cy="8" r="6.5" stroke="#9CA3AF" strokeWidth="1.3" />
    <path d="M8 7.5v4" stroke="#9CA3AF" strokeWidth="1.3" strokeLinecap="round" />
    <circle cx="8" cy="5.5" r=".8" fill="#9CA3AF" />
  </svg>
);

function formatSessionDate(iso?: string): string {
  if (!iso) return 'Schedule to be announced';

  return new Date(iso).toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  });
}

function formatSessionTime(startIso?: string, endIso?: string): string {
  if (!startIso) return 'Time to be announced';

  const start = new Date(startIso);
  const end = endIso ? new Date(endIso) : new Date(start.getTime() + 60 * 60 * 1000);

  const formatTime = (date: Date) => date.toLocaleTimeString('en-US', {
    hour: 'numeric',
    minute: '2-digit',
  });

  return `${formatTime(start)} - ${formatTime(end)}`;
}

function buildPaymentIntentId(): string {
  return `pi_demo_${Date.now()}`;
}

function getBadgeClass(type = ''): string {
  const normalized = type.toLowerCase();
  if (normalized.includes('yoga')) return 'bc-badge--yoga';
  if (normalized.includes('breath')) return 'bc-badge--breathwork';
  return 'bc-badge--meditation';
}

function initials(name = ''): string {
  return name.split(' ').map((part) => part[0] ?? '').join('').slice(0, 2).toUpperCase() || 'SN';
}

export default function BookingCheckoutPage() {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();

  const [session, setSession] = useState<Session | null>(null);
  const [loading, setLoading] = useState(true);
  const [pageError, setPageError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [cardholderName, setCardholderName] = useState('');
  const [cardNumber, setCardNumber] = useState('4242 4242 4242 4242');
  const [expiry, setExpiry] = useState('');
  const [cvv, setCvv] = useState('');
  const [saveCard, setSaveCard] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [confirmationMessage, setConfirmationMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!sessionId) {
      setPageError('Session not found.');
      setLoading(false);
      return;
    }

    let active = true;

    stillnessApi.getSessionById(sessionId)
      .then((result) => {
        if (!active) return;
        setSession(result);
        setCardholderName((current) => current || result.instructor?.fullName || 'StillNess Guest');
      })
      .catch((error: unknown) => {
        if (!active) return;
        const message = error instanceof Error ? error.message : 'Failed to load checkout details.';
        setPageError(message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [sessionId]);

  const totals = useMemo(() => {
    const fee = session?.price ?? 0;
    const tax = fee * 0.09;
    return { fee, tax, total: fee + tax };
  }, [session]);

  const isFreeSession = totals.total === 0;

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!session) {
      setSubmitError('Session details are unavailable.');
      return;
    }

    setProcessing(true);
    setSubmitError(null);

    try {
      const booking = await stillnessApi.createBooking(session.id);

      if (booking.paymentStatus === 'PENDING' && booking.amount > 0) {
        await stillnessApi.confirmPayment(buildPaymentIntentId(), booking.id);
      }

      setConfirmationMessage(`Booking confirmed for ${session.title}.`);
      navigate('/bookings');
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Unable to complete booking.';
      setSubmitError(message);
    } finally {
      setProcessing(false);
    }
  };

  if (loading) {
    return (
      <div className="bc-page">
        <AppNav />
        <div className="bc-state">Loading checkout details...</div>
      </div>
    );
  }

  if (pageError || !session) {
    return (
      <div className="bc-page">
        <AppNav />
        <div className="bc-state bc-state--error">{pageError ?? 'Session not found.'}</div>
      </div>
    );
  }

  return (
    <div className="bc-page">
      <AppNav />

      {/* Header Section */}
      <div className="bc-header">
        <div className="bc-header-content">
          <div className="bc-header-text">
            <h1>Book Your Session</h1>
            <p className="bc-header-subtitle">Complete your booking and secure your spot</p>
          </div>
        </div>
      </div>

      <div className="bc-breadcrumb">
        <Link to="/sessions" className="bc-breadcrumb__link">Sessions</Link>
        <span className="bc-breadcrumb__sep"><ChevronRight /></span>
        <Link to={`/sessions/${session.id}`} className="bc-breadcrumb__link">{session.title}</Link>
        <span className="bc-breadcrumb__sep"><ChevronRight /></span>
        <span className="bc-breadcrumb__current">Checkout</span>
      </div>

      <div className="bc-main">
        <div className="bc-left">
          <section className="bc-hero">
            <span className={`bc-badge ${getBadgeClass(session.type)}`}>{session.type || 'Wellness'}</span>
            <div className="bc-hero__body">
              <p className="bc-hero__eyebrow">Confirm your reservation</p>
              <h1 className="bc-hero__title">{session.title}</h1>
              <p className="bc-hero__copy">
                Review the session details, choose how you want to pay, and secure your spot before the class fills up.
              </p>
            </div>
          </section>

          <section className="bc-section bc-section--card">
            <h2 className="bc-section__heading">Session Overview</h2>

            <div className="bc-summary-grid">
              <div className="bc-detail-row">
                <span className="bc-detail-row__icon"><CalendarIcon /></span>
                <div>
                  <p className="bc-detail-row__label">Date</p>
                  <p className="bc-detail-row__value">{formatSessionDate(session.startTime)}</p>
                </div>
              </div>

              <div className="bc-detail-row">
                <span className="bc-detail-row__icon"><ClockIcon /></span>
                <div>
                  <p className="bc-detail-row__label">Time</p>
                  <p className="bc-detail-row__value">{formatSessionTime(session.startTime, session.endTime)}</p>
                </div>
              </div>

              <div className="bc-detail-row">
                <span className="bc-detail-row__icon"><PinIcon /></span>
                <div>
                  <p className="bc-detail-row__label">Location</p>
                  <p className="bc-detail-row__value">{session.location || 'StillNess Studio'}</p>
                </div>
              </div>

              <div className="bc-detail-row">
                <span className="bc-detail-row__icon"><InfoIcon /></span>
                <div>
                  <p className="bc-detail-row__label">Availability</p>
                  <p className="bc-detail-row__value">{Math.max((session.capacity ?? 0) - (session.bookedCount ?? 0), 0)} spots left</p>
                </div>
              </div>
            </div>

            <p className="bc-section__body">
              {session.description || 'A guided wellness session crafted to help you reset, refocus, and recharge.'}
            </p>
          </section>

          <section className="bc-section bc-section--card">
            <h2 className="bc-section__heading">Instructor</h2>
            <div className="bc-instructor">
              <div className="bc-instructor__avatar">{initials(session.instructor?.fullName)}</div>
              <div>
                <p className="bc-instructor__name">{session.instructor?.fullName || 'StillNess Team'}</p>
                <p className="bc-instructor__meta">{session.instructor?.specialty || session.type || 'Wellness Guide'}</p>
              </div>
            </div>
          </section>
        </div>

        <aside className="bc-checkout-card">
          <h2 className="bc-checkout-card__title">Checkout</h2>

          <div className="bc-mode-banner">
            <span className="bc-mode-banner__icon">ⓘ</span>
            {isFreeSession ? 'No payment required for this class.' : 'Sandbox card entry for checkout preview.'}
          </div>

          <div className="bc-price-block">
            <div className="bc-price-row">
              <span>Session Fee</span>
              <span>{totals.fee > 0 ? `$${totals.fee.toFixed(2)}` : 'Free'}</span>
            </div>
            <div className="bc-price-row">
              <span>Tax</span>
              <span>${totals.tax.toFixed(2)}</span>
            </div>
            <div className="bc-price-row bc-price-row--total">
              <span>Total</span>
              <span>${totals.total.toFixed(2)}</span>
            </div>
          </div>

          {submitError ? <div className="bc-alert bc-alert--error">{submitError}</div> : null}
          {confirmationMessage ? <div className="bc-alert bc-alert--success">{confirmationMessage}</div> : null}

          <form className="bc-form" onSubmit={handleSubmit}>
            <div className="bc-input-group">
              <label htmlFor="cardholderName">Cardholder Name</label>
              <input
                id="cardholderName"
                type="text"
                value={cardholderName}
                onChange={(e) => setCardholderName(e.target.value)}
                disabled={processing || isFreeSession}
              />
            </div>

            <div className="bc-input-group">
              <label htmlFor="cardNumber">Card Number</label>
              <div className="bc-card-input-wrapper">
                <input
                  id="cardNumber"
                  type="text"
                  value={cardNumber}
                  onChange={(e) => setCardNumber(e.target.value)}
                  disabled={processing || isFreeSession}
                />
                <span className="bc-card-input-wrapper__icon">💳</span>
              </div>
            </div>

            <div className="bc-form-row">
              <div className="bc-input-group">
                <label htmlFor="expiry">Expiry Date</label>
                <input
                  id="expiry"
                  type="text"
                  placeholder="MM/YY"
                  value={expiry}
                  onChange={(e) => setExpiry(e.target.value)}
                  disabled={processing || isFreeSession}
                />
              </div>

              <div className="bc-input-group">
                <label htmlFor="cvv">CVV</label>
                <input
                  id="cvv"
                  type="text"
                  placeholder="123"
                  value={cvv}
                  onChange={(e) => setCvv(e.target.value)}
                  disabled={processing || isFreeSession}
                />
              </div>
            </div>

            <label className="bc-checkbox-row" htmlFor="saveCard">
              <input
                id="saveCard"
                type="checkbox"
                checked={saveCard}
                onChange={(e) => setSaveCard(e.target.checked)}
                disabled={processing || isFreeSession}
              />
              <span>Save card for future bookings</span>
            </label>

            <button type="submit" className="bc-submit" disabled={processing}>
              {processing ? 'Processing...' : isFreeSession ? 'Confirm Free Booking' : 'Pay & Confirm Booking'}
            </button>

            <p className="bc-secure-copy">
              {isFreeSession ? 'This reservation will be confirmed instantly.' : 'Secured by Stripe in sandbox mode.'}
            </p>
          </form>
        </aside>
      </div>

      <footer className="bc-footer">
        <span className="bc-footer__copy">© 2026 StillNess. All rights reserved.</span>
        <div className="bc-footer__links">
          <a href="#">Privacy Policy</a>
          <a href="#">Terms</a>
          <a href="#">Contact</a>
        </div>
      </footer>
    </div>
  );
}