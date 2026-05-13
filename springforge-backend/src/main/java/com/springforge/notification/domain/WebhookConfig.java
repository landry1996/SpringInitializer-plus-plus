package com.springforge.notification.domain;

import com.springforge.shared.domain.BaseEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "webhook_configs", schema = "springforge")
public class WebhookConfig extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(name = "secret_token")
    private String secretToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @ElementCollection
    @CollectionTable(name = "webhook_events", schema = "springforge",
            joinColumns = @JoinColumn(name = "webhook_config_id"))
    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private List<NotificationEventType> subscribedEvents;

    @Column(nullable = false)
    private boolean active = true;

    protected WebhookConfig() {}

    public WebhookConfig(UUID organizationId, String name, String url,
                         NotificationChannel channel, List<NotificationEventType> events) {
        this.organizationId = organizationId;
        this.name = name;
        this.url = url;
        this.channel = channel;
        this.subscribedEvents = events;
    }

    public UUID getOrganizationId() { return organizationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getSecretToken() { return secretToken; }
    public void setSecretToken(String secretToken) { this.secretToken = secretToken; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public List<NotificationEventType> getSubscribedEvents() { return subscribedEvents; }
    public void setSubscribedEvents(List<NotificationEventType> events) { this.subscribedEvents = events; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
