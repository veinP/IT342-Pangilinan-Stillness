import api from './axios';
import type { ApiResponse } from '../../features/auth/api';

export interface Quote {
  text: string;
  author: string;
  source?: string;
}

export const quotesApi = {
  async getRandomQuote(): Promise<Quote> {
    const fallbackQuotes: Quote[] = [
      { text: 'Peace comes from within. Do not seek it without.', author: 'Buddha', source: 'local' },
      { text: 'The quieter you become, the more you can hear.', author: 'Ram Dass', source: 'local' },
      { text: 'Stillness is where creativity and solutions are found.', author: 'StillNess', source: 'local' },
      { text: 'Inhale the future, exhale the past.', author: 'Unknown', source: 'local' },
    ];

    try {
      const res = await api.get<ApiResponse<{ quote?: Quote; text?: string; author?: string }>>('/quotes/random');
      
      if (!res.data.success || !res.data.data) {
        throw new Error('Failed');
      }
      
      const payload = res.data.data;
      if (payload.quote) {
        return payload.quote;
      }

      if (payload.text && payload.author) {
        return { text: payload.text, author: payload.author, source: 'api' };
      }
      return fallbackQuotes[0];
    } catch {
      return fallbackQuotes[Math.floor(Math.random() * fallbackQuotes.length)];
    }
  }
};
