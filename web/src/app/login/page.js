'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { setUser, homeFor } from '@/lib/session';
import { useUser } from '@/lib/useUser';

export default function LoginPage() {
  const router = useRouter();
  const user = useUser();
  const [role, setRole] = useState('CANDIDATE');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  // Already signed in in this tab: go straight to the right page
  useEffect(() => {
    if (user) router.replace(user.mustChangePassword ? '/change-password' : homeFor(user.role));
  }, [user, router]);

  async function onSubmit(event) {
    event.preventDefault();
    setError('');
    setBusy(true);
    try {
      const result = await api('/auth/login', {
        method: 'POST',
        body: { email: email.trim(), password },
      });

      // The server decides the real role. If it differs from the choice made here,
      // sign out again and show the same generic message as any failed login.
      if (result.role !== role) {
        await api('/auth/logout', { method: 'POST' }).catch(() => {});
        setError('Invalid email or password.');
        return;
      }

      setUser({
        role: result.role,
        mustChangePassword: result.mustChangePassword,
        email: email.trim().toLowerCase(),
      });
      router.replace(result.mustChangePassword ? '/change-password' : homeFor(result.role));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const roleName = role === 'CANDIDATE' ? 'candidate' : 'coordinator';

  return (
    <div className="auth">
      <div className="aside">
        <div className="wm">Learning Tracker</div>
        <h1 className="big">Welcome back</h1>
        <p>Sign in to manage your cohort or keep your training progress up to date.</p>
      </div>
      <div className="formside">
        <form className="card" onSubmit={onSubmit}>
          <h2>Sign in</h2>
          <fieldset className="seg">
            <legend>Sign in as</legend>
            <input
              type="radio"
              id="role-candidate"
              name="role"
              checked={role === 'CANDIDATE'}
              onChange={() => setRole('CANDIDATE')}
            />
            <label htmlFor="role-candidate">Candidate</label>
            <input
              type="radio"
              id="role-coordinator"
              name="role"
              checked={role === 'COORDINATOR'}
              onChange={() => setRole('COORDINATOR')}
            />
            <label htmlFor="role-coordinator">Coordinator</label>
          </fieldset>

          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            autoComplete="username"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          {error ? (
            <div className="err" role="alert">
              {error}
            </div>
          ) : null}

          <button type="submit" className="btn block" disabled={busy}>
            {busy ? 'Signing in…' : `Sign in as ${roleName}`}
          </button>
          <div className="note">
            <strong>No account yet?</strong> Accounts are created by your coordinator. Ask them for
            your email and a temporary password.
          </div>
        </form>
      </div>
    </div>
  );
}
