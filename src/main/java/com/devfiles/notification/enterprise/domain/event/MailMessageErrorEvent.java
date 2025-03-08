package com.devfiles.notification.enterprise.domain.event;

import lombok.Getter;
import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEvent;

@Getter
public class MailMessageErrorEvent extends ApplicationEvent {
    private final Message originalMessage;

    public MailMessageErrorEvent(Object source, Message originalMessage) {
        super(source);
        this.originalMessage = originalMessage;
    }
}