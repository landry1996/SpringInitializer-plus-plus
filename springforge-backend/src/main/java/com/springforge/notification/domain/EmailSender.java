package com.springforge.notification.domain;

public interface EmailSender {
    void send(String toAddress, NotificationEventType eventType, String payload);
}
