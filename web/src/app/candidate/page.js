'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Banner from '@/components/Banner';
import { api } from '@/lib/api';
import { logout } from '@/lib/auth';
import { useUser } from '@/lib/useUser';

const STATUSES = ['Not Started', 'In Progress', 'Completed'];

export default function MyTrainingPage() {
  const router = useRouter();
  const user = useUser();
  const [assignments, setAssignments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [savingId, setSavingId] = useState(null);

  useEffect(() => {
    let cancelled = false;
    api('/me/assignments')
      .then((list) => {
        if (cancelled) return;
        setAssignments(list);
        setError('');
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function changeStatus(assignment, status) {
    setError('');
    setMessage('');
    setSavingId(assignment.id);
    try {
      const updated = await api(`/assignments/${assignment.id}/status`, {
        method: 'PATCH',
        body: { status },
      });
      setAssignments((current) => current.map((a) => (a.id === updated.id ? updated : a)));
      setMessage(`Status saved for ${updated.activity.title}.`);
    } catch (err) {
      setError(err.message);
    } finally {
      setSavingId(null);
    }
  }

  return (
    <>
      <Banner title="My Training" sub={user ? `Signed in as ${user.email}` : ''}>
        <button type="button" className="btn2" onClick={() => logout(router)}>
          Log out
        </button>
      </Banner>

      <section className="card" aria-labelledby="training-title">
        <h2 id="training-title">Your assigned training</h2>
        {loading ? <p className="muted">Loading…</p> : null}
        {!loading && assignments.length === 0 ? (
          <p className="muted">No training assigned yet.</p>
        ) : null}
        {assignments.map((a) => (
          <div className="row" key={a.id}>
            <span>
              {a.activity.title}
              <small>
                {a.activity.topic} · {a.activity.plannedHours} hours
              </small>
            </span>
            <span className="statusbox">
              <label htmlFor={`status-${a.id}`}>Status</label>
              <select
                id={`status-${a.id}`}
                value={a.status}
                disabled={savingId === a.id}
                onChange={(e) => changeStatus(a, e.target.value)}
              >
                {STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </span>
          </div>
        ))}
        {error ? (
          <div className="err" role="alert">
            {error}
          </div>
        ) : null}
        {message ? (
          <div className="ok" role="status">
            {message}
          </div>
        ) : null}
      </section>
    </>
  );
}
