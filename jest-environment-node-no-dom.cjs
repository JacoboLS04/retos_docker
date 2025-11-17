const NodeEnvironment = require('jest-environment-node').TestEnvironment;

class CustomNodeEnvironment extends NodeEnvironment {
  constructor(config, context) {
    super(config, context);
    
    // Eliminar completamente localStorage y sessionStorage para evitar errores en CI
    delete this.global.localStorage;
    delete this.global.sessionStorage;
    delete this.global.indexedDB;
  }

  async setup() {
    await super.setup();
  }

  async teardown() {
    await super.teardown();
  }
}

module.exports = CustomNodeEnvironment;
