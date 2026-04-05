import type { ReactNode } from 'react';
import AppNav from '../pages/Appnav';
import { useAuth } from '../context/AuthContext';

interface AppShellProps {
  title?: string;
  subtitle?: string;
  children: ReactNode;
}

export default function AppShell({ title, subtitle, children }: AppShellProps) {
  const { user } = useAuth();
  const isInstructor = user?.role === 'ROLE_INSTRUCTOR';

  return (
    <div className={`app-shell${isInstructor ? ' app-shell--full' : ''}`}>
      <AppNav />

      <div className="app-main">
        {title ? (
          <header className="topbar">
            <h1>{title}</h1>
            {subtitle ? <p className="subtitle">{subtitle}</p> : null}
          </header>
        ) : null}

        <main className="page-content">{children}</main>
      </div>

      <footer className="sc-footer">
        <span className="sc-footer__copy">© 2026 StillNess. All rights reserved.</span>
        <div className="sc-footer__links">
          <a href="#">Privacy Policy</a>
          <a href="#">Terms</a>
          <a href="#">Contact</a>
        </div>
      </footer>
    </div>
  );
}
