// Preload script to neutralize NodeJS tool injected experimental localStorage
// Ensures Jest environment initialization does not trigger SecurityError
if (typeof global.localStorage === 'undefined') {
  const store = new Map();
  global.localStorage = {
    getItem: (k) => (store.has(k) ? store.get(k) : null),
    setItem: (k, v) => { store.set(k, String(v)); },
    removeItem: (k) => { store.delete(k); },
    clear: () => { store.clear(); },
    key: (i) => Array.from(store.keys())[i] || null,
    get length() { return store.size; }
  };
}
// Clear any NODE_OPTIONS that may inject experimental localstorage flags
if (process.env.NODE_OPTIONS) {
  // Preserve other flags except ones related to localstorage
  const filtered = process.env.NODE_OPTIONS
    .split(/\s+/)
    .filter(f => !f.includes('localstorage') && !f.includes('web-storage'))
    .join(' ');
  process.env.NODE_OPTIONS = filtered;
}
