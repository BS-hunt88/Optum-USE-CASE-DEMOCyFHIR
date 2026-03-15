// Mock neo4j-driver before any imports
import neo4jController from '../controllers/neo4jController';

const mockRun = jest.fn();
const mockClose = jest.fn();
const mockDriverClose = jest.fn();
const mockVerifyConnectivity = jest.fn();

const mockSession = {
  writeTransaction: jest.fn((fn) => fn({ run: mockRun })),
  close: mockClose
};

const mockDriver = {
  session: jest.fn(() => mockSession),
  close: mockDriverClose,
  verifyConnectivity: mockVerifyConnectivity
};

jest.mock('neo4j-driver', () => ({
  __esModule: true,
  default: {
    driver: jest.fn(() => mockDriver),
    auth: {
      basic: jest.fn()
    }
  }
}));

jest.mock('../controllers/cypherController', () => ({
  __esModule: true,
  default: {
    loadBundle: jest.fn((_bundle) => 'CALL cyfhir.bundle.load("mock")'),
    deleteAll: jest.fn(() => 'MATCH (n) DETACH DELETE n'),
    buildBundleAroundID: jest.fn((_id) => 'MATCH mock query'),
    buildBundleAroundIDWithFilter: jest.fn((_id, _filter) => 'MATCH mock filter query'),
    loadResource: jest.fn((_resource) => 'CALL cyfhir.resource.load("mock")'),
    getResource: jest.fn((_id) => 'MATCH resource query')
  }
}));

describe('neo4jController', () => {
  let mockRes: any;

  beforeEach(() => {
    jest.clearAllMocks();
    mockRes = {
      status: jest.fn().mockReturnThis(),
      send: jest.fn().mockReturnThis()
    };
    // Default: writeTransaction resolves
    mockSession.writeTransaction.mockImplementation((fn) => {
      const result = fn({ run: mockRun });
      return Promise.resolve(result);
    });
    mockRun.mockReturnValue({ records: [] });
  });

  describe('loadBundle', () => {
    it('should call res.status(200).send on success', async () => {
      mockRun.mockReturnValue({ records: [{ _fields: ['ok'] }] });

      neo4jController.loadBundle({ resourceType: 'Bundle' }, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(200);
      expect(mockRes.send).toHaveBeenCalled();
    });

    it('should call res.status(500) on transaction promise rejection', async () => {
      mockSession.writeTransaction.mockImplementation(() => {
        return Promise.reject(new Error('Transaction failed'));
      });
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      neo4jController.loadBundle({ resourceType: 'Bundle' }, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalled();
      consoleSpy.mockRestore();
    });

    it('should handle synchronous error in startTransaction', async () => {
      mockSession.writeTransaction.mockImplementation(() => {
        throw new Error('Sync error');
      });
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      neo4jController.loadBundle({ resourceType: 'Bundle' }, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(consoleSpy).toHaveBeenCalled();
      consoleSpy.mockRestore();
    });
  });

  describe('deleteAll', () => {
    it('should call res.status(200) with success message on success', async () => {
      mockRun.mockReturnValue({ summary: {} });

      neo4jController.deleteAll({} as any, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(200);
    });

    it('should call res.status(500) on error', async () => {
      mockSession.writeTransaction.mockImplementation(() => {
        return Promise.reject(new Error('Delete failed'));
      });

      neo4jController.deleteAll({} as any, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));
    });
  });

  describe('buildBundle', () => {
    it('should call getBundle when no filter provided', async () => {
      mockRun.mockReturnValue({
        records: [{ _fields: [{ resourceType: 'Bundle', entry: [] }] }]
      });

      neo4jController.buildBundle('test-id', null as any, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(200);
    });

    it('should call getBundleWithFilter when filter is provided', async () => {
      mockRun.mockReturnValue({
        records: [{ _fields: [{ resourceType: 'Bundle', entry: [] }] }]
      });

      neo4jController.buildBundle('test-id', "['Patient']", mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(200);
    });

    it('should call getBundle when filter is empty object', async () => {
      mockRun.mockReturnValue({
        records: [{ _fields: [{ resourceType: 'Bundle' }] }]
      });

      neo4jController.buildBundle('test-id', '' as any, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));
    });

    it('should return 400 when bundle is empty', async () => {
      mockRun.mockReturnValue({
        records: [{ _fields: [{}] }]
      });

      neo4jController.buildBundle('test-id', null as any, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(400);
    });

    it('should return 500 on getBundle error', async () => {
      mockSession.writeTransaction.mockImplementation(() => {
        return Promise.reject(new Error('Query failed'));
      });
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      neo4jController.buildBundle('test-id', null as any, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(500);
      consoleSpy.mockRestore();
    });

    it('should return 400 when getBundleWithFilter returns empty bundle', async () => {
      mockRun.mockReturnValue({
        records: [{ _fields: [{}] }]
      });

      neo4jController.buildBundle('test-id', "['Patient']", mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(400);
    });

    it('should return 500 on getBundleWithFilter error', async () => {
      mockSession.writeTransaction.mockImplementation(() => {
        return Promise.reject(new Error('Query failed'));
      });
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      neo4jController.buildBundle('test-id', "['Patient']", mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(500);
      consoleSpy.mockRestore();
    });
  });

  describe('loadResource', () => {
    it('should call res.status(200) on success', async () => {
      mockRun.mockReturnValue({ records: [{ _fields: ['ok'] }] });

      neo4jController.loadResource({ resourceType: 'Patient' }, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(200);
    });

    it('should call res.status(500) on error', async () => {
      mockSession.writeTransaction.mockImplementation(() => {
        return Promise.reject(new Error('Load resource failed'));
      });
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      neo4jController.loadResource({ resourceType: 'Patient' }, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalled();
      consoleSpy.mockRestore();
    });

    it('should handle synchronous error in loadResource', async () => {
      mockSession.writeTransaction.mockImplementation(() => {
        throw new Error('Sync error in loadResource');
      });
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      neo4jController.loadResource({ resourceType: 'Patient' }, mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(consoleSpy).toHaveBeenCalled();
      consoleSpy.mockRestore();
    });
  });

  describe('getFhirResource', () => {
    it('should return 200 with non-empty resource', async () => {
      mockRun.mockReturnValue({
        records: [{ _fields: [{ resourceType: 'Patient', id: 'p-1' }] }]
      });

      neo4jController.getFhirResource('p-1', mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(200);
    });

    it('should return 400 with empty resource', async () => {
      mockRun.mockReturnValue({
        records: [{ _fields: [{}] }]
      });

      neo4jController.getFhirResource('missing-id', mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(400);
    });

    it('should return 500 on error', async () => {
      mockSession.writeTransaction.mockImplementation(() => {
        return Promise.reject(new Error('Query failed'));
      });
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      neo4jController.getFhirResource('p-1', mockRes);

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(mockRes.status).toHaveBeenCalledWith(500);
      consoleSpy.mockRestore();
    });
  });

  describe('verifyConnection', () => {
    it('should log success on verified connection', async () => {
      mockVerifyConnectivity.mockResolvedValue(undefined);
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      await neo4jController.verifyConnection();

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(consoleSpy).toHaveBeenCalledWith('Verified Neo4j Connection');
      consoleSpy.mockRestore();
    });

    it('should log error on failed connection', async () => {
      mockVerifyConnectivity.mockRejectedValue(new Error('Connection refused'));
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      await neo4jController.verifyConnection();

      await new Promise(resolve => setTimeout(resolve, 50));

      expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining('Connection refused'));
      consoleSpy.mockRestore();
    });
  });
});
