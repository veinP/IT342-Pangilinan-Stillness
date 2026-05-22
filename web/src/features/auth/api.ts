/**
 * Auth feature API
 * Vertical slice: all auth-related API calls in one place.
 */
import api from '../../shared/api/axios';
import { GOOGLE_OAUTH_URL } from '../../shared/api/axios';
export { API_BASE_URL, GOOGLE_OAUTH_URL } from '../../shared/api/axios';

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: 'ROLE_USER' | 'ROLE_INSTRUCTOR';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserDto {
  id: string;
  email: string;
  fullName: string;
  role: string;
  profileImageUrl: string | null;
  createdAt: string;
}

export interface AuthResponse {
  user: UserDto;
  token: string;
  refreshToken: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: { code: string; message: string; details?: unknown } | null;
  timestamp: string;
}

export const authApi = {
  register(data: RegisterRequest) {
    return api.post<ApiResponse<AuthResponse>>('/auth/register', data);
  },
  login(data: LoginRequest) {
    return api.post<ApiResponse<AuthResponse>>('/auth/login', data);
  },
  logout() {
    return api.post<ApiResponse<{ message: string }>>('/auth/logout');
  },
  me() {
    return api.get<ApiResponse<{ user: UserDto }>>('/auth/me');
  },
};

export async function startGoogleOAuth() {
  const currentOrigin = window.location.origin;
  const oauthUrl = `${GOOGLE_OAUTH_URL}?frontend_url=${encodeURIComponent(currentOrigin)}`;

  try {
    if (window.top && window.top !== window) {
      window.top.location.assign(oauthUrl);
      return;
    }
  } catch {
    // Fall back to current frame navigation if top-level is inaccessible.
  }

  window.location.assign(oauthUrl);
}
