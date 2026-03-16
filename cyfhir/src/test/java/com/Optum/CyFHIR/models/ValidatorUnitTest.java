package com.Optum.CyFHIR.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorUnitTest {

    // ---- Constructor tests ----

    @Test
    void testDefaultConstructorDefaultsToR4() throws Exception {
        Validator validator = new Validator();
        assertNotNull(validator);
        assertNotNull(validator.ctx);
        assertNotNull(validator.parser);
        assertNotNull(validator.hapiValidator);
    }

    @Test
    void testConstructorWithR4Uppercase() throws Exception {
        Validator validator = new Validator("R4");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithR4Lowercase() throws Exception {
        Validator validator = new Validator("r4");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithDSTU3Lowercase() throws Exception {
        Validator validator = new Validator("dstu3");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithDSTU3Uppercase() throws Exception {
        Validator validator = new Validator("DSTU3");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithR5() throws Exception {
        Validator validator = new Validator("R5");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithR5Lowercase() throws Exception {
        Validator validator = new Validator("r5");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithInvalidVersionThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            new Validator("invalid_version");
        });
        assertEquals("Unsupported FHIR Version", exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyStringThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            new Validator("");
        });
        assertEquals("Unsupported FHIR Version", exception.getMessage());
    }

    @Test
    void testConstructorWithMixedCaseR4() throws Exception {
        Validator validator = new Validator("R4");
        assertNotNull(validator);
        assertNotNull(validator.ctx);
    }

    // ---- validate dispatch tests ----

    @Test
    void testValidateWithUnknownResourceTypeThrowsException() throws Exception {
        Validator validator = new Validator("R4");
        String json = "{\"resourceType\":\"UnknownType\",\"id\":\"test\"}";
        assertThrows(IllegalStateException.class, () -> validator.validate(json, "UnknownType"));
    }

    @Test
    void testValidateDSTU3UnknownResourceTypeThrows() throws Exception {
        Validator validator = new Validator("DSTU3");
        String json = "{\"resourceType\":\"FakeResource\",\"id\":\"test\"}";
        assertThrows(IllegalStateException.class, () -> validator.validate(json, "FakeResource"));
    }

    @Test
    void testValidateR5UnknownResourceTypeThrows() throws Exception {
        Validator validator = new Validator("R5");
        String json = "{\"resourceType\":\"FakeResource\",\"id\":\"test\"}";
        assertThrows(IllegalStateException.class, () -> validator.validate(json, "FakeResource"));
    }

    // ---- R4 resource type tests (parameterized to cover switch cases) ----

    @ParameterizedTest
    @ValueSource(strings = {
        "Account", "ActivityDefinition", "AdverseEvent", "AllergyIntolerance",
        "Appointment", "AppointmentResponse", "AuditEvent", "Basic", "Binary",
        "BiologicallyDerivedProduct", "BodyStructure",
        "Bundle", "CapabilityStatement", "CarePlan", "CareTeam",
        "ChargeItem", "ChargeItemDefinition",
        "Claim", "ClaimResponse", "ClinicalImpression", "CodeSystem",
        "Communication", "CommunicationRequest", "CompartmentDefinition",
        "Composition", "ConceptMap", "Condition", "Consent", "Contract",
        "Coverage", "CoverageEligibilityRequest", "CoverageEligibilityResponse",
        "DetectedIssue", "Device", "DeviceDefinition", "DeviceMetric",
        "DeviceRequest", "DeviceUseStatement", "DiagnosticReport",
        "DocumentManifest", "DocumentReference",
        "Encounter", "Endpoint", "EnrollmentRequest", "EnrollmentResponse",
        "EpisodeOfCare", "EventDefinition",
        "ExplanationOfBenefit", "FamilyMemberHistory",
        "Flag", "Goal", "GraphDefinition", "Group", "GuidanceResponse",
        "HealthcareService", "ImagingStudy", "Immunization",
        "ImmunizationEvaluation", "ImmunizationRecommendation",
        "ImplementationGuide", "InsurancePlan", "Invoice",
        "Library", "Linkage", "Location",
        "Measure", "MeasureReport", "Media", "Medication",
        "MedicationAdministration", "MedicationDispense",
        "MedicationKnowledge", "MedicationRequest", "MedicationStatement",
        "MessageDefinition", "MessageHeader", "MolecularSequence",
        "NamingSystem", "NutritionOrder", "Observation", "ObservationDefinition",
        "OperationDefinition", "OperationOutcome", "Organization",
        "OrganizationAffiliation", "Parameters", "Patient",
        "PaymentNotice", "PaymentReconciliation", "Person",
        "PlanDefinition", "Practitioner", "PractitionerRole",
        "Procedure", "Provenance", "Questionnaire", "QuestionnaireResponse",
        "RelatedPerson", "RequestGroup",
        "ResearchStudy", "ResearchSubject",
        "RiskAssessment", "Schedule", "SearchParameter",
        "ServiceRequest", "Slot", "Specimen", "SpecimenDefinition",
        "StructureDefinition", "StructureMap", "Subscription",
        "Substance",
        "SupplyDelivery", "SupplyRequest", "Task",
        "TerminologyCapabilities",
        "TestReport", "TestScript", "ValueSet",
        "VerificationResult", "VisionPrescription"
    })
    void testValidateR4ResourceTypes(String resourceType) throws Exception {
        Validator validator = new Validator("R4");
        String json = "{\"resourceType\":\"" + resourceType + "\",\"id\":\"test-" + resourceType.toLowerCase() + "\"}";
        Object result = validator.validate(json, resourceType);
        assertNotNull(result, "R4 validation for " + resourceType + " should return non-null");
    }

    // ---- DSTU3 resource type tests (parameterized to cover switch cases) ----
    // Uses only types from the DSTU3 parseDSTU3() switch in Validator.java

    @ParameterizedTest
    @ValueSource(strings = {
        "Account", "ActivityDefinition", "AdverseEvent", "AllergyIntolerance",
        "Appointment", "AppointmentResponse", "AuditEvent", "Basic", "Binary",
        "BodySite", "Bundle", "CapabilityStatement", "CarePlan", "CareTeam",
        "ChargeItem", "Claim", "ClaimResponse", "ClinicalImpression", "CodeSystem",
        "Communication", "CommunicationRequest", "CompartmentDefinition",
        "Composition", "ConceptMap", "Condition", "Consent", "Contract",
        "Coverage", "DataElement", "DetectedIssue", "Device",
        "DeviceComponent", "DeviceMetric", "DeviceRequest", "DeviceUseStatement",
        "DiagnosticReport", "DocumentManifest", "DocumentReference",
        "EligibilityRequest", "EligibilityResponse",
        "Encounter", "Endpoint", "EnrollmentRequest", "EnrollmentResponse",
        "EpisodeOfCare", "ExpansionProfile", "ExplanationOfBenefit",
        "FamilyMemberHistory", "Flag", "Goal", "GraphDefinition",
        "Group", "GuidanceResponse", "HealthcareService",
        "ImagingManifest", "ImagingStudy", "Immunization",
        "ImmunizationRecommendation", "ImplementationGuide",
        "Library", "Linkage", "Location",
        "Measure", "MeasureReport", "Media", "Medication",
        "MedicationAdministration", "MedicationDispense",
        "MedicationRequest", "MedicationStatement",
        "MessageDefinition", "MessageHeader", "NamingSystem",
        "NutritionOrder", "Observation", "OperationDefinition",
        "OperationOutcome", "Organization", "Parameters", "Patient",
        "PaymentNotice", "PaymentReconciliation", "Person",
        "PlanDefinition", "Practitioner", "PractitionerRole",
        "Procedure", "ProcedureRequest", "ProcessRequest", "ProcessResponse",
        "Provenance", "Questionnaire", "QuestionnaireResponse",
        "ReferralRequest", "RelatedPerson", "RequestGroup",
        "ResearchStudy", "ResearchSubject", "RiskAssessment",
        "Schedule", "SearchParameter", "Sequence",
        "ServiceDefinition", "Slot", "Specimen",
        "StructureDefinition", "StructureMap", "Subscription",
        "Substance", "SupplyDelivery", "SupplyRequest", "Task",
        "TestReport", "TestScript", "ValueSet", "VisionPrescription"
    })
    void testValidateDSTU3ResourceTypes(String resourceType) throws Exception {
        Validator validator = new Validator("DSTU3");
        String json = "{\"resourceType\":\"" + resourceType + "\",\"id\":\"test-" + resourceType.toLowerCase() + "\"}";
        Object result = validator.validate(json, resourceType);
        assertNotNull(result, "DSTU3 validation for " + resourceType + " should return non-null");
    }

    // ---- R5 resource type tests (parameterized to cover switch cases) ----
    // Uses only types from the R5 parseR5() switch in Validator.java

    @ParameterizedTest
    @ValueSource(strings = {
        "Account", "ActivityDefinition", "AdministrableProductDefinition",
        "AdverseEvent", "AllergyIntolerance",
        "Appointment", "AppointmentResponse", "AuditEvent", "Basic", "Binary",
        "BiologicallyDerivedProduct", "BodyStructure",
        "Bundle", "CapabilityStatement", "CapabilityStatement2",
        "CarePlan", "CareTeam",
        "CatalogEntry", "ChargeItem", "ChargeItemDefinition", "Citation",
        "Claim", "ClaimResponse", "ClinicalImpression", "ClinicalUseIssue",
        "CodeSystem", "Communication", "CommunicationRequest",
        "CompartmentDefinition", "Composition", "ConceptMap",
        "Condition", "ConditionDefinition", "Consent", "Contract",
        "Coverage", "CoverageEligibilityRequest", "CoverageEligibilityResponse",
        "DetectedIssue", "Device", "DeviceDefinition", "DeviceMetric",
        "DeviceRequest", "DeviceUsage", "DiagnosticReport",
        "DocumentManifest", "DocumentReference",
        "Encounter", "Endpoint", "EnrollmentRequest", "EnrollmentResponse",
        "EpisodeOfCare", "EventDefinition",
        "EvidenceReport",
        "ExplanationOfBenefit", "FamilyMemberHistory",
        "Flag", "Goal", "GraphDefinition", "Group", "GuidanceResponse",
        "HealthcareService", "ImagingStudy", "Immunization",
        "ImmunizationEvaluation", "ImmunizationRecommendation",
        "ImplementationGuide", "Ingredient", "InsurancePlan", "Invoice",
        "Library", "Linkage", "Location",
        "ManufacturedItemDefinition",
        "Measure", "MeasureReport", "Medication",
        "MedicationAdministration", "MedicationDispense",
        "MedicationKnowledge", "MedicationRequest", "MedicationUsage",
        "MedicinalProductDefinition",
        "MessageDefinition", "MessageHeader", "MolecularSequence",
        "NamingSystem", "NutritionIntake", "NutritionOrder", "NutritionProduct",
        "Observation", "ObservationDefinition",
        "OperationDefinition", "OperationOutcome", "Organization",
        "OrganizationAffiliation", "PackagedProductDefinition",
        "Parameters", "Patient", "PaymentNotice", "PaymentReconciliation",
        "Permission", "Person", "PlanDefinition", "Practitioner", "PractitionerRole",
        "Procedure", "Provenance", "Questionnaire", "QuestionnaireResponse",
        "RegulatedAuthorization", "RelatedPerson", "RequestGroup",
        "ResearchStudy", "ResearchSubject",
        "RiskAssessment", "Schedule", "SearchParameter",
        "ServiceRequest", "Slot", "Specimen", "SpecimenDefinition",
        "StructureDefinition", "StructureMap", "Subscription",
        "SubscriptionStatus", "SubscriptionTopic",
        "Substance", "SubstanceDefinition",
        "SupplyDelivery", "SupplyRequest", "Task",
        "TerminologyCapabilities",
        "TestReport", "TestScript", "ValueSet",
        "VerificationResult", "VisionPrescription"
    })
    void testValidateR5ResourceTypes(String resourceType) throws Exception {
        Validator validator = new Validator("R5");
        String json = "{\"resourceType\":\"" + resourceType + "\",\"id\":\"test-" + resourceType.toLowerCase() + "\"}";
        Object result = validator.validate(json, resourceType);
        assertNotNull(result, "R5 validation for " + resourceType + " should return non-null");
    }

    // ---- ListResource special case tests ----
    // ListResource is a special case: the switch uses "ListResource" but
    // the FHIR JSON resourceType is "List"

    @Test
    void testValidateR4ListResource() throws Exception {
        Validator validator = new Validator("R4");
        String json = "{\"resourceType\":\"List\",\"id\":\"test-list\",\"status\":\"current\",\"mode\":\"working\"}";
        Object result = validator.validate(json, "ListResource");
        assertNotNull(result, "R4 validation for ListResource should return non-null");
    }

    @Test
    void testValidateDSTU3ListResource() throws Exception {
        Validator validator = new Validator("DSTU3");
        String json = "{\"resourceType\":\"List\",\"id\":\"test-list\",\"status\":\"current\",\"mode\":\"working\"}";
        Object result = validator.validate(json, "ListResource");
        assertNotNull(result, "DSTU3 validation for ListResource should return non-null");
    }

    @Test
    void testValidateR5ListResource() throws Exception {
        Validator validator = new Validator("R5");
        String json = "{\"resourceType\":\"List\",\"id\":\"test-list\",\"status\":\"current\",\"mode\":\"working\"}";
        Object result = validator.validate(json, "ListResource");
        assertNotNull(result, "R5 validation for ListResource should return non-null");
    }
}
