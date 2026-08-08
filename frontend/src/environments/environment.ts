/**
 * Production environment.
 *
 * apiUrl is relative by default so the same-origin reverse proxy can route `/api/v1`.
 * Override at build/deploy time (e.g. replace this file or inject the backend base URL)
 * when the API is served from a different origin — do not hardcode a fictional domain.
 */
export const environment = {
  production: true,
  apiUrl: '/api/v1'
};
