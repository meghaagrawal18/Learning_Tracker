import { useMemo, useSyncExternalStore } from 'react';
import { getRawUser, subscribe } from './session';

// undefined = still checking, null = signed out, object = signed in
export function useUser() {
  const raw = useSyncExternalStore(subscribe, getRawUser, () => undefined);
  return useMemo(() => {
    if (raw === undefined) return undefined;
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }, [raw]);
}
