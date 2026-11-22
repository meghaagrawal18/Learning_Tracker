'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { logout } from '@/lib/auth';

export default function TopBar({ variant }) {
  const pathname = usePathname();
  const router = useRouter();

  if (variant === 'candidate') {
    return (
      <header className="bar">
        <span className="brand">Learning Tracker</span>
        <span className="lab">Learner view</span>
      </header>
    );
  }

  const onLearners = pathname.startsWith('/coordinator/learners');
  return (
    <header className="bar">
      <span className="brand">Learning Tracker</span>
      <nav className="nav" aria-label="Main">
        <Link
          href="/coordinator"
          className={onLearners ? '' : 'on'}
          aria-current={onLearners ? undefined : 'page'}
        >
          Dashboard
        </Link>
        <Link
          href="/coordinator/learners"
          className={onLearners ? 'on' : ''}
          aria-current={onLearners ? 'page' : undefined}
        >
          Learners
        </Link>
        <button type="button" onClick={() => logout(router)}>
          Log out
        </button>
      </nav>
    </header>
  );
}
