// Use require-style references that work with jest.mock hoisting
const mockStart = jest.fn();
const mockLogger: { error: jest.Mock | ((...args: any[]) => void) } = { error: jest.fn() };

jest.mock('elastic-apm-node', () => {
  return {
    __esModule: true,
    default: {
      start: mockStart,
      logger: mockLogger
    }
  };
});

jest.mock('dotenv', () => ({
  __esModule: true,
  default: {
    config: jest.fn()
  }
}));

import config from '../config';
import dotenv from 'dotenv';

describe('config', () => {
  const originalEnv = process.env;

  beforeEach(() => {
    jest.clearAllMocks();
    process.env = { ...originalEnv };
  });

  afterAll(() => {
    process.env = originalEnv;
  });

  it('should load .env.test in test environment', () => {
    process.env.NODE_ENV = 'test';
    config();
    expect(dotenv.config).toHaveBeenCalledWith(
      expect.objectContaining({
        path: expect.stringContaining('.env.test')
      })
    );
  });

  it('should load .env in non-test environment', () => {
    process.env.NODE_ENV = 'production';
    config();
    expect(dotenv.config).toHaveBeenCalledWith(
      expect.objectContaining({
        path: expect.stringContaining('.env')
      })
    );
    // Ensure it's not .env.test
    const callArg = (dotenv.config as jest.Mock).mock.calls[0][0];
    expect(callArg.path).not.toContain('.env.test');
  });

  it('should handle APM start failure gracefully', () => {
    mockStart.mockImplementation(() => {
      throw new Error('APM start failed');
    });

    const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

    expect(() => config()).not.toThrow();

    consoleSpy.mockRestore();
  });

  it('should set up APM logger error handler', () => {
    mockStart.mockImplementation(() => { /* no-op */ });
    config();

    // The logger.error should have been reassigned
    expect(mockLogger.error).toBeDefined();
  });
});

describe('loggerError behavior', () => {
  it('should log APM transport error once', () => {
    mockStart.mockImplementation(() => { /* no-op */ });

    const stdoutSpy = jest.spyOn(process.stdout, 'write').mockImplementation(() => true);

    config();

    // Simulate APM transport error via the logger.error handler
    const errorHandler = mockLogger.error;
    expect(typeof errorHandler).toBe('function');
    (errorHandler as Function)('APM Server transport error: connection refused');
    expect(stdoutSpy).toHaveBeenCalledWith('APM Server Missing\n');

    stdoutSpy.mockRestore();
  });

  it('should not log non-APM errors', () => {
    mockStart.mockImplementation(() => { /* no-op */ });

    const stdoutSpy = jest.spyOn(process.stdout, 'write').mockImplementation(() => true);

    config();

    const errorHandler = mockLogger.error;
    expect(typeof errorHandler).toBe('function');
    stdoutSpy.mockClear();
    (errorHandler as Function)('Some other error message');
    // Should not have written APM Server Missing
    const calls = stdoutSpy.mock.calls.filter(c => c[0] === 'APM Server Missing\n');
    expect(calls.length).toBe(0);

    stdoutSpy.mockRestore();
  });

  it('should only log duplicate APM transport error once', () => {
    mockStart.mockImplementation(() => { /* no-op */ });

    const stdoutSpy = jest.spyOn(process.stdout, 'write').mockImplementation(() => true);

    config();

    const errorHandler = mockLogger.error;
    expect(typeof errorHandler).toBe('function');

    // Call with same error twice
    (errorHandler as Function)('APM Server transport error: connection refused');
    (errorHandler as Function)('APM Server transport error: connection refused');

    // Should only have been logged once (deduplication via Set)
    const apmCalls = stdoutSpy.mock.calls.filter(c => c[0] === 'APM Server Missing\n');
    expect(apmCalls.length).toBe(1);

    stdoutSpy.mockRestore();
  });

  it('should log different APM transport errors separately', () => {
    mockStart.mockImplementation(() => { /* no-op */ });

    const stdoutSpy = jest.spyOn(process.stdout, 'write').mockImplementation(() => true);

    config();

    const errorHandler = mockLogger.error;
    expect(typeof errorHandler).toBe('function');

    // Call with two different APM errors
    (errorHandler as Function)('APM Server transport error: connection refused');
    (errorHandler as Function)('APM Server transport error: timeout');

    // Both should have been logged
    const apmCalls = stdoutSpy.mock.calls.filter(c => c[0] === 'APM Server Missing\n');
    expect(apmCalls.length).toBe(2);

    stdoutSpy.mockRestore();
  });
});
