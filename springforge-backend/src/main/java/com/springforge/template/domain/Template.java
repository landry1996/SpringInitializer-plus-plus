package com.springforge.template.domain;

import com.springforge.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "templates", schema = "springforge")
public class Template extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String path;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateScope scope;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int version = 1;

    private String blueprintType;

    protected Template() {}

    public Template(String name, String path, String category, TemplateScope scope,
                    String content, String blueprintType) {
        this.name = name;
        this.path = path;
        this.category = category;
        this.scope = scope;
        this.content = content;
        this.blueprintType = blueprintType;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public String getCategory() { return category; }
    public TemplateScope getScope() { return scope; }
    public String getContent() { return content; }
    public int getVersion() { return version; }
    public String getBlueprintType() { return blueprintType; }

    public void updateContent(String content) {
        this.content = content;
        this.version++;
    }
}
