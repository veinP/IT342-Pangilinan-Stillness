import api from './axios';
import { GOOGLE_OAUTH_URL } from './axios';

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
  try {
    if (window.top && window.top !== window) {
      window.top.location.assign(GOOGLE_OAUTH_URL);
      return;
    }
  } catch {
    // If top-level window is inaccessible, fall back to current frame navigation.
  }

  window.location.assign(GOOGLE_OAUTH_URL);
}
