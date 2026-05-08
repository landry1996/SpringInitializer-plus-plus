package com.springforge.blueprint.application;

import com.springforge.blueprint.domain.Blueprint;

import java.util.UUID;

public record BlueprintResponse(
        UUID id,
        String name,
        String description,
        String type,
        String constraints,
        String defaults,
        String structure,
        int version,
        boolean builtIn
) {
    public static BlueprintResponse from(Blueprint b) {
        return new BlueprintResponse(b.getId(), b.getName(), b.getDescription(),
                b.getType().name(), b.getConstraintsJson(), b.getDefaultsJson(),
                b.getStructureJson(), b.getVersion(), b.isBuiltIn());
    }
}
