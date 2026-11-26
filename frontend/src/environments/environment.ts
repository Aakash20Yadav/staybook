// The dev server proxies /api to http://localhost:8080 (see proxy.conf.json),
// so the frontend never needs a hard-coded backend host.
export const environment = {
  apiBaseUrl: '/api',
};
