package com.devfiles.notification.core.invitation.domain.event;

import com.devfiles.notification.core.invitation.infrastructure.adapter.dto.InvitationMessageDto;
import com.devfiles.notification.enterprise.domain.valueobject.Mail;
import lombok.Getter;
import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEvent;

@Getter
public class InvitationMailEvent extends ApplicationEvent {
    private final Mail mail;
    private final Message originalMessage;
    private final InvitationMessageDto invitationMessageDto;
    private final String dlqExchange;
    private final String dlqRoutingKey;

    public InvitationMailEvent(
            Object source, Mail mail, Message originalMessage, InvitationMessageDto invitationMessageDto,
            String dlqExchange, String dlqRoutingKey
    ) {
        super(source);
        this.mail = mail;
        this.originalMessage = originalMessage;
        this.invitationMessageDto = invitationMessageDto;
        this.dlqExchange = dlqExchange;
        this.dlqRoutingKey = dlqRoutingKey;
    }
}