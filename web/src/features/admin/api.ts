/**
 * Admin feature API — vertical slice
 */
import api from '../../shared/api/axios';
import type { ApiResponse } from '../auth/api';
import type { Session } from '../sessions/api';
export type { Session };
import { sessionsApi } from '../sessions/api';

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

export type Attendee = {
  fullName: string;
  email: string;
  bookingNumber: string;
  status: string;
  paid: boolean;
};

function unwrap<T>(res: { data: ApiResponse<T> }): T {
  if (!res.data.success || res.data.data === null) {
    throw new Error(res.data.error?.message ?? 'Request failed');
  }
  return res.data.data;
}

export const adminApi = {
  async getAdminSessions(): Promise<Session[]> {
    const result = await sessionsApi.getSessions({ page: 0, limit: 100 });
    return result.sessions;
  },

  async getAdminAttendees(sessionId: string): Promise<Attendee[]> {
    try {
      const res = await api.get<ApiResponse<unknown>>(`/admin/sessions/${sessionId}/attendees`);
      const payload = unwrap(res);
      if (Array.isArray(payload)) return payload as Attendee[];
      if (payload && typeof payload === 'object') {
        const attendees = (payload as Record<string, unknown>).attendees;
        if (Array.isArray(attendees)) return attendees as Attendee[];
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
            totalRevenue: records.filter((r) => r.status === 'PAID').reduce((acc, r) => acc + r.amount, 0),
            paidTransactions: records.filter((r) => r.status === 'PAID').length,
            failedTransactions: records.filter((r) => r.status === 'FAILED').length,
          },
          records,
        };
      }
      return { summary: { totalRevenue: 0, paidTransactions: 0, failedTransactions: 0 }, records: [] };
    } catch {
      return { summary: { totalRevenue: 0, paidTransactions: 0, failedTransactions: 0 }, records: [] };
    }
  },
};
