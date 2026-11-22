import { api } from './api';
import { clearUser } from './session';

export async function logout(router) {
  try {
    await api('/auth/logout', { method: 'POST' });
  } catch {
    /* already signed out */
  }
  clearUser();
  router.replace('/login');
}
