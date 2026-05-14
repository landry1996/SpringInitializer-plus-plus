package com.springforge.generator;

import com.springforge.generator.application.ArchitectureConfigValidator;
import com.springforge.generator.domain.BuildTool;
import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.generator.domain.ProjectConfiguration.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArchitectureConfigValidatorTest {

    private final ArchitectureConfigValidator validator = new ArchitectureConfigValidator();

    @Test
    void microservices_validConfig_noErrors() {
        var services = List.of(
                new ServiceDefinition("user-service", "Handles users", 8081,
                        List.of(new DatabaseConfig("POSTGRESQL", "PRIMARY_STORE", true))),
                new ServiceDefinition("order-service", "Handles orders", 8082,
                        List.of(new DatabaseConfig("MONGODB", "PRIMARY_STORE", true),
                                new DatabaseConfig("REDIS", "CACHE", false)))
        );
        var syncComms = List.of(new SyncCommunication("REST", "order-service", "user-service", List.of("/api/users"), true));
        var asyncComms = List.of(new AsyncCommunication("KAFKA", "user-service", "order-service", "user-events", "UserCreatedEvent", "JSON"));
        var msConfig = new MicroservicesConfig(services, syncComms, asyncComms, List.of(),
                new DiscoveryConfig("EUREKA"), null, null, null, List.of(), null);

        var arch = new Architecture("MICROSERVICES", List.of("user-service", "order-service"), false, false,
                msConfig, null, null, null, null, null, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.isEmpty(), "Expected no errors but got: " + errors);
    }

    @Test
    void microservices_duplicateServiceName_error() {
        var services = List.of(
                new ServiceDefinition("user-service", "", 8081, List.of(new DatabaseConfig("POSTGRESQL", "PRIMARY_STORE", true))),
                new ServiceDefinition("user-service", "", 8082, List.of(new DatabaseConfig("POSTGRESQL", "PRIMARY_STORE", true)))
        );
        var msConfig = new MicroservicesConfig(services, List.of(), List.of(), List.of(), null, null, null, null, List.of(), null);
        var arch = new Architecture("MICROSERVICES", List.of(), false, false, msConfig, null, null, null, null, null, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Duplicate service name")));
    }

    @Test
    void microservices_duplicatePort_error() {
        var services = List.of(
                new ServiceDefinition("svc-a", "", 8081, List.of(new DatabaseConfig("POSTGRESQL", "PRIMARY_STORE", true))),
                new ServiceDefinition("svc-b", "", 8081, List.of(new DatabaseConfig("POSTGRESQL", "PRIMARY_STORE", true)))
        );
        var msConfig = new MicroservicesConfig(services, List.of(), List.of(), List.of(), null, null, null, null, List.of(), null);
        var arch = new Architecture("MICROSERVICES", List.of(), false, false, msConfig, null, null, null, null, null, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Duplicate port")));
    }

    @Test
    void microservices_noPrimaryStore_error() {
        var services = List.of(
                new ServiceDefinition("user-service", "", 8081, List.of(new DatabaseConfig("REDIS", "CACHE", false)))
        );
        var msConfig = new MicroservicesConfig(services, List.of(), List.of(), List.of(), null, null, null, null, List.of(), null);
        var arch = new Architecture("MICROSERVICES", List.of(), false, false, msConfig, null, null, null, null, null, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("PRIMARY_STORE")));
    }

    @Test
    void microservices_unknownServiceInComm_error() {
        var services = List.of(
                new ServiceDefinition("user-service", "", 8081, List.of(new DatabaseConfig("POSTGRESQL", "PRIMARY_STORE", true)))
        );
        var syncComms = List.of(new SyncCommunication("REST", "user-service", "nonexistent-service", List.of(), true));
        var msConfig = new MicroservicesConfig(services, syncComms, List.of(), List.of(), null, null, null, null, List.of(), null);
        var arch = new Architecture("MICROSERVICES", List.of(), false, false, msConfig, null, null, null, null, null, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("unknown target")));
    }

    @Test
    void ddd_duplicateBoundedContext_error() {
        var contexts = List.of(
                new BoundedContext("orders", "Order management", "handles orders"),
                new BoundedContext("orders", "Duplicate", "")
        );
        var dddConfig = new DddConfig(contexts, List.of(), List.of(), List.of(), List.of(), List.of());
        var arch = new Architecture("DDD", List.of(), false, false, null, null, null, null, dddConfig, null, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Duplicate bounded context")));
    }

    @Test
    void ddd_invalidRelationType_error() {
        var contexts = List.of(
                new BoundedContext("orders", "", ""),
                new BoundedContext("users", "", "")
        );
        var mapping = List.of(new ContextRelationship("orders", "users", "INVALID_TYPE"));
        var dddConfig = new DddConfig(contexts, mapping, List.of(), List.of(), List.of(), List.of());
        var arch = new Architecture("DDD", List.of(), false, false, null, null, null, null, dddConfig, null, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Invalid context relation type")));
    }

    @Test
    void cqrs_invalidSeparationStrategy_error() {
        var cqrsConfig = new CqrsConfig(List.of(), List.of(),
                new StoreConfig("POSTGRESQL", "CREATE"),
                new StoreConfig("MONGODB", "CREATE"),
                new EventStoreConfig("AXON", "30d"),
                List.of(), "INVALID");
        var arch = new Architecture("CQRS", List.of(), true, false, null, null, null, null, null, cqrsConfig, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Invalid CQRS separation strategy")));
    }

    @Test
    void eventDriven_invalidBrokerType_error() {
        var edConfig = new EventDrivenConfig(List.of(), List.of(), List.of(),
                new BrokerConfig("INVALID_BROKER", null), List.of(),
                new ReliabilityConfig(true, 3, true, "MESSAGE_ID"),
                new SchemaRegistryConfig("NONE", "BACKWARD"),
                new StreamProcessingConfig(false, "KAFKA_STREAMS"));
        var arch = new Architecture("EVENT_DRIVEN", List.of(), false, false, null, null, null, null, null, null, edConfig, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Invalid broker type")));
    }

    @Test
    void modulith_invalidEnforcementLevel_error() {
        var modules = List.of(new ModulithModule("users", "User management", List.of()));
        var archTests = new ArchTestConfig(true, "INVALID", List.of());
        var modConfig = new ModulithConfig(modules, List.of(), List.of(), List.of(), archTests);
        var arch = new Architecture("MODULITH", List.of(), false, false, null, null, null, null, null, null, null, modConfig);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Invalid enforcement level")));
    }

    @Test
    void hexagonal_invalidInboundPort_error() {
        var hexConfig = new HexagonalConfig(List.of("INVALID_PORT"), List.of("DATABASE"), List.of(), List.of(), null);
        var arch = new Architecture("HEXAGONAL", List.of(), false, false, null, null, null, hexConfig, null, null, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Invalid inbound port")));
    }

    @Test
    void monolithic_invalidPackaging_error() {
        var monoConfig = new MonolithicConfig(List.of(), "INVALID", List.of(), null, null, "STATELESS_JWT", null);
        var arch = new Architecture("MONOLITHIC", List.of(), false, false, null, monoConfig, null, null, null, null, null, null);
        var config = new ProjectConfiguration(metadata(), arch, List.of(), null, null, null, null, null, null, null);

        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Invalid packaging")));
    }

    private Metadata metadata() {
        return new Metadata("com.example", "test-app", "Test App", "desc",
                "com.example.testapp", "21", "3.3.5", BuildTool.MAVEN);
    }
}
