package com.springforge.generator.application;

import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.generator.domain.ProjectConfiguration.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ArchitectureConfigValidator {

    public List<String> validate(ProjectConfiguration config) {
        List<String> errors = new ArrayList<>();
        if (config.architecture() == null) return errors;

        String type = config.architecture().type();
        if (type == null) return errors;

        switch (type.toUpperCase()) {
            case "MICROSERVICES" -> validateMicroservices(config.architecture().microservices(), errors);
            case "MONOLITHIC" -> validateMonolithic(config.architecture().monolithic(), errors);
            case "LAYERED" -> validateLayered(config.architecture().layered(), errors);
            case "HEXAGONAL" -> validateHexagonal(config.architecture().hexagonal(), errors);
            case "DDD" -> validateDdd(config.architecture().ddd(), errors);
            case "CQRS" -> validateCqrs(config.architecture().cqrs(), errors);
            case "EVENT_DRIVEN" -> validateEventDriven(config.architecture().eventDriven(), errors);
            case "MODULITH" -> validateModulith(config.architecture().modulith(), errors);
        }

        return errors;
    }

    private void validateMicroservices(MicroservicesConfig ms, List<String> errors) {
        if (ms == null) return;

        if (ms.services() == null || ms.services().isEmpty()) {
            errors.add("At least one microservice must be defined");
            return;
        }

        Set<String> serviceNames = new HashSet<>();
        Set<Integer> ports = new HashSet<>();

        for (ServiceDefinition svc : ms.services()) {
            if (svc.name() == null || svc.name().isBlank()) {
                errors.add("Service name is required");
                continue;
            }
            if (!serviceNames.add(svc.name())) {
                errors.add("Duplicate service name: " + svc.name());
            }
            if (svc.port() > 0 && !ports.add(svc.port())) {
                errors.add("Duplicate port " + svc.port() + " for service: " + svc.name());
            }
            if (svc.databases() != null) {
                boolean hasPrimary = svc.databases().stream()
                        .anyMatch(db -> "PRIMARY_STORE".equals(db.purpose()));
                if (!hasPrimary && !svc.databases().isEmpty()) {
                    errors.add("Service '" + svc.name() + "' must have at least one PRIMARY_STORE database");
                }
                for (DatabaseConfig db : svc.databases()) {
                    if (db.type() == null || db.type().isBlank()) {
                        errors.add("Database type is required for service: " + svc.name());
                    }
                }
            }
        }

        if (ms.syncCommunications() != null) {
            for (SyncCommunication comm : ms.syncCommunications()) {
                if (!serviceNames.contains(comm.source())) {
                    errors.add("Sync communication references unknown source service: " + comm.source());
                }
                if (!serviceNames.contains(comm.target())) {
                    errors.add("Sync communication references unknown target service: " + comm.target());
                }
            }
        }

        if (ms.asyncCommunications() != null) {
            for (AsyncCommunication comm : ms.asyncCommunications()) {
                if (!serviceNames.contains(comm.source())) {
                    errors.add("Async communication references unknown source service: " + comm.source());
                }
                if (!serviceNames.contains(comm.target())) {
                    errors.add("Async communication references unknown target service: " + comm.target());
                }
            }
        }

        if (ms.resilience() != null) {
            for (ResilienceConfig res : ms.resilience()) {
                if (!serviceNames.contains(res.source())) {
                    errors.add("Resilience config references unknown source: " + res.source());
                }
                if (!serviceNames.contains(res.target())) {
                    errors.add("Resilience config references unknown target: " + res.target());
                }
            }
        }
    }

    private void validateMonolithic(MonolithicConfig mono, List<String> errors) {
        if (mono == null) return;

        if (mono.modules() != null) {
            Set<String> names = new HashSet<>();
            for (MonolithModule mod : mono.modules()) {
                if (mod.name() == null || mod.name().isBlank()) {
                    errors.add("Monolithic module name is required");
                    continue;
                }
                if (!names.add(mod.name())) {
                    errors.add("Duplicate monolithic module name: " + mod.name());
                }
                if (mod.dependsOn() != null) {
                    for (String dep : mod.dependsOn()) {
                        if (!names.contains(dep) && !mono.modules().stream().anyMatch(m -> dep.equals(m.name()))) {
                            errors.add("Module '" + mod.name() + "' depends on unknown module: " + dep);
                        }
                    }
                }
            }
        }

        if (mono.packaging() != null && !Set.of("JAR", "WAR").contains(mono.packaging())) {
            errors.add("Invalid packaging type: " + mono.packaging());
        }
    }

    private void validateLayered(LayeredConfig layered, List<String> errors) {
        if (layered == null) return;

        if (layered.dtoStrategy() != null &&
                !Set.of("MAPSTRUCT", "MODELMAPPER", "MANUAL").contains(layered.dtoStrategy())) {
            errors.add("Invalid DTO strategy: " + layered.dtoStrategy());
        }
    }

    private void validateHexagonal(HexagonalConfig hex, List<String> errors) {
        if (hex == null) return;

        Set<String> validInbound = Set.of("REST", "GRPC", "GRAPHQL", "CLI", "MESSAGING");
        Set<String> validOutbound = Set.of("DATABASE", "EXTERNAL_API", "MESSAGING", "FILE_STORAGE", "CACHE");

        if (hex.inboundPorts() != null) {
            for (String port : hex.inboundPorts()) {
                if (!validInbound.contains(port)) {
                    errors.add("Invalid inbound port type: " + port);
                }
            }
        }

        if (hex.outboundPorts() != null) {
            for (String port : hex.outboundPorts()) {
                if (!validOutbound.contains(port)) {
                    errors.add("Invalid outbound port type: " + port);
                }
            }
        }
    }

    private void validateDdd(DddConfig ddd, List<String> errors) {
        if (ddd == null) return;

        Set<String> contextNames = new HashSet<>();
        if (ddd.boundedContexts() != null) {
            for (BoundedContext bc : ddd.boundedContexts()) {
                if (bc.name() == null || bc.name().isBlank()) {
                    errors.add("Bounded context name is required");
                    continue;
                }
                if (!contextNames.add(bc.name())) {
                    errors.add("Duplicate bounded context name: " + bc.name());
                }
            }
        }

        Set<String> validRelations = Set.of(
                "SHARED_KERNEL", "CUSTOMER_SUPPLIER", "ACL", "CONFORMIST", "OPEN_HOST", "PUBLISHED_LANGUAGE");

        if (ddd.contextMapping() != null) {
            for (ContextRelationship rel : ddd.contextMapping()) {
                if (!contextNames.isEmpty() && !contextNames.contains(rel.source())) {
                    errors.add("Context mapping references unknown source: " + rel.source());
                }
                if (!contextNames.isEmpty() && !contextNames.contains(rel.target())) {
                    errors.add("Context mapping references unknown target: " + rel.target());
                }
                if (rel.relationType() != null && !validRelations.contains(rel.relationType())) {
                    errors.add("Invalid context relation type: " + rel.relationType());
                }
            }
        }
    }

    private void validateCqrs(CqrsConfig cqrs, List<String> errors) {
        if (cqrs == null) return;

        if (cqrs.separationStrategy() != null &&
                !Set.of("SAME_DB", "SEPARATE_DB").contains(cqrs.separationStrategy())) {
            errors.add("Invalid CQRS separation strategy: " + cqrs.separationStrategy());
        }

        if (cqrs.eventStore() != null && cqrs.eventStore().type() != null &&
                !Set.of("AXON", "EVENTSTOREDB", "CUSTOM").contains(cqrs.eventStore().type())) {
            errors.add("Invalid event store type: " + cqrs.eventStore().type());
        }
    }

    private void validateEventDriven(EventDrivenConfig ed, List<String> errors) {
        if (ed == null) return;

        if (ed.broker() != null && ed.broker().type() != null &&
                !Set.of("KAFKA", "RABBITMQ").contains(ed.broker().type())) {
            errors.add("Invalid broker type: " + ed.broker().type());
        }

        if (ed.schemaRegistry() != null && ed.schemaRegistry().type() != null &&
                !Set.of("CONFLUENT", "APICURIO", "NONE").contains(ed.schemaRegistry().type())) {
            errors.add("Invalid schema registry type: " + ed.schemaRegistry().type());
        }

        if (ed.events() != null) {
            for (EventDefinition event : ed.events()) {
                if (event.schemaFormat() != null &&
                        !Set.of("AVRO", "JSON", "PROTOBUF").contains(event.schemaFormat())) {
                    errors.add("Invalid event schema format: " + event.schemaFormat());
                }
            }
        }
    }

    private void validateModulith(ModulithConfig mod, List<String> errors) {
        if (mod == null) return;

        Set<String> moduleNames = new HashSet<>();
        if (mod.modules() != null) {
            for (ModulithModule m : mod.modules()) {
                if (m.name() == null || m.name().isBlank()) {
                    errors.add("Modulith module name is required");
                    continue;
                }
                if (!moduleNames.add(m.name())) {
                    errors.add("Duplicate modulith module name: " + m.name());
                }
            }
        }

        if (mod.allowedDependencies() != null) {
            for (ModuleDependency dep : mod.allowedDependencies()) {
                if (!moduleNames.isEmpty() && !moduleNames.contains(dep.from())) {
                    errors.add("Module dependency references unknown source: " + dep.from());
                }
                if (!moduleNames.isEmpty() && !moduleNames.contains(dep.to())) {
                    errors.add("Module dependency references unknown target: " + dep.to());
                }
            }
        }

        if (mod.archTests() != null && mod.archTests().enforcementLevel() != null &&
                !Set.of("WARN", "FAIL").contains(mod.archTests().enforcementLevel())) {
            errors.add("Invalid enforcement level: " + mod.archTests().enforcementLevel());
        }
    }
}
