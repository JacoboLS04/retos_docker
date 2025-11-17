// Mock fs to avoid import.meta.url issues during testing
jest.mock('fs', () => ({
  readFileSync: jest.fn().mockReturnValue(JSON.stringify({ name: 'test-service', version: '1.0.0' })),
}));

// Mock url module to avoid import.meta errors
jest.mock('url', () => ({
  fileURLToPath: jest.fn((url: string) => '/mocked/path/health.js'),
}));

import { setReady, setAlive, getBootTimeIso, initBootTime } from '../src/health.js';

describe('Health Service', () => {
  let bootTime: Date;

  beforeEach(() => {
    bootTime = new Date('2025-01-01T00:00:00.000Z');
    initBootTime(bootTime);
    setReady(true);
    setAlive(true);
  });

  test('should initialize boot time correctly', () => {
    expect(getBootTimeIso()).toBe('2025-01-01T00:00:00.000Z');
  });

  test('should set ready state', () => {
    setReady(false);
    // We can't directly test internal state, but we can verify no errors
    expect(() => setReady(true)).not.toThrow();
  });

  test('should set alive state', () => {
    setAlive(false);
    expect(() => setAlive(true)).not.toThrow();
  });

  test('getBootTimeIso should return ISO string', () => {
    const isoString = getBootTimeIso();
    expect(isoString).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
  });
});
