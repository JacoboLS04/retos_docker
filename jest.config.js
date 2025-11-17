export default {
  preset: 'ts-jest',
  testEnvironment: '<rootDir>/jest-environment-node-no-dom.cjs',
  testMatch: ['**/__tests__/**/*.test.ts'],
  moduleNameMapper: {
    '^(\\.{1,2}/.*)\\.js$': '$1',
  },
  collectCoverageFrom: [
    'src/**/*.ts',
    '!src/**/*.d.ts',
    '!src/index.ts',
    '!src/prueba.ts',
  ],
  reporters: [
    'default',
    ['jest-junit', {
      outputDirectory: '.',
      outputName: 'junit.xml',
      classNameTemplate: '{classname}',
      titleTemplate: '{title}',
      ancestorSeparator: ' › ',
      usePathForSuiteName: true,
    }],
  ],
  maxWorkers: 1,
  testTimeout: 10000,
  globals: {
    'ts-jest': {
      tsconfig: {
        module: 'commonjs',
        target: 'ES2022',
        types: ['jest', 'node'],
        esModuleInterop: true,
      },
    },
  },
};
