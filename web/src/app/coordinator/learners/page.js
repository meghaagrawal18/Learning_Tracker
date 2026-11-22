'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import Banner from '@/components/Banner';
import CredentialsCard from '@/components/CredentialsCard';
import { api } from '@/lib/api';

export default function LearnersPage() {
  const [learners, setLearners] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [form, setForm] = useState({ name: '', email: '', cohort: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [created, setCreated] = useState(null);

  const [version, setVersion] = useState(0);
  const refresh = () => setVersion((v) => v + 1);

  useEffect(() => {
    let cancelled = false;
    api('/learners')
      .then((list) =>
        Promise.all(
          list.map((l) => api(`/learners/${l.id}/assignments`).then((a) => ({ ...l, assigned: a.length }))),
        ),
      )
      .then((withCounts) => {
        if (cancelled) return;
        setLearners(withCounts);
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

  async function createCandidate(event) {
    event.preventDefault();
    setError('');
    if (!form.name.trim() || !form.email.trim() || !form.cohort.trim()) {
      return setError('Name, email and cohort are required.');
    }
    setBusy(true);
    try {
      const result = await api('/learners', {
        method: 'POST',
        body: { name: form.name.trim(), email: form.email.trim(), cohort: form.cohort.trim() },
      });
      setCreated(result.credentials);
      setForm({ name: '', email: '', cohort: form.cohort });
      refresh();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const cohorts = [...new Set(learners.map((l) => l.cohort))];
  const sub = cohorts.length === 1 ? cohorts[0] : cohorts.length > 1 ? 'All cohorts' : '';

  return (
    <>
      <Banner title="Learners" sub={sub} />

      {loadError ? (
        <div className="err" role="alert">
          {loadError}
        </div>
      ) : null}

      <div className="cols">
        <section className="card" aria-labelledby="learners-title">
          <h2 id="learners-title">All learners</h2>
          {loading ? <p className="muted">Loading…</p> : null}
          {!loading && learners.length === 0 ? <p className="muted">No learners yet.</p> : null}
          {learners.map((l) => (
            <div className="row" key={l.id}>
              <span>
                {l.name}
                <small>{l.email}</small>
              </span>
              <span className="inline">
                {l.assigned} assigned
                <Link
                  className="btn2"
                  href={`/coordinator/learners/${l.id}`}
                  aria-label={`View progress for ${l.name}`}
                >
                  View progress
                </Link>
              </span>
            </div>
          ))}
        </section>

        <form className="card" onSubmit={createCandidate} noValidate aria-labelledby="add-title">
          <h2 id="add-title">Add candidate</h2>
          <label htmlFor="name">Name</label>
          <input
            id="name"
            type="text"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
          <label htmlFor="cohort">Cohort</label>
          <input
            id="cohort"
            type="text"
            value={form.cohort}
            onChange={(e) => setForm({ ...form, cohort: e.target.value })}
          />
          {error ? (
            <div className="err" role="alert">
              {error}
            </div>
          ) : null}
          <div className="actions">
            <button type="submit" className="btn" disabled={busy}>
              {busy ? 'Creating…' : 'Create account'}
            </button>
          </div>
        </form>
      </div>

      {created ? (
        <div className="stack">
          <CredentialsCard
            title="Candidate created"
            credentials={created}
            onDone={() => setCreated(null)}
          />
        </div>
      ) : null}
    </>
  );
}
