package org.kumaran.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_notification", indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipientUsername"),
        @Index(name = "idx_notification_recipient_read", columnList = "recipientUsername, read")
})
@Schema(description = "Notification item shown in the user inbox")
public class AppNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Notification identifier", example = "120")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Username who receives this notification", example = "john.doe@leavepal.com")
    private String recipientUsername;

    @Column(nullable = false)
    @Schema(description = "Notification title", example = "New Leave Request")
    private String title;

    @Column(nullable = false, length = 1000)
    @Schema(description = "Notification body text", example = "Alice submitted a Casual leave request (2 days).")
    private String message;

    @Column(nullable = false)
    @Schema(description = "Notification category key", example = "leave-request-submitted")
    private String type;

    @Column(nullable = false)
    @Schema(description = "Read state", example = "false")
    private boolean read = false;

    @Column(nullable = false)
    @Schema(description = "Creation timestamp (ISO-8601)", example = "2026-04-16T09:42:15Z")
    private String createdAt;

    @Schema(description = "Read timestamp (ISO-8601)", example = "2026-04-16T10:15:30Z")
    private String readAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getReadAt() {
        return readAt;
    }

    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }
}
