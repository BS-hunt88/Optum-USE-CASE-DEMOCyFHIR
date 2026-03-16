import cypher from '../controllers/cypherController';

describe('cypherController', () => {
  describe('loadBundle', () => {
    it('should return CALL cyfhir.bundle.load with escaped JSON', () => {
      const bundle = { resourceType: 'Bundle', type: 'transaction' };
      const result = cypher.loadBundle(bundle);
      expect(result).toContain('CALL cyfhir.bundle.load');
      expect(result).toContain('Bundle');
    });

    it('should properly escape double quotes in values', () => {
      const bundle = { key: 'value with "quotes"' };
      const result = cypher.loadBundle(bundle);
      // The JSON is stringified and quotes are escaped with \"
      expect(result).toContain('\\"');
      // Verify the Cypher string contains escaped quotes within the load call
      expect(result).toMatch(/cyfhir\.bundle\.load/);
    });

    it('should handle empty object', () => {
      const result = cypher.loadBundle({});
      expect(result).toContain('CALL cyfhir.bundle.load');
    });
  });

  describe('deleteAll', () => {
    it('should return exact delete cypher string', () => {
      const result = cypher.deleteAll();
      expect(result).toBe('MATCH (n) DETACH DELETE n');
    });
  });

  describe('buildBundleAroundID', () => {
    it('should contain the ID in the Cypher query', () => {
      const result = cypher.buildBundleAroundID('test-id-123');
      expect(result).toContain('test-id-123');
      expect(result).toContain('_resourceId');
      expect(result).toContain('cyfhir.resource.expand');
      expect(result).toContain('cyfhir.bundle.format');
    });

    it('should handle IDs with special characters', () => {
      const result = cypher.buildBundleAroundID('id-with-special_chars.123');
      expect(result).toContain('id-with-special_chars.123');
    });
  });

  describe('buildBundleAroundIDWithFilter', () => {
    it('should embed both ID and filter in Cypher', () => {
      const result = cypher.buildBundleAroundIDWithFilter('test-id', "['Patient', 'Encounter']");
      expect(result).toContain('test-id');
      expect(result).toContain("['Patient', 'Encounter']");
      expect(result).toContain('filter');
    });

    it('should place filter correctly in the query', () => {
      const result = cypher.buildBundleAroundIDWithFilter('my-id', "['Condition']");
      expect(result).toContain('AS filter');
      expect(result).toContain('n.resourceType in filter');
    });
  });

  describe('loadResource', () => {
    it('should return CALL cyfhir.resource.load with escaped JSON', () => {
      const resourceObj = { resourceType: 'Patient', id: 'p-123' };
      const result = cypher.loadResource(resourceObj);
      expect(result).toContain('CALL cyfhir.resource.load');
      expect(result).toContain('Patient');
    });
  });

  describe('getResource', () => {
    it('should contain ID in Cypher match clause', () => {
      const result = cypher.getResource('resource-456');
      expect(result).toContain('resource-456');
      expect(result).toContain('cyfhir.resource.expand');
      expect(result).toContain('cyfhir.resource.format');
    });
  });
});
