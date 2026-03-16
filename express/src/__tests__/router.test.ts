import request from 'supertest';
import express from 'express';
import bodyParser from 'body-parser';

import router from '../router';
import neo4jController from '../controllers/neo4jController';

// Mock neo4jController
jest.mock('../controllers/neo4jController', () => ({
  __esModule: true,
  default: {
    loadBundle: jest.fn((bundle, res) => res.status(200).send({ result: 'loaded' })),
    buildBundle: jest.fn((_id, _filter, res) => res.status(200).send({ result: 'bundle' })),
    loadResource: jest.fn((resource, res) => res.status(200).send({ result: 'resource loaded' })),
    getFhirResource: jest.fn((_id, res) => res.status(200).send({ result: 'resource' })),
    deleteAll: jest.fn((req, res) => res.status(200).send('All nodes deleted'))
  }
}));

const app = express();
app.use(bodyParser.json({ limit: '50mb' }));
app.use('/api/', router);

describe('Router', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('POST /api/Bundle', () => {
    it('should call neo4jController.loadBundle with req.body', async () => {
      const bundle = { resourceType: 'Bundle', type: 'transaction' };
      const res = await request(app)
        .post('/api/Bundle')
        .send(bundle)
        .expect(200);

      expect(neo4jController.loadBundle).toHaveBeenCalled();
    });
  });

  describe('POST /api/Bundle/:_id', () => {
    it('should call neo4jController.buildBundle with _id and filter', async () => {
      const res = await request(app)
        .post('/api/Bundle/test-id-123')
        .send({ filter: "['Patient']" })
        .expect(200);

      expect(neo4jController.buildBundle).toHaveBeenCalledWith(
        'test-id-123',
        "['Patient']",
        expect.anything()
      );
    });

    it('should call buildBundle without filter', async () => {
      const res = await request(app)
        .post('/api/Bundle/test-id-456')
        .send({})
        .expect(200);

      expect(neo4jController.buildBundle).toHaveBeenCalledWith(
        'test-id-456',
        undefined,
        expect.anything()
      );
    });
  });

  describe('POST /api/Resource', () => {
    it('should call neo4jController.loadResource with req.body', async () => {
      const resource = { resourceType: 'Patient', id: 'p-123' };
      const res = await request(app)
        .post('/api/Resource')
        .send(resource)
        .expect(200);

      expect(neo4jController.loadResource).toHaveBeenCalled();
    });
  });

  describe('GET /api/Resource/:_id', () => {
    it('should call neo4jController.getFhirResource with _id', async () => {
      const res = await request(app)
        .get('/api/Resource/patient-123')
        .expect(200);

      expect(neo4jController.getFhirResource).toHaveBeenCalledWith(
        'patient-123',
        expect.anything()
      );
    });
  });

  describe('DELETE /api/delete', () => {
    it('should call neo4jController.deleteAll', async () => {
      const res = await request(app)
        .delete('/api/delete')
        .expect(200);

      expect(neo4jController.deleteAll).toHaveBeenCalled();
    });
  });

  describe('POST /api/compareJSON', () => {
    it('should return isEqual true for identical JSONs', async () => {
      const res = await request(app)
        .post('/api/compareJSON')
        .send({
          jsons: [
            { a: 1, b: 2 },
            { b: 2, a: 1 }
          ]
        })
        .expect(200);

      expect(res.body.isEqual).toBe(true);
    });

    it('should return isEqual false for different JSONs', async () => {
      const res = await request(app)
        .post('/api/compareJSON')
        .send({
          jsons: [
            { a: 1, b: 2 },
            { a: 1, b: 3 }
          ]
        })
        .expect(200);

      expect(res.body.isEqual).toBe(false);
    });

    it('should return isEqual true for differently-ordered but identical JSONs', async () => {
      const res = await request(app)
        .post('/api/compareJSON')
        .send({
          jsons: [
            { z: 26, a: 1, m: 13 },
            { a: 1, m: 13, z: 26 }
          ]
        })
        .expect(200);

      expect(res.body.isEqual).toBe(true);
    });
  });
});
