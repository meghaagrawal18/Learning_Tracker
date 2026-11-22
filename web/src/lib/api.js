import { clearUser } from './session';

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

function readCookie(name) {
  const part = document.cookie.split('; ').find((c) => c.startsWith(`${name}=`));
  return part ? decodeURIComponent(part.slice(name.length + 1)) : '';
}

// Spring Boot sends an XSRF-TOKEN cookie. Every POST/PUT/PATCH must echo it in a header.
async function csrfToken() {
  if (!readCookie('XSRF-TOKEN')) {
    await fetch('/api/v1/auth/csrf', { credentials: 'same-origin', cache: 'no-store' });
  }
  return readCookie('XSRF-TOKEN');
}

export async function api(path, { method = 'GET', body } = {}) {
  const headers = { Accept: 'application/json' };
  if (method !== 'GET') headers['X-XSRF-TOKEN'] = await csrfToken();
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let response;
  try {
    response = await fetch(`/api/v1${path}`, {
      method,
      headers,
      credentials: 'same-origin',
      cache: 'no-store',
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Please try again.');
  }

  if (response.status === 204) return null;

  let data = null;
  try {
    data = await response.json();
  } catch {
    /* no JSON body */
  }

  if (!response.ok) {
    // Not signed in, or the session expired: go back to the sign-in page
    if (response.status === 401 && path !== '/auth/login') {
      clearUser();
      window.location.replace('/login');
    }
    throw new ApiError(response.status, data?.message || 'Something went wrong. Please try again.');
  }
  return data;
}
