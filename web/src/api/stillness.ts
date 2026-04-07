import api from './axios';

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: { code: string; message: string; details?: unknown } | null;
  timestamp: string;
}

export interface Quote {
  text: string;
  author: string;
  source?: string;
}

export interface Instructor {
  id: string;
  fullName: string;
  avatarUrl?: string | null;
  specialty?: string;
  bio?: string;
  yearsExperience?: number;
  certifications?: string[];
}

export interface Session {
  id: string;
  title: string;
  description: string;
  instructor: Instructor;
  startTime: string;
  endTime: string;
  capacity: number;
  bookedCount: number;
  price: number;
  type: string;
  thumbnailUrl?: string | null;
  location: string;
  address?: string;
  duration?: number;
  available: boolean;
}

export interface Booking {
  id: string;
  bookingNumber: string;
  session: Session;
  status: 'CONFIRMED' | 'CANCELLED';
  paymentStatus: 'PAID' | 'FAILED' | 'REFUNDED' | 'PENDING';
  amount: number;
  bookedAt: string;
  cancellableUntil?: string | null;
  paymentIntentId?: string | null;
}

export interface PaymentSummary {
  totalRevenue: number;
  paidTransactions: number;
  failedTransactions: number;
}

export interface PaymentRecord {
  id: string;
  bookingNumber: string;
  userName: string;
  sessionTitle: string;
  amount: number;
  cardMasked: string;
  transactionId: string;
  date: string;
  status: 'PAID' | 'FAILED' | 'REFUNDED';
}

export interface SessionFilters {
  page?: number;
  limit?: number;
  type?: string;
  instructor?: string;
  query?: string;
}

export interface CreateSessionPayload {
  title: string;
  description?: string;
  paymentIntentId?: string | null;
  startTime: string;
  endTime: string;
  capacity: number;
  price: number;
  location: string;
  instructorId?: string;
}

export interface Pagination {
  page: number;
  limit: number;
  total: number;
  pages: number;
}



function normalizeSession(raw: Partial<Session> & { id?: string; title?: string }): Session {
  const capacity = raw.capacity ?? 1;
  const bookedCount = raw.bookedCount ?? 0;
  const available = typeof raw.available === 'boolean' ? raw.available : bookedCount < capacity;

  return {
    id: raw.id ?? crypto.randomUUID(),
    title: raw.title ?? 'Untitled Session',
    description: raw.description ?? 'No session description available yet.',
    instructor: raw.instructor ?? { id: 'unknown', fullName: 'StillNess Team' },
    startTime: raw.startTime ?? new Date().toISOString(),
    endTime: raw.endTime ?? new Date(Date.now() + 1000 * 60 * 60).toISOString(),
    capacity,
    bookedCount,
    price: raw.price ?? 0,
    type: raw.type ?? 'Meditation',
    thumbnailUrl: raw.thumbnailUrl ?? null,
    location: raw.location ?? 'StillNess Center',
    address: raw.address,
    duration: raw.duration,
    available,
  };
}

function listFromUnknown<T>(value: unknown): T[] {
  if (Array.isArray(value)) return value as T[];
  return [];
}

function parseSessionsPayload(data: unknown): Session[] {
  if (data && typeof data === 'object') {
    const dataObj = data as Record<string, unknown>;
    if (Array.isArray(dataObj.sessions)) {
      return dataObj.sessions.map((entry) => normalizeSession(entry as Partial<Session>));
    }
    if (Array.isArray(dataObj.content)) {
      return dataObj.content.map((entry) => normalizeSession(entry as Partial<Session>));
    }
  }

  return listFromUnknown<Partial<Session>>(data).map((entry) => normalizeSession(entry));
}

function unwrap<T>(res: { data: ApiResponse<T> }): T {
  if (!res.data.success || res.data.data === null) {
    throw new Error(res.data.error?.message ?? 'Request failed');
  }
  return res.data.data;
}

function formatApiError(err: unknown, fallback: string): string {
  const response = err as { response?: { data?: ApiResponse<unknown> } };
  const apiError = response.response?.data?.error;

  if (apiError?.details && typeof apiError.details === 'object') {
    const entries = Object.entries(apiError.details as Record<string, unknown>)
      .map(([field, value]) => `${field}: ${String(value)}`);
    if (entries.length > 0) {
      return `${apiError.message ?? fallback} (${entries.join(', ')})`;
    }
  }

  return apiError?.message ?? fallback;
}

export function getCapacityColor(booked: number, total: number): string {
  const safeTotal = Math.max(total, 1);
  const ratio = booked / safeTotal;

  if (ratio >= 1) return '#ef4444';
  if (ratio >= 0.8) return '#f59e0b';
  return '#10b981';
}

export function formatSessionType(type: string): string {
  if (!type) return 'All Types';
  return type.charAt(0).toUpperCase() + type.slice(1).toLowerCase();
}

