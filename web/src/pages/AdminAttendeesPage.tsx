import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import AppShell from '../components/AppShell';
import { useAuth } from '../context/AuthContext';
import { stillnessApi } from '../api/stillness';
import '../styles/AdminAttendeesPage.css';

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

  useEffect(() => {
    if (!sessionId) return;
    stillnessApi.getAdminAttendees(sessionId)
      .then(setAttendees)
      .finally(() => setLoading(false));
  }, [sessionId]);

  const getInitials = (name: string): string => {
    return name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
  };

  if (user?.role !== 'ROLE_INSTRUCTOR') {
    return (
      <AppShell title="Instructor Attendees">
        <p className="inline-error">Access denied. Instructor role required.</p>
      </AppShell>
    );
  }

  return (
    <AppShell title="Session Attendees" subtitle="View registrants and payment details for this session">
      <Link to="/admin/sessions" className="back-link">← Back to Sessions</Link>

      <section className="attendees-header">
        <div className="attendees-header-content">
          <div className="attendees-header-text">
            <h1>Session Attendees</h1>
            <p className="attendees-header-subtitle">View registrants and payment details for this session</p>
          </div>
          <button type="button" className="export-btn">📥 Export CSV</button>
        </div>
      </section>

      {loading ? <p className="muted-copy">Loading attendees...</p> : null}

      {!loading && attendees.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">👤</div>
          <h3>No attendees yet</h3>
          <p>Attendees will appear here once they book this session</p>
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
              {attendees.map((attendee) => (
                <tr key={attendee.bookingNumber}>
                  <td>
                    <div className="attendee-cell">
                      <div className="attendee-avatar">{getInitials(attendee.fullName)}</div>
                      <div className="attendee-info">
                        <h4>{attendee.fullName}</h4>
                      </div>
                    </div>
                  </td>
                  <td><a href={`mailto:${attendee.email}`} style={{color: '#3b82f6'}}>{attendee.email}</a></td>
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
