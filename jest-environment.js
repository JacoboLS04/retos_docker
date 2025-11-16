const NodeEnvironment = require('jest-environment-node').TestEnvironment;

class CustomEnvironment extends NodeEnvironment {
  async setup() {
    await super.setup();
    // Prevent localStorage initialization error
    if (typeof this.global.localStorage === 'undefined') {
      this.global.localStorage = null;
    }
  }
}

module.exports = CustomEnvironment;
