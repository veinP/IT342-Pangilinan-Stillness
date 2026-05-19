import axios from 'axios';

const DEFAULT_API_BASE_PATH = '/api/v1';

function buildApiBaseUrl() {
  const configured = import.meta.env.VITE_API_BASE_URL?.trim();
  if (configured) {
    return configured.replace(/\/$/, '');
  }

  // Use same-origin API by default so OAuth redirects and API calls work behind dev proxy/reverse proxy.
  return `${window.location.origin}${DEFAULT_API_BASE_PATH}`;
}

export const API_BASE_URL = buildApiBaseUrl();

// Use window.location.origin so the proxy catches /oauth2, or in production it hits the correct root path
export const GOOGLE_OAUTH_URL = `${window.location.origin}/oauth2/authorization/google`;

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000, // 10 second timeout
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only redirect on 401 if it's not a login/register request (let those handle their own errors)
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/')) {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
