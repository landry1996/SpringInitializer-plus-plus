package ${packageName}.${moduleName}.domain;

import java.util.Objects;

public record ${entityName}Id(java.util.UUID value) {

    public ${entityName}Id {
        Objects.requireNonNull(value, "${entityName}Id must not be null");
    }

    public static ${entityName}Id generate() {
        return new ${entityName}Id(java.util.UUID.randomUUID());
    }

    public static ${entityName}Id from(String value) {
        return new ${entityName}Id(java.util.UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
