// Remembers who signed in (role, email, first-login flag) for this browser tab only.
// It holds NO password or token. The real login lives in the server session cookie,
// and the server checks the role on every request.
const KEY = 'tracker.user';
const listeners = new Set();

export function subscribe(callback) {
  listeners.add(callback);
  return () => listeners.delete(callback);
}

export function getRawUser() {
  try {
    return sessionStorage.getItem(KEY);
  } catch {
    return null;
  }
}

export function setUser(user) {
  try {
    sessionStorage.setItem(KEY, JSON.stringify(user));
  } catch {
    /* storage unavailable: the user simply signs in again after a refresh */
  }
  listeners.forEach((callback) => callback());
}

export function clearUser() {
  try {
    sessionStorage.removeItem(KEY);
  } catch {
    /* ignore */
  }
  listeners.forEach((callback) => callback());
}

export function homeFor(role) {
  return role === 'COORDINATOR' ? '/coordinator' : '/candidate';
}
