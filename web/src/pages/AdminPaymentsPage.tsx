import { useEffect, useState } from 'react';
import AppShell from '../components/AppShell';
import { useAuth } from '../context/AuthContext';
import { stillnessApi, type PaymentRecord, type PaymentSummary } from '../api/stillness';
import '../styles/AdminPaymentsPage.css';

export default function AdminPaymentsPage() {
  const { user } = useAuth();
  const [summary, setSummary] = useState<PaymentSummary>({ totalRevenue: 0, paidTransactions: 0, failedTransactions: 0 });
  const [records, setRecords] = useState<PaymentRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    stillnessApi.getAdminPayments()
      .then((data) => {
        setSummary(data.summary);
        setRecords(data.records);
      })
      .finally(() => setLoading(false));
  }, []);

  const filteredRecords = records.filter((record) => {
    const matchesSearch = !searchTerm || 
      record.bookingNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      record.userName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = !statusFilter || record.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getStatusBadgeClass = (status: string): string => {
    switch (status) {
      case 'PAID': return 'status-paid';
      case 'FAILED': return 'status-failed';
      case 'REFUNDED': return 'status-refunded';
      default: return 'status-pending';
    }
  };

  if (user?.role !== 'ROLE_INSTRUCTOR') {
    return (
      <AppShell title="Instructor Payments">
        <p className="inline-error">Access denied. Instructor role required.</p>
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="payments-header">
        <div className="payments-header-content">
          <div className="payments-header-text">
            <h1>Payment Records</h1>
            <p className="header-subtitle">Track and manage all payment transactions</p>
          </div>
          <button type="button" className="export-btn btn-lg">📥 Export CSV</button>
        </div>
      </section>

      <section className="summary-grid">
        <article className="summary-card">
          <p>Total Revenue</p>
          <h3>${summary.totalRevenue.toFixed(2)}</h3>
        </article>
        <article className="summary-card">
          <p>Paid Transactions</p>
          <h3>{summary.paidTransactions}</h3>
        </article>
        <article className="summary-card">
          <p>Failed Transactions</p>
          <h3>{summary.failedTransactions}</h3>
        </article>
      </section>

      <section className="payments-filters">
        <h3 className="filter-title">🔍 Filter Transactions</h3>
        <div className="filter-group">
          <input 
            type="text"
            className="search-input" 
            placeholder="Search by booking # or user name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <select 
            className="search-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Status</option>
            <option value="PAID">Paid</option>
            <option value="FAILED">Failed</option>
            <option value="REFUNDED">Refunded</option>
          </select>
          <button 
            type="button" 
            className="filter-btn-clear"
            onClick={() => {
              setSearchTerm('');
              setStatusFilter('');
            }}
          >
            Clear
          </button>
        </div>
      </section>

      {loading ? <p className="muted-copy">Loading payments...</p> : null}

      {!loading && filteredRecords.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">💳</div>
          <h3>No payments found</h3>
          <p>{searchTerm || statusFilter ? 'Try adjusting your filters' : 'No transactions recorded yet'}</p>
        </div>
      ) : (
        <div className="payments-table-wrap">
          <table className="payments-table">
            <thead>
              <tr>
                <th>Booking #</th>
                <th>User</th>
                <th>Session</th>
                <th>Amount</th>
                <th>Payment Method</th>
                <th>Transaction</th>
                <th>Date</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {filteredRecords.map((record) => (
                <tr key={record.id}>
                  <td><strong>{record.bookingNumber}</strong></td>
                  <td>{record.userName}</td>
                  <td>{record.sessionTitle}</td>
                  <td><strong>${record.amount.toFixed(2)}</strong></td>
                  <td>{record.cardMasked}</td>
                  <td><code style={{fontSize: '0.8rem', color: '#64748b'}}>{record.transactionId.slice(0, 12)}...</code></td>
                  <td>{new Date(record.date).toLocaleDateString()} {new Date(record.date).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})}</td>
                  <td>
                    <span className={`status-badge ${getStatusBadgeClass(record.status)}`}>
                      {record.status}
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
