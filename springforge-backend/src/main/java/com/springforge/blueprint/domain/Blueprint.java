package com.springforge.blueprint.domain;

import com.springforge.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "blueprints", schema = "springforge")
public class Blueprint extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArchitectureType type;

    @Column(columnDefinition = "TEXT")
    private String constraintsJson;

    @Column(columnDefinition = "TEXT")
    private String defaultsJson;

    @Column(columnDefinition = "TEXT")
    private String structureJson;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private boolean builtIn = false;

    protected Blueprint() {}

    public Blueprint(String name, String description, ArchitectureType type,
                    String constraintsJson, String defaultsJson, String structureJson, boolean builtIn) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.constraintsJson = constraintsJson;
        this.defaultsJson = defaultsJson;
        this.structureJson = structureJson;
        this.builtIn = builtIn;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ArchitectureType getType() { return type; }
    public String getConstraintsJson() { return constraintsJson; }
    public String getDefaultsJson() { return defaultsJson; }
    public String getStructureJson() { return structureJson; }
    public int getVersion() { return version; }
    public boolean isBuiltIn() { return builtIn; }
}
