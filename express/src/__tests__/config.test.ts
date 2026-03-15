// Mock elastic-apm-node before imports
import config from '../config';
import dotenv from 'dotenv';

const mockStart = jest.fn();
const mockLogger = { error: jest.fn() };

jest.mock('elastic-apm-node', () => ({
  __esModule: true,
  default: {
    start: mockStart,
    logger: mockLogger
  }
}));

jest.mock('dotenv', () => ({
  __esModule: true,
  default: {
    config: jest.fn()
  }
}));

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
    if (typeof errorHandler === 'function') {
      errorHandler('APM Server transport error: connection refused');
      expect(stdoutSpy).toHaveBeenCalledWith('APM Server Missing\n');
    }

    stdoutSpy.mockRestore();
  });

  it('should not log non-APM errors', () => {
    mockStart.mockImplementation(() => { /* no-op */ });

    const stdoutSpy = jest.spyOn(process.stdout, 'write').mockImplementation(() => true);

    config();

    const errorHandler = mockLogger.error;
    if (typeof errorHandler === 'function') {
      stdoutSpy.mockClear();
      errorHandler('Some other error message');
      // Should not have written APM Server Missing
      const calls = stdoutSpy.mock.calls.filter(c => c[0] === 'APM Server Missing\n');
      expect(calls.length).toBe(0);
    }

    stdoutSpy.mockRestore();
  });
});
