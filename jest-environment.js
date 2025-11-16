const { TestEnvironment } = require('jest-environment-node');

class CustomTestEnvironment extends TestEnvironment {
  constructor(config, context) {
    // Patch the config to avoid localStorage issues
    const patchedConfig = {
      ...config,
      projectConfig: {
        ...config.projectConfig,
        testEnvironmentOptions: {
          ...(config.projectConfig?.testEnvironmentOptions || {}),
          url: 'http://localhost',
        },
      },
    };
    
    super(patchedConfig, context);
  }

  async setup() {
    await super.setup();
  }

  async teardown() {
    await super.teardown();
  }

  getVmContext() {
    return super.getVmContext();
  }
}

module.exports = CustomTestEnvironment;
