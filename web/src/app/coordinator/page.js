'use client';

import { useEffect, useRef, useState } from 'react';
import Banner from '@/components/Banner';
import { api } from '@/lib/api';

const EMPTY = { id: null, title: '', topic: '', plannedHours: '' };

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [activities, setActivities] = useState([]);
  const [cohortTitle, setCohortTitle] = useState('Progress overview');
  const [form, setForm] = useState(EMPTY);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const titleRef = useRef(null);

  const [version, setVersion] = useState(0);
  const refresh = () => setVersion((v) => v + 1);

  useEffect(() => {
    let cancelled = false;
    Promise.all([api('/cohort-summary'), api('/activities'), api('/learners')])
      .then(([s, a, l]) => {
        if (cancelled) return;
        setSummary(s);
        setActivities(a);
        const cohorts = [...new Set(l.map((x) => x.cohort))];
        setCohortTitle(cohorts.length === 1 ? cohorts[0] : 'All cohorts');
        setLoadError('');
      })
      .catch((err) => {
        if (!cancelled) setLoadError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [version]);

  function startNew() {
    setForm(EMPTY);
    setError('');
    setMessage('');
    titleRef.current?.focus();
  }

  function startEdit(activity) {
    setForm({
      id: activity.id,
      title: activity.title,
      topic: activity.topic,
      plannedHours: String(activity.plannedHours),
    });
    setError('');
    setMessage('');
    titleRef.current?.focus();
  }

  async function save(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    const hours = Number(form.plannedHours);
    if (!form.title.trim() || !form.topic.trim()) {
      return setError('Title and topic are required.');
    }
    if (!Number.isFinite(hours) || hours <= 0) {
      return setError('Planned hours must be greater than zero.');
    }

    setBusy(true);
    try {
      const body = { title: form.title.trim(), topic: form.topic.trim(), plannedHours: hours };
      if (form.id) await api(`/activities/${form.id}`, { method: 'PUT', body });
      else await api('/activities', { method: 'POST', body });
      setMessage('Activity saved.');
      setForm(EMPTY);
      refresh();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <Banner title={cohortTitle} sub="Progress overview">
        <button type="button" className="btn" onClick={startNew}>
          + Add activity
        </button>
      </Banner>

      {loadError ? (
        <div className="err" role="alert">
          {loadError}
        </div>
      ) : null}

      <div className="tiles">
        <div className="tile">
          <div className="num">{summary ? summary.learners : '–'}</div>
          <div className="lab">Learners</div>
        </div>
        <div className="tile">
          <div className="num">{summary ? summary.assignments : '–'}</div>
          <div className="lab">Assignments</div>
        </div>
        <div className="tile">
          <div className="num">{summary ? summary.completed : '–'}</div>
          <div className="lab">Completed</div>
        </div>
      </div>

      <div className="cols">
        <section className="card" aria-labelledby="activities-title">
          <h2 id="activities-title">Learning activities</h2>
          {loading ? <p className="muted">Loading…</p> : null}
          {!loading && activities.length === 0 ? <p className="muted">No activities yet.</p> : null}
          {activities.map((a) => (
            <div className="row" key={a.id}>
              <span>
                {a.title}
                <small>
                  {a.topic} · {a.plannedHours} hours
                </small>
              </span>
              <button
                type="button"
                className="btn2"
                aria-label={`Edit ${a.title}`}
                onClick={() => startEdit(a)}
              >
                Edit
              </button>
            </div>
          ))}
        </section>

        <form className="card" onSubmit={save} noValidate aria-labelledby="form-title">
          <h2 id="form-title">{form.id ? 'Edit activity' : 'Add activity'}</h2>
          <label htmlFor="title">Title</label>
          <input
            id="title"
            ref={titleRef}
            type="text"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
          <label htmlFor="topic">Topic</label>
          <input
            id="topic"
            type="text"
            value={form.topic}
            onChange={(e) => setForm({ ...form, topic: e.target.value })}
          />
          <label htmlFor="hours">Planned hours</label>
          <input
            id="hours"
            type="number"
            min="0"
            step="0.5"
            value={form.plannedHours}
            onChange={(e) => setForm({ ...form, plannedHours: e.target.value })}
          />
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
              {busy ? 'Saving…' : 'Save activity'}
            </button>
            {form.id ? (
              <button type="button" className="btn2" onClick={startNew}>
                Cancel
              </button>
            ) : null}
          </div>
        </form>
      </div>
    </>
  );
}
