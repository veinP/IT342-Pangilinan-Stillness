import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import AppShell from '../components/AppShell';
import { useAuth } from '../context/AuthContext';
import { stillnessApi, type Session } from '../api/stillness';
import '../styles/AdminSessionsPage.css';

interface SessionFormState {
  id?: string;
  title: string;
  type: string;
  location: string;
  instructor: string;
  capacity: string;
  description: string;
  price: string;
  startDate: string;
  startTime: string;
  endDate: string;
  endTime: string;
  thumbnail: File | null;
}

const initialFormState: SessionFormState = {
  title: '',
  type: 'Meditation',
  location: '',
  instructor: '',
  capacity: '',
  description: '',
  price: '0',
  startDate: '',
  startTime: '',
  endDate: '',
  endTime: '',
  thumbnail: null,
};

export default function AdminSessionsPage() {
  const { user } = useAuth();
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form, setForm] = useState<SessionFormState>(initialFormState);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [editingId, setEditingId] = useState<string | null>(null);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const instructorOptions = useMemo(() => {
    const set = new Set(sessions.map((s) => s.instructor?.fullName).filter(Boolean) as string[]);
    if (user?.fullName) set.add(user.fullName);
    return Array.from(set);
  }, [sessions, user?.fullName]);

  const sessionTypes = useMemo(() => {
    return Array.from(new Set(sessions.map((s) => s.type)));
  }, [sessions]);

  const filteredSessions = useMemo(() => {
    return sessions.filter((session) => {
      const matchesSearch = searchQuery === '' || 
        session.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        session.instructor.fullName.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesType = filterType === '' || session.type === filterType;
      const matchesAvailable = filterStatus === '' || (filterStatus === 'Active' ? session.available : !session.available);
      return matchesSearch && matchesType && matchesAvailable;
    });
  }, [sessions, searchQuery, filterType, filterStatus]);

  const stats = useMemo(() => {
    return {
      total: sessions.length,
      active: sessions.filter((s) => s.available).length,
      booked: sessions.reduce((sum, s) => sum + (s.bookedCount || 0), 0),
    };
  }, [sessions]);

  useEffect(() => {
    stillnessApi.getAdminSessions()
      .then(setSessions)
      .finally(() => setLoading(false));
  }, []);

  const openModal = (session?: Session) => {
    if (session) {
      setEditingId(session.id);
      setForm({
        id: session.id,
        title: session.title,
        type: session.type,
        location: session.location,
        instructor: session.instructor.fullName,
        capacity: String(session.capacity),
        description: session.description || '',
        price: String(session.price || 0),
        startDate: new Date(session.startTime).toISOString().split('T')[0],
        startTime: new Date(session.startTime).toTimeString().slice(0, 5),
        endDate: new Date(session.endTime).toISOString().split('T')[0],
        endTime: new Date(session.endTime).toTimeString().slice(0, 5),
        thumbnail: null,
      });
    } else {
      setEditingId(null);
      setForm((prev) => ({
        ...initialFormState,
        instructor: prev.instructor || user?.fullName || instructorOptions[0] || '',
      }));
    }
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingId(null);
    setForm(initialFormState);
  };

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleThumbnailChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] ?? null;
    setForm((prev) => ({ ...prev, thumbnail: file }));
  };

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.title || !form.capacity || !form.location || !form.startDate || !form.startTime || !form.endDate || !form.endTime) {
      setMessage({ type: 'error', text: 'Please fill in all required fields' });
      return;
    }

    const startDateTime = new Date(`${form.startDate}T${form.startTime}`);
    const endDateTime = new Date(`${form.endDate}T${form.endTime}`);
    const capacityNum = Number.parseInt(form.capacity, 10);
    const priceNum = Number.parseFloat(form.price);

    if (Number.isNaN(startDateTime.getTime()) || Number.isNaN(endDateTime.getTime()) || startDateTime >= endDateTime) {
      setMessage({ type: 'error', text: 'End time must be after start time' });
      return;
    }

    if (Number.isNaN(capacityNum) || capacityNum <= 0) {
      setMessage({ type: 'error', text: 'Capacity must be greater than 0' });
      return;
    }

    if (Number.isNaN(priceNum) || priceNum < 0) {
      setMessage({ type: 'error', text: 'Price cannot be negative' });
      return;
    }

    try {
      const startDateTimeValue = `${form.startDate}T${form.startTime}:00`;
      const endDateTimeValue = `${form.endDate}T${form.endTime}:00`;

      const payload = {
        title: form.title.trim(),
        description: form.description.trim(),
        sessionType: form.type,
        startTime: startDateTimeValue,
        endTime: endDateTimeValue,
        capacity: capacityNum,
        price: priceNum,
        location: form.location.trim(),
      };

      if (editingId) {
        await stillnessApi.updateSession(editingId, payload);
      } else {
        await stillnessApi.createSession(payload);
      }

      const refreshedSessions = await stillnessApi.getAdminSessions();
      setSessions(refreshedSessions);
      setMessage({ type: 'success', text: editingId ? 'Session updated successfully' : 'Session created successfully' });
      setTimeout(() => {
        closeModal();
        setMessage(null);
      }, 1500);
    } catch (error) {
      setMessage({ type: 'error', text: error instanceof Error ? error.message : 'Failed to save session' });
    }
  };

  const handleDelete = async (sessionId: string) => {
    if (window.confirm('Are you sure you want to delete this session? This action cannot be undone.')) {
      try {
        await stillnessApi.deleteSession(sessionId);
        setSessions((prev) => prev.filter((s) => s.id !== sessionId));
        setMessage({ type: 'success', text: 'Session deleted successfully' });
      } catch (error) {
        setMessage({ type: 'error', text: error instanceof Error ? error.message : 'Failed to delete session' });
      }
    }
  };

  const getCapacityLevel = (booked: number, capacity: number): string => {
    const percentage = (booked / capacity) * 100;
    if (percentage >= 80) return 'low';
    if (percentage >= 50) return 'medium';
    return 'high';
  };

  if (user?.role !== 'ROLE_INSTRUCTOR') {
    return (
      <AppShell title="Instructor Sessions">
        <p className="inline-error">Access denied. Instructor role required.</p>
      </AppShell>
    );
  }

  return (
    <AppShell>
      {message && (
        <div className={`alert alert-${message.type}`}>
          {message.text}
        </div>
      )}

      <section className="sessions-header">
        <div className="sessions-header-content">
          <div className="sessions-header-text">
            <h1>Manage Sessions</h1>
            <p className="header-subtitle">Create, edit, and manage your meditation and wellness sessions</p>
          </div>
          <button type="button" className="action-btn btn-lg btn-create" onClick={() => openModal()}>
            + Create New Session
          </button>
        </div>
      </section>

      <section className="stats-grid">
        <div className="stat-card">
          <div className="stat-value">{stats.total}</div>
          <div className="stat-label">Total Sessions</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{stats.active}</div>
          <div className="stat-label">Active Sessions</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{stats.booked}</div>
          <div className="stat-label">Total Bookings</div>
        </div>
      </section>

      <section className="sessions-filters">
        <div className="filter-group">
          <input 
            type="text"
            className="search-input" 
            placeholder="Search by title or instructor..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <button 
            type="button" 
            className="filter-btn-clear"
            onClick={() => {
              setSearchQuery('');
              setFilterType('');
              setFilterStatus('');
            }}
          >
            Clear
          </button>
          <select 
            className="search-select"
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
          >
            <option value="">All Types</option>
            {sessionTypes.map((type) => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
          <select 
            className="search-select"
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
          >
            <option value="">All Status</option>
            <option value="Active">Available</option>
            <option value="Inactive">Unavailable</option>
          </select>
        </div>
      </section>

      {loading && <p className="muted-copy">Loading sessions...</p>}
      {!loading && filteredSessions.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📋</div>
          <h3>No sessions found</h3>
          <p>{searchQuery || filterType || filterStatus ? 'Try adjusting your filters' : 'Create your first session to get started'}</p>
          <button type="button" className="action-btn" onClick={() => openModal()}>
            Create First Session
          </button>
        </div>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table sessions-table">
            <thead>
              <tr>
                <th>Session</th>
                <th>Type</th>
                <th>Instructor</th>
                <th>Date & Time</th>
                <th>Capacity</th>
                <th>Price</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredSessions.map((session) => (
                <tr key={session.id} className="sessions-table-row">
                  <td>
                    <div className="session-cell">
                      <div className="table-avatar">{session.type.slice(0, 2)}</div>
                      <div>
                        <div className="session-title">{session.title}</div>
                        <div className="session-location">{session.location}</div>
                      </div>
                    </div>
                  </td>
                  <td><span className="badge badge-type">{session.type}</span></td>
                  <td>{session.instructor.fullName}</td>
                  <td className="session-datetime">
                    <div>{new Date(session.startTime).toLocaleDateString()}</div>
                    <div className="time-info">{new Date(session.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
                  </td>
                  <td>
                    <div className="capacity-info">
                      <div className="capacity-number">
                        <span>{session.bookedCount}</span>
                        <span>/</span>
                        <span>{session.capacity}</span>
                      </div>
                      <div className="capacity-bar">
                        <div 
                          className={`capacity-fill ${getCapacityLevel(session.bookedCount, session.capacity)}`}
                          style={{ width: `${(session.bookedCount / session.capacity) * 100}%` }}
                        ></div>
                      </div>
                    </div>
                  </td>
                  <td>{session.price > 0 ? `$${session.price.toFixed(2)}` : <span className="badge badge-free">Free</span>}</td>
                  <td>
                    <span className={`badge ${session.available ? 'badge-active' : 'badge-inactive'}`}>
                      {session.available ? 'Available' : 'Unavailable'}
                    </span>
                  </td>
                  <td>
                    <div className="actions-cell">
                      <Link to={`/admin/sessions/${session.id}/attendees`} className="action-link" title="View attendees">
                        👥
                      </Link>
                      <button 
                        type="button" 
                        className="action-link" 
                        onClick={() => openModal(session)}
                        title="Edit"
                      >
                        ✏️
                      </button>
                      <button 
                        type="button" 
                        className="action-link action-link-delete" 
                        onClick={() => handleDelete(session.id)}
                        title="Delete"
                      >
                        🗑️
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {isModalOpen && (
        <div className="session-modal-overlay" onClick={closeModal} role="presentation">
          <div className="session-modal" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true" aria-label={editingId ? "Edit Session" : "Add New Session"}>
            <div className="session-modal__head">
              <h3>{editingId ? 'Edit Session' : 'Create New Session'}</h3>
              <button type="button" className="session-modal__close" onClick={closeModal} aria-label="Close">×</button>
            </div>

            {message && (
              <div className={`alert alert-${message.type} session-modal__alert`} aria-live="polite">
                {message.text}
              </div>
            )}

            <form className="session-modal__form" onSubmit={handleSave}>
              <div className="session-modal__field session-modal__field--full">
                <label htmlFor="title">Session Title *</label>
                <input
                  id="title"
                  name="title"
                  value={form.title}
                  onChange={handleChange}
                  placeholder="e.g., Morning Mindfulness Meditation"
                  required
                />
              </div>

              <div className="session-modal__grid">
                <div className="session-modal__col">
                  <div className="session-modal__field">
                    <label htmlFor="type">Session Type *</label>
                    <select id="type" name="type" value={form.type} onChange={handleChange} required>
                      <option>Meditation</option>
                      <option>Yoga</option>
                      <option>Breathwork</option>
                    </select>
                  </div>

                  <div className="session-modal__field">
                    <label htmlFor="instructor">Instructor *</label>
                    <select id="instructor" name="instructor" value={form.instructor} onChange={handleChange} required>
                      {instructorOptions.length === 0 ? <option value="">No instructors</option> : null}
                      {instructorOptions.map((name) => (
                        <option key={name} value={name}>{name}</option>
                      ))}
                    </select>
                  </div>

                  <div className="session-modal__field">
                    <label htmlFor="location">Location *</label>
                    <input
                      id="location"
                      name="location"
                      value={form.location}
                      onChange={handleChange}
                      placeholder="e.g., Studio A, Virtual, Outdoor Garden"
                      required
                    />
                  </div>

                  <div className="session-modal__field">
                    <label htmlFor="capacity">Maximum Capacity *</label>
                    <input id="capacity" name="capacity" type="number" min="1" value={form.capacity} onChange={handleChange} required />
                  </div>

                  <div className="session-modal__field">
                    <label htmlFor="price">Price ($)</label>
                    <input id="price" name="price" type="number" min="0" step="0.01" value={form.price} onChange={handleChange} placeholder="0 for free" />
                  </div>
                </div>

                <div className="session-modal__col">
                  <div className="session-modal__field">
                    <label htmlFor="startDate">Start Date *</label>
                    <input id="startDate" name="startDate" type="date" value={form.startDate} onChange={handleChange} required />
                  </div>

                  <div className="session-modal__field">
                    <label htmlFor="startTime">Start Time *</label>
                    <input id="startTime" name="startTime" type="time" value={form.startTime} onChange={handleChange} required />
                  </div>

                  <div className="session-modal__field">
                    <label htmlFor="endDate">End Date *</label>
                    <input id="endDate" name="endDate" type="date" value={form.endDate} onChange={handleChange} required />
                  </div>

                  <div className="session-modal__field">
                    <label htmlFor="endTime">End Time *</label>
                    <input id="endTime" name="endTime" type="time" value={form.endTime} onChange={handleChange} required />
                  </div>
                </div>
              </div>

              <div className="session-modal__field">
                <label htmlFor="description">Description</label>
                <textarea
                  id="description"
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  placeholder="Describe your session, benefits, and what participants should expect"
                  rows={4}
                />
              </div>

              <div className="session-modal__field">
                <label htmlFor="thumbnail">Session Thumbnail</label>
                <label className="session-modal__upload" htmlFor="thumbnail">
                  <span className="session-modal__upload-icon">📤</span>
                  <span>Click to upload or drag and drop</span>
                  <small>JPEG, PNG - max 5MB</small>
                  {form.thumbnail ? <em>✓ {form.thumbnail.name}</em> : null}
                </label>
                <input id="thumbnail" name="thumbnail" type="file" accept="image/png,image/jpeg" onChange={handleThumbnailChange} hidden />
              </div>

              <div className="session-modal__actions">
                <button type="button" className="ghost-btn" onClick={closeModal}>Cancel</button>
                <button type="submit" className="action-btn">
                  {editingId ? 'Update Session' : 'Create Session'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AppShell>
  );
}
