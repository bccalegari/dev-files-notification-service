package com.devfiles.notification.core.application.publisher;

import com.devfiles.notification.core.domain.event.InvitationMailEvent;
import com.devfiles.notification.core.invitation.infrastructure.adapter.dto.InvitationMessageDto;
import com.devfiles.notification.enterprise.domain.valueobject.Mail;
import com.devfiles.notification.enterprise.domain.valueobject.MailText;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvitationPublisher {
    private final Environment environment;
    private final ApplicationEventPublisher eventPublisher;
    private final TemplateEngine templateEngine;

    @Value("${mail.sender.email}")
    private String senderAddress;

    public void publishMailEvent(Message originalMessage, InvitationMessageDto invitationMessageDto) {
        log.info("Publishing invitation mail event");

        try {
            var mail = buildMail(invitationMessageDto);
            var dlqExchange = environment.getProperty("message.broker-registration-invite-dead-letter-exchange");
            var dlqKey = environment.getProperty("message.broker-registration-invite-dead-letter-key");
            eventPublisher.publishEvent(new InvitationMailEvent(
                    this, mail, originalMessage, invitationMessageDto, dlqExchange, dlqKey)
            );
            log.info("Invitation mail event published successfully");
        } catch (Exception e) {
            log.error("Error publishing invitation mail event", e);
        }
    }

    private Mail buildMail(InvitationMessageDto invitationMessageDto) {
        var context = buildContext(invitationMessageDto);
        var html = templateEngine.process("invitation-mail-template", context);
        var mailText = new MailText(html, true);

        return new Mail(
                senderAddress,
                invitationMessageDto.email(),
                "Account Confirmation",
                mailText
        );
    }

    private Context buildContext(InvitationMessageDto invitationMessageDto) {
        var context = new Context();
        context.setVariable("username", invitationMessageDto.username());
        context.setVariable("activationCode", invitationMessageDto.invitationCode());
        context.setVariable("activationLink", "Not implemented yet");
        return context;
    }
}
