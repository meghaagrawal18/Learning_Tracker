'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useUser } from '@/lib/useUser';
import { homeFor } from '@/lib/session';

export default function Home() {
  const router = useRouter();
  const user = useUser();

  useEffect(() => {
    if (user === undefined) return;
    if (!user) router.replace('/login');
    else router.replace(user.mustChangePassword ? '/change-password' : homeFor(user.role));
  }, [user, router]);

  return (
    <div className="page">
      <p className="muted pad">Loading…</p>
    </div>
  );
}
