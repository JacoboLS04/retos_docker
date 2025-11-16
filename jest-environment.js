const NodeEnvironment = require('jest-environment-node').TestEnvironment;

class CustomEnvironment extends NodeEnvironment {
  constructor(config, context) {
    super(
      Object.assign({}, config, {
        testEnvironmentOptions: Object.assign({}, config.testEnvironmentOptions, {
          url: 'http://localhost',
        }),
      }),
      context
    );
  }

  async setup() {
    await super.setup();
    // Override localStorage to prevent initialization error
    this.global.localStorage = undefined;
  }
}

module.exports = CustomEnvironment;
