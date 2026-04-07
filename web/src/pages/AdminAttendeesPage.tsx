import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import AppShell from '../components/AppShell';
import { useAuth } from '../context/AuthContext';
import { stillnessApi } from '../api/stillness';
import '../styles/AdminAttendeesPage.css';

const ChevronRight = () => (
  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
    <path d="M5.25 3.5L8.75 7l-3.5 3.5" stroke="#D1D5DB" strokeWidth="1.5"
      strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

interface Attendee {
  fullName: string;
  email: string;
  bookingNumber: string;
  status: string;
  paid: boolean;
}

export default function AdminAttendeesPage() {
  const { user } = useAuth();
  const { sessionId } = useParams<{ sessionId: string }>();
  const [attendees, setAttendees] = useState<Attendee[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [paymentFilter, setPaymentFilter] = useState('');

  useEffect(() => {
    if (!sessionId) return;
    stillnessApi.getAdminAttendees(sessionId)
      .then(setAttendees)
      .finally(() => setLoading(false));
  }, [sessionId]);

  const getInitials = (name: string): string => {
    return name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
  };

  const stats = useMemo(() => {
    return {
      total: attendees.length,
      confirmed: attendees.filter(a => a.status === 'CONFIRMED').length,
      pending: attendees.filter(a => a.status === 'PENDING').length,
      paid: attendees.filter(a => a.paid).length,
    };
  }, [attendees]);

  const filteredAttendees = useMemo(() => {
    return attendees.filter((attendee) => {
      const matchesSearch = !searchTerm || 
        attendee.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        attendee.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        attendee.bookingNumber.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesStatus = !statusFilter || attendee.status === statusFilter;
      const matchesPayment = !paymentFilter || 
        (paymentFilter === 'PAID' ? attendee.paid : !attendee.paid);
      return matchesSearch && matchesStatus && matchesPayment;
    });
  }, [attendees, searchTerm, statusFilter, paymentFilter]);

  if (user?.role !== 'ROLE_INSTRUCTOR') {
    return (
      <AppShell title="Instructor Attendees">
        <p className="inline-error">Access denied. Instructor role required.</p>
      </AppShell>
    );
  }

  return (
    <AppShell>
      <div className="attendees-breadcrumb">
        <Link to="/admin/sessions" className="attendees-breadcrumb__link">Sessions</Link>
        <span className="attendees-breadcrumb__sep"><ChevronRight /></span>
        <span className="attendees-breadcrumb__current">Attendees</span>
      </div>

      <section className="attendees-header">
        <div className="attendees-header-content">
          <div className="attendees-header-text">
            <h1>Session Attendees</h1>
            <p className="attendees-header-subtitle">View registrants and payment details for this session</p>
          </div>
          <button type="button" className="export-btn">📥 Export CSV</button>
        </div>
      </section>

      {!loading && (
        <section className="summary-grid">
          <article className="summary-card">
            <p>Total Attendees</p>
            <h3>{stats.total}</h3>
          </article>
          <article className="summary-card">
            <p>Confirmed</p>
            <h3>{stats.confirmed}</h3>
          </article>
          <article className="summary-card">
            <p>Pending</p>
            <h3>{stats.pending}</h3>
          </article>
          <article className="summary-card">
            <p>Paid</p>
            <h3>{stats.paid}</h3>
          </article>
        </section>
      )}

      <section className="attendees-filters">
        <h3 className="filter-title">🔍 Filter Attendees</h3>
        <div className="filter-group">
          <input 
            type="text"
            className="search-input" 
            placeholder="Search by name, email, or booking #..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button 
            type="button" 
            className="filter-btn-clear"
            onClick={() => {
              setSearchTerm('');
              setStatusFilter('');
              setPaymentFilter('');
            }}
          >
            Clear
          </button>
          <select 
            className="search-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Status</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="PENDING">Pending</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
          <select 
            className="search-select"
            value={paymentFilter}
            onChange={(e) => setPaymentFilter(e.target.value)}
          >
            <option value="">All Payment</option>
            <option value="PAID">Paid</option>
            <option value="PENDING">Pending</option>
          </select>
        </div>
      </section>

      {loading ? <p className="muted-copy">Loading attendees...</p> : null}

      {!loading && filteredAttendees.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">👤</div>
          <h3>No attendees found</h3>
          <p>{searchTerm || statusFilter || paymentFilter ? 'Try adjusting your filters' : 'Attendees will appear here once they book this session'}</p>
        </div>
      ) : (
        <div className="attendees-table-wrap">
          <table className="attendees-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Booking #</th>
                <th>Status</th>
                <th>Payment</th>
              </tr>
            </thead>
            <tbody>
              {filteredAttendees.map((attendee) => (
                <tr key={attendee.bookingNumber}>
                  <td>
                    <div className="attendee-cell">
                      <div className="attendee-avatar">{getInitials(attendee.fullName)}</div>
                      <div className="attendee-info">
                        <h4>{attendee.fullName}</h4>
                      </div>
                    </div>
                  </td>
                  <td><a href={`mailto:${attendee.email}`} className="email-link">{attendee.email}</a></td>
                  <td><strong>{attendee.bookingNumber}</strong></td>
                  <td>
                    <span className={`status-badge status-${attendee.status.toLowerCase()}`}>
                      {attendee.status}
                    </span>
                  </td>
                  <td>
                    <span className={`payment-badge ${attendee.paid ? 'payment-paid' : 'payment-pending'}`}>
                      {attendee.paid ? '✓ Paid' : '⏳ Pending'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </AppShell>
  );
}
