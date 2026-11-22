'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import Banner from '@/components/Banner';
import CredentialsCard from '@/components/CredentialsCard';
import ProgressBar from '@/components/ProgressBar';
import StatusTag from '@/components/StatusTag';
import { api } from '@/lib/api';

export default function LearnerDetailPage() {
  const { id } = useParams();
  const [learner, setLearner] = useState(null);
  const [assignments, setAssignments] = useState([]);
  const [activities, setActivities] = useState([]);
  const [activityId, setActivityId] = useState('');
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [loadError, setLoadError] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const [credentials, setCredentials] = useState(null);

  const [version, setVersion] = useState(0);
  const refresh = () => setVersion((v) => v + 1);

  useEffect(() => {
    let cancelled = false;
    Promise.all([api('/learners'), api(`/learners/${id}/assignments`), api('/activities')])
      .then(([learners, list, acts]) => {
        if (cancelled) return;
        const found = learners.find((l) => String(l.id) === String(id));
        if (!found) {
          setNotFound(true);
        } else {
          setLearner(found);
          setAssignments(list);
          setActivities(acts);
        }
        setLoadError('');
      })
      .catch((err) => {
        if (cancelled) return;
        if (err.status === 404) setNotFound(true);
        else setLoadError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id, version]);

  async function assign(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    if (!activityId) return setError('Choose an activity.');
    setBusy(true);
    try {
      await api('/assignments', {
        method: 'POST',
        body: { learnerId: Number(id), activityId: Number(activityId) },
      });
      setMessage('Activity assigned.');
      setActivityId('');
      refresh();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function resetPassword() {
    if (!window.confirm(`Reset the password for ${learner.name}? They will be signed out everywhere.`)) {
      return;
    }
    setError('');
    try {
      setCredentials(await api(`/learners/${id}/reset-password`, { method: 'POST' }));
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <p className="muted">Loading…</p>;

  if (notFound) {
    return (
      <>
        <p className="back">
          <Link href="/coordinator/learners">← Back to learners</Link>
        </p>
        <div className="err" role="alert">
          Learner not found.
        </div>
      </>
    );
  }

  if (loadError || !learner) {
    return (
      <div className="err" role="alert">
        {loadError || 'Something went wrong.'}
      </div>
    );
  }

  return (
    <>
      <p className="back">
        <Link href="/coordinator/learners">← Back to learners</Link>
      </p>
      <Banner title={learner.name} sub={`${learner.cohort} · ${learner.email}`}>
        <button type="button" className="btn2" onClick={resetPassword}>
          Reset password
        </button>
      </Banner>

      <div className="cols">
        <section className="card" aria-labelledby="assigned-title">
          <h2 id="assigned-title">Assigned training</h2>
          {assignments.length === 0 ? <p className="muted">No training assigned yet.</p> : null}
          {assignments.map((a) => (
            <div className="rowp" key={a.id}>
              <div className="top">
                <span>{a.activity.title}</span>
                <StatusTag status={a.status} />
              </div>
              <ProgressBar status={a.status} label={`${a.activity.title} progress`} />
              <small>
                {a.activity.topic} · {a.activity.plannedHours} hours
              </small>
            </div>
          ))}
        </section>

        <form className="card" onSubmit={assign} noValidate aria-labelledby="assign-title">
          <h2 id="assign-title">Assign an activity</h2>
          <label htmlFor="activity">Activity</label>
          <select id="activity" value={activityId} onChange={(e) => setActivityId(e.target.value)}>
            <option value="">Choose an activity</option>
            {activities.map((a) => (
              <option key={a.id} value={a.id}>
                {a.title}
              </option>
            ))}
          </select>
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
          <div className="actions">
            <button type="submit" className="btn" disabled={busy}>
              {busy ? 'Assigning…' : 'Assign'}
            </button>
          </div>
          <p className="hint">
            Reset password creates a new one-time temporary password and signs the candidate out
            everywhere.
          </p>
        </form>
      </div>

      {credentials ? (
        <div className="stack">
          <CredentialsCard
            title="New temporary password"
            credentials={credentials}
            onDone={() => setCredentials(null)}
          />
        </div>
      ) : null}
    </>
  );
}
