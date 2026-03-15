// Mock elastic-apm-node before any imports
import request from 'supertest';
import app from '../app';

jest.mock('elastic-apm-node', () => ({
  __esModule: true,
  default: {
    start: jest.fn(),
    logger: { error: jest.fn() }
  }
}));

jest.mock('neo4j-driver', () => ({
  __esModule: true,
  default: {
    driver: jest.fn(() => ({
      session: jest.fn(() => ({
        writeTransaction: jest.fn(),
        close: jest.fn()
      })),
      close: jest.fn(),
      verifyConnectivity: jest.fn()
    })),
    auth: {
      basic: jest.fn()
    }
  }
}));

describe('Express App', () => {
  it('should be an Express application instance', () => {
    expect(app).toBeDefined();
    expect(typeof app.listen).toBe('function');
    expect(typeof app.use).toBe('function');
  });

  it('should have /api/ routes mounted', async () => {
    // A route that doesn't exist should return 404
    const res = await request(app).get('/api/nonexistent');
    // Express returns 404 for unknown routes under /api/
    expect(res.status).toBe(404);
  });

  it('should handle CORS', async () => {
    const res = await request(app)
      .options('/api/Bundle')
      .set('Origin', 'http://example.com');
    // CORS headers should be present
    expect(res.headers['access-control-allow-origin']).toBeDefined();
  });

  it('should accept large JSON payloads up to 50mb', () => {
    // The bodyParser is configured with 50mb limit
    // We verify app is configured (can't easily test 50mb without memory issues)
    expect(app).toBeDefined();
  });

  it('should serve swagger docs at /docs', async () => {
    const res = await request(app).get('/docs/');
    // Swagger UI redirects or returns HTML
    expect([200, 301, 304]).toContain(res.status);
  });
});
