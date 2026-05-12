package com.springforge.marketplace;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blueprint_comments")
public class BlueprintComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String blueprintId;

    private String author;

    @Column(length = 2000)
    private String content;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBlueprintId() { return blueprintId; }
    public void setBlueprintId(String blueprintId) { this.blueprintId = blueprintId; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
