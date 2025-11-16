const { TestEnvironment } = require('jest-environment-node');

class MinimalEnvironment extends TestEnvironment {
  async setup() {
    await super.setup();
    // Attempt to override localStorage WITHOUT triggering getter access.
    try {
      const desc = Object.getOwnPropertyDescriptor(global, 'localStorage');
      // Define a benign stub only if descriptor exists (getter) or missing.
      if (!desc || typeof desc.get === 'function') {
        Object.defineProperty(global, 'localStorage', {
          value: {
            getItem: () => null,
            setItem: () => {},
            removeItem: () => {},
            clear: () => {},
            key: () => null,
            length: 0
          },
          configurable: true,
          enumerable: false,
          writable: true
        });
      }
    } catch (e) {
      // Swallow any errors; tests do not depend on localStorage.
    }
  }
}

module.exports = MinimalEnvironment;
