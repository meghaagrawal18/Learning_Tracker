'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { setUser, homeFor } from '@/lib/session';
import { useUser } from '@/lib/useUser';

export default function ChangePasswordPage() {
  const router = useRouter();
  const user = useUser();
  const [current, setCurrent] = useState('');
  const [next, setNext] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (user === null) router.replace('/login');
  }, [user, router]);

  async function onSubmit(event) {
    event.preventDefault();
    setError('');
    if (next.length < 8) return setError('New password must be at least 8 characters.');
    if (next !== confirm) return setError('The new passwords do not match.');
    if (next === current) return setError('New password must be different from the current password.');

    setBusy(true);
    try {
      await api('/auth/change-password', {
        method: 'POST',
        body: { currentPassword: current, newPassword: next },
      });
      setUser({ ...user, mustChangePassword: false });
      router.replace(homeFor(user.role));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (!user) {
    return (
      <div className="page">
        <p className="muted pad">Loading…</p>
      </div>
    );
  }

  return (
    <div className="auth">
      <div className="aside">
        <div className="wm">Learning Tracker</div>
        <h1 className="big">One quick step</h1>
        <p>
          Replace your temporary password with one only you know. You will go straight to your
          training afterwards.
        </p>
      </div>
      <div className="formside">
        <form className="card" onSubmit={onSubmit} noValidate>
          <h2>Set a new password</h2>
          {user.mustChangePassword ? (
            <div className="note first">
              Your temporary password must be replaced before you can continue.
            </div>
          ) : null}
          <label htmlFor="current">
            {user.mustChangePassword ? 'Temporary password' : 'Current password'}
          </label>
          <input
            id="current"
            type="password"
            autoComplete="current-password"
            value={current}
            onChange={(e) => setCurrent(e.target.value)}
          />
          <label htmlFor="next">New password</label>
          <input
            id="next"
            type="password"
            autoComplete="new-password"
            value={next}
            onChange={(e) => setNext(e.target.value)}
          />
          <label htmlFor="confirm">Confirm new password</label>
          <input
            id="confirm"
            type="password"
            autoComplete="new-password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
          />
          <p className="hint">At least 8 characters and different from the temporary one.</p>

          {error ? (
            <div className="err" role="alert">
              {error}
            </div>
          ) : null}

          <button type="submit" className="btn block" disabled={busy}>
            {busy ? 'Saving…' : 'Save password'}
          </button>
        </form>
      </div>
    </div>
  );
}
