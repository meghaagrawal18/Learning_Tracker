'use client';

import { useState } from 'react';

// Shows one-time credentials. They live only in memory and are never stored.
export default function CredentialsCard({ title, credentials, onDone }) {
  const [copied, setCopied] = useState(false);

  async function copyPassword() {
    try {
      await navigator.clipboard.writeText(credentials.temporaryPassword);
      setCopied(true);
    } catch {
      setCopied(false);
    }
  }

  return (
    <section className="card success" aria-labelledby="credentials-title">
      <p className="cap">Shown once</p>
      <h2 id="credentials-title">{title}</h2>
      <p className="sub">Share these credentials privately. They will not be shown again.</p>
      <div className="row">
        <span>
          Email<small>{credentials.email}</small>
        </span>
      </div>
      <div className="row">
        <span>
          Temporary password<small className="mono">{credentials.temporaryPassword}</small>
        </span>
        <button type="button" className="btn2" onClick={copyPassword}>
          {copied ? 'Copied' : 'Copy'}
        </button>
      </div>
      <p className="hint">
        Expires {new Date(credentials.expiresAt).toLocaleString()}. The candidate must change it at
        first sign-in.
      </p>
      <div className="actions">
        <button type="button" className="btn" onClick={onDone}>
          Done
        </button>
      </div>
    </section>
  );
}
