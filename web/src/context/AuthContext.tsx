import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { authApi, type UserDto, type LoginRequest, type RegisterRequest } from '../api/auth';

interface AuthContextType {
  user: UserDto | null;
  loading: boolean;
  login: (data: LoginRequest) => Promise<UserDto>;
  register: (data: RegisterRequest) => Promise<UserDto>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      authApi.me()
        .then((res) => {
          if (res.data.success && res.data.data) {
            setUser(res.data.data.user);
          }
        })
        .catch(() => {
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (data: LoginRequest) => {
    try {
      const res = await authApi.login(data);
      if (res.data.success && res.data.data) {
        const { user, token, refreshToken } = res.data.data;
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(user));
        setUser(user);
        return user;
      } else {
        throw new Error(res.data.error?.message || 'Login failed');
      }
    } catch (error: any) {
      // If it's an axios error, try to extract the API error message
      if (error?.response?.data?.error?.message) {
        throw new Error(error.response.data.error.message);
      }
      // Otherwise re-throw the original error
      throw error;
    }
  };

  const register = async (data: RegisterRequest) => {
    try {
      const res = await authApi.register(data);
      if (res.data.success && res.data.data) {
        const { user, token, refreshToken } = res.data.data;
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(user));
        setUser(user);
        return user;
      } else {
        throw new Error(res.data.error?.message || 'Registration failed');
      }
    } catch (error: any) {
      // If it's an axios error, try to extract the API error message
      if (error?.response?.data?.error?.message) {
        throw new Error(error.response.data.error.message);
      }
      // Otherwise re-throw the original error
      throw error;
    }
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
