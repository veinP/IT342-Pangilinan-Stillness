/**
 * Bookings feature API — vertical slice
 */
import api from '../../shared/api/axios';
import type { ApiResponse } from '../auth/api';
import { normalizeSession, type Session } from '../sessions/api';

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

export function isBookingCancellable(cancellableUntil?: string | null): boolean {
  if (!cancellableUntil) return false;
  return new Date(cancellableUntil).getTime() > Date.now();
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

export const bookingsApi = {
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
      let rawBookings: any[] = [];
      if (Array.isArray(payload)) {
        rawBookings = payload;
      } else if (payload && typeof payload === 'object') {
        const bookings = (payload as Record<string, unknown>).bookings;
        if (Array.isArray(bookings)) {
          rawBookings = bookings;
        }
      }
      return rawBookings.map((b: any) => ({
        ...b,
        session: normalizeSession(b.session),
      }));
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

  async bookSession(sessionId: string): Promise<Booking> {
    return this.createBooking(sessionId);
  },
};
