/**
 * Sessions feature API — vertical slice
 */
import api from '../../shared/api/axios';
import { API_BASE_URL } from '../../shared/api/axios';
import type { ApiResponse } from '../auth/api';

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
  status?: string;
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

function resolveThumbnailUrl(url: string | null | undefined): string | null {
  if (!url) return null;
  // If it's already absolute or a data URL, keep it as-is
  if (url.startsWith('http') || url.startsWith('data:')) return url;
  // It's a relative path like /sessions/{id}/thumbnail — resolve against API base
  return `${API_BASE_URL}${url}`;
}

export function normalizeSession(raw: Partial<Session> & { id?: string; title?: string }): Session {
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
    thumbnailUrl: resolveThumbnailUrl(raw.thumbnailUrl),
    location: raw.location ?? 'StillNess Center',
    address: raw.address,
    duration: raw.duration,
    available,
    status: raw.status ?? 'ACTIVE',
  };
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
  if (Array.isArray(data)) return (data as Partial<Session>[]).map(normalizeSession);
  return [];
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
    if (entries.length > 0) return `${apiError.message ?? fallback} (${entries.join(', ')})`;
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

export const sessionsApi = {
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
      return { sessions, pagination: { page: 0, limit: 20, total: sessions.length, pages: 1 } };
    } catch {
      return { sessions: [], pagination: { page: 0, limit: 0, total: 0, pages: 0 } };
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

  async createSession(payload: CreateSessionPayload): Promise<Session> {
    try {
      const res = await api.post<ApiResponse<unknown>>('/sessions', payload);
      const data = unwrap(res);
      if (data && typeof data === 'object') return normalizeSession(data as Partial<Session>);
      return normalizeSession({ title: payload.title });
    } catch (err: unknown) {
      throw new Error(formatApiError(err, 'Failed to create session.'));
    }
  },

  async updateSession(sessionId: string, payload: CreateSessionPayload): Promise<Session> {
    try {
      const res = await api.put<ApiResponse<unknown>>(`/sessions/${sessionId}`, payload);
      const data = unwrap(res);
      if (data && typeof data === 'object') return normalizeSession(data as Partial<Session>);
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

  async uploadSessionThumbnail(sessionId: string, file: File): Promise<Session> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await api.post<ApiResponse<unknown>>(`/sessions/${sessionId}/thumbnail`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 30000, // longer timeout for file uploads
      });
      const data = unwrap(res);
      if (data && typeof data === 'object') return normalizeSession(data as Partial<Session>);
      return normalizeSession({ id: sessionId });
    } catch (err: unknown) {
      throw new Error(formatApiError(err, 'Failed to upload thumbnail.'));
    }
  },
};
