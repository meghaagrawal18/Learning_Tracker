import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useUser } from './useUser';
import { homeFor } from './session';

// Sends people to the right place. The server still enforces the real rules (401/403).
export function useRequireRole(role) {
  const router = useRouter();
  const user = useUser();

  useEffect(() => {
    if (user === undefined) return;
    if (user === null) router.replace('/login');
    else if (user.mustChangePassword) router.replace('/change-password');
    else if (user.role !== role) router.replace(homeFor(user.role));
  }, [user, role, router]);

  const ready = Boolean(user) && !user.mustChangePassword && user.role === role;
  return { ready, user: ready ? user : null };
}
