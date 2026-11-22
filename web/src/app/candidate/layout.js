'use client';

import TopBar from '@/components/TopBar';
import { useRequireRole } from '@/lib/useRequireRole';

export default function CandidateLayout({ children }) {
  const { ready } = useRequireRole('CANDIDATE');

  if (!ready) {
    return (
      <div className="page">
        <p className="muted pad">Loading…</p>
      </div>
    );
  }
  return (
    <div className="page">
      <TopBar variant="candidate" />
      <main className="main wrap">{children}</main>
    </div>
  );
}
