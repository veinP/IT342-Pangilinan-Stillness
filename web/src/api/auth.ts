import api from './axios';

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
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
