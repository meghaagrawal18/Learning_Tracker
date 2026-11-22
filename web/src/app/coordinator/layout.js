'use client';

import TopBar from '@/components/TopBar';
import { useRequireRole } from '@/lib/useRequireRole';

export default function CoordinatorLayout({ children }) {
  const { ready } = useRequireRole('COORDINATOR');

  if (!ready) {
    return (
      <div className="page">
        <p className="muted pad">Loading…</p>
      </div>
    );
  }
  return (
    <div className="page">
      <TopBar variant="coordinator" />
      <main className="main wrap">{children}</main>
    </div>
  );
}