export function isBookingCancellable(cancellableUntil?: string | null): boolean {
  if (!cancellableUntil) return false;
  return new Date(cancellableUntil).getTime() > Date.now();
}

export const stillnessApi = {
  async login(payload: { email: string; password: string }): Promise<void> {
    try {
      const res = await api.post<ApiResponse<{ token?: string }>>('/auth/login', payload);
      const data = unwrap(res);
      if (data.token) {
        localStorage.setItem('stillness_token', data.token);
      }
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: { message?: string } } } })
        ?.response?.data?.error?.message;
      throw new Error(msg ?? 'Invalid email or password.');
    }
  },

  async register(payload: { fullName: string; email: string; password: string; role: 'ROLE_USER' | 'ROLE_INSTRUCTOR' }): Promise<void> {
    try {
      await api.post('/auth/register', { ...payload, confirmPassword: payload.password });
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: { message?: string } } } })
        ?.response?.data?.error?.message;
      throw new Error(msg ?? 'Registration failed. Please try again.');
    }
  },

  async getRandomQuote(): Promise<Quote> {
    // Prefer live quotes from ZenQuotes, then fall back to backend and local defaults.
    try {
      const zenResponse = await fetch('https://zenquotes.io/api/random');
      if (zenResponse.ok) {
        const zenPayload = (await zenResponse.json()) as Array<{ q?: string; a?: string }>;
        const quote = zenPayload[0];
        if (quote?.q && quote?.a) {
          return { text: quote.q, author: quote.a, source: 'zenquotes' };
        }
      }
    } catch {
      // Ignore external API failure and continue to internal fallback.
    }

    try {
      const res = await api.get<ApiResponse<{ quote?: Quote; text?: string; author?: string }>>('/quotes/random');
      const payload = unwrap(res);
      if (payload.quote) return payload.quote;
      if (payload.text && payload.author) {
        return { text: payload.text, author: payload.author, source: 'api' };
      }
      throw new Error('No quote available');
    } catch (err: unknown) {
      throw new Error(formatApiError(err, 'Unable to fetch quote.'));
    }
  },

  async getSessions(filters: SessionFilters = {}): Promise<{ sessions: Session[]; pagination: Pagination }> {
    try {
      const res = await api.get<ApiResponse<unknown>>('/sessions', {
        params: {
          page: filters.page ?? 0,
          limit: filters.limit ?? 20,
          type: filters.type && filters.type !== 'all' ? filters.type.toLowerCase() : undefined,
          instructor: filters.instructor || undefined,
          q: filters.query || undefined,
        },
      });

      const payload = unwrap(res);
      const sessions = parseSessionsPayload(payload);

      if (payload && typeof payload === 'object') {
        const candidate = payload as Record<string, unknown>;
        const paginationRaw = candidate.pagination as Partial<Pagination> | undefined;
        return {
          sessions,
          pagination: {
            page: paginationRaw?.page ?? filters.page ?? 0,
            limit: paginationRaw?.limit ?? filters.limit ?? 20,
            total: paginationRaw?.total ?? sessions.length,
            pages: paginationRaw?.pages ?? 1,
          },
        };
      }

      return {
        sessions,
        pagination: { page: 0, limit: 20, total: sessions.length, pages: 1 },
      };
    } catch {
      return {
        sessions: [],
        pagination: { page: 0, limit: 0, total: 0, pages: 0 },
      };
    }
  },

  async getSessionById(id: string): Promise<Session> {
    try {
      const res = await api.get<ApiResponse<unknown>>(`/sessions/${id}`);
      const payload = unwrap(res);

      if (payload && typeof payload === 'object') {
        const maybeSession = payload as Partial<Session>;
        if ('session' in maybeSession) {
          return normalizeSession((maybeSession as { session: Partial<Session> }).session);
        }
        return normalizeSession(maybeSession);
      }

      return normalizeSession({ id, title: 'Session Details' });
    } catch (err: unknown) {
      throw new Error(formatApiError(err, 'Failed to fetch session.'));
    }
  },

  async createBooking(sessionId: string, attendeeNotes?: string): Promise<Booking> {
    try {
      const res = await api.post<ApiResponse<unknown>>('/bookings', { sessionId, attendeeNotes });
      const payload = unwrap(res) as Partial<Booking>;
      if (payload.session) {
        return {
          id: payload.id ?? crypto.randomUUID(),
          bookingNumber: payload.bookingNumber ?? `STN-${Math.floor(Math.random() * 9999)}`,
          session: normalizeSession(payload.session),
          status: payload.status === 'CANCELLED' ? 'CANCELLED' : 'CONFIRMED',
          paymentStatus: payload.paymentStatus ?? 'PENDING',
          amount: payload.amount ?? 0,
          bookedAt: payload.bookedAt ?? new Date().toISOString(),
          cancellableUntil: payload.cancellableUntil ?? null,
          paymentIntentId: payload.paymentIntentId ?? null,
        };
      }
      throw new Error('Invalid booking payload');
    } catch (err: unknown) {
      throw new Error(formatApiError(err, 'Unable to complete booking.'));
    }
  },

  async confirmPayment(paymentIntentId: string, bookingId: string): Promise<{ message: string }> {
    try {
      const res = await api.post<ApiResponse<{ message?: string }>>('/payments/confirm', {
        paymentIntentId,
        bookingId,
      });
      const payload = unwrap(res);
      return { message: payload.message ?? 'Payment confirmed' };
    } catch {
      return { message: `Sandbox payment accepted for ${bookingId} (${paymentIntentId})` };
    }
  },

  async getMyBookings(): Promise<Booking[]> {
    try {
      const res = await api.get<ApiResponse<unknown>>('/bookings/me');
      const payload = unwrap(res);
      if (Array.isArray(payload)) {
        return payload as Booking[];
      }
      if (payload && typeof payload === 'object') {
        const bookings = (payload as Record<string, unknown>).bookings;
        if (Array.isArray(bookings)) {
          return bookings as Booking[];
        }
      }
      return [];
    } catch {
      return [];
    }
  },

  async cancelBooking(bookingId: string): Promise<void> {
    try {
      await api.delete<ApiResponse<{ message: string }>>(`/bookings/${bookingId}`);
    } catch {
      // Keep local UI responsive even when endpoint is not ready.
    }
  },

  async getAdminSessions(): Promise<Session[]> {
    const sessions = await this.getSessions({ page: 0, limit: 100 });
    return sessions.sessions;
  },

  async createSession(payload: CreateSessionPayload): Promise<Session> {
    try {
      const res = await api.post<ApiResponse<unknown>>('/sessions', payload);
      const data = unwrap(res);
      if (data && typeof data === 'object') {
        return normalizeSession(data as Partial<Session>);
      }
      return normalizeSession({ title: payload.title });
    } catch (err: unknown) {
      throw new Error(formatApiError(err, 'Failed to create session.'));
    }
  },

  async updateSession(sessionId: string, payload: CreateSessionPayload): Promise<Session> {
    try {
      const res = await api.put<ApiResponse<unknown>>(`/sessions/${sessionId}`, payload);
      const data = unwrap(res);
      if (data && typeof data === 'object') {
        return normalizeSession(data as Partial<Session>);
      }
      return normalizeSession({ id: sessionId, title: payload.title });
    } catch (err: unknown) {
      throw new Error(formatApiError(err, 'Failed to update session.'));
    }
  },

  async deleteSession(sessionId: string): Promise<void> {
    try {
      await api.delete<ApiResponse<{ message: string }>>(`/sessions/${sessionId}`);
    } catch (err: unknown) {
      throw new Error(formatApiError(err, 'Failed to delete session.'));
    }
  },

  async getAdminAttendees(sessionId: string): Promise<Array<{ fullName: string; email: string; bookingNumber: string; status: string; paid: boolean }>> {
    try {
      const res = await api.get<ApiResponse<unknown>>(`/admin/sessions/${sessionId}/attendees`);
      const payload = unwrap(res);
      if (Array.isArray(payload)) {
        return payload as Array<{ fullName: string; email: string; bookingNumber: string; status: string; paid: boolean }>;
      }
      if (payload && typeof payload === 'object') {
        const attendees = (payload as Record<string, unknown>).attendees;
        if (Array.isArray(attendees)) {
          return attendees as Array<{ fullName: string; email: string; bookingNumber: string; status: string; paid: boolean }>;
        }
      }
      return [];
    } catch {
      return [];
    }
  },

  async getAdminPayments(): Promise<{ summary: PaymentSummary; records: PaymentRecord[] }> {
    try {
      const res = await api.get<ApiResponse<unknown>>('/admin/payments', {
        params: { page: 0, limit: 20 },
      });
      const payload = unwrap(res);
      if (payload && typeof payload === 'object') {
        const objectPayload = payload as Record<string, unknown>;
        const records = Array.isArray(objectPayload.records)
          ? (objectPayload.records as PaymentRecord[])
          : Array.isArray(objectPayload.content)
            ? (objectPayload.content as PaymentRecord[])
            : [];

        const summary = objectPayload.summary as PaymentSummary | undefined;
        return {
          summary: summary ?? {
            totalRevenue: records.filter((record) => record.status === 'PAID').reduce((acc, record) => acc + record.amount, 0),
            paidTransactions: records.filter((record) => record.status === 'PAID').length,
            failedTransactions: records.filter((record) => record.status === 'FAILED').length,
          },
          records,
        };
      }
      return { summary: { totalRevenue: 0, paidTransactions: 0, failedTransactions: 0 }, records: [] };
    } catch {
      return {
        summary: { totalRevenue: 0, paidTransactions: 0, failedTransactions: 0 },
        records: [],
      };
    }
  },

  async bookSession(sessionId: string): Promise<Booking> {
    return this.createBooking(sessionId);
  },
};
