package com.springforge.preset.domain;

import com.springforge.shared.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "presets")
public class Preset extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "configuration", columnDefinition = "jsonb", nullable = false)
    private String configuration;

    @Column(name = "owner_id", nullable = false)
    private java.util.UUID ownerId;

    @Column(name = "is_shared", nullable = false)
    private boolean shared = false;

    protected Preset() {}

    public Preset(String name, String description, String configuration, java.util.UUID ownerId) {
        this.name = name;
        this.description = description;
        this.configuration = configuration;
        this.ownerId = ownerId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    public java.util.UUID getOwnerId() { return ownerId; }
    public boolean isShared() { return shared; }
    public void setShared(boolean shared) { this.shared = shared; }
}
