import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';

export default function OAuth2CallbackPage() {
  const navigate = useNavigate();

  useEffect(() => {
    const initOAuth = async () => {
      const params = new URLSearchParams(window.location.search);
      const token = params.get('token');

      if (!token) {
        navigate('/login', { replace: true });
        return;
      }

      localStorage.setItem('token', token);
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');

      try {
        const me = await authApi.me();
        const role = me.data.data?.user.role;
        navigate(role === 'ROLE_INSTRUCTOR' ? '/admin/sessions' : '/sessions', { replace: true });
      } catch {
        navigate('/login', { replace: true });
      }
    };

    void initOAuth();
  }, [navigate]);

  return (
    <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center' }}>
      <p>Completing Google sign in...</p>
    </div>
  );
}