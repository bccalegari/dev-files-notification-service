package com.devfiles.notification.enterprise.application.listener;

import com.devfiles.notification.core.invitation.domain.event.InvitationMailEvent;
import com.devfiles.notification.enterprise.application.service.MailSender;
import com.devfiles.notification.enterprise.domain.event.MailMessageErrorEvent;
import com.devfiles.notification.enterprise.domain.valueobject.Mail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailListener {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MailSender mailSender;

    @EventListener(InvitationMailEvent.class)
    public void onInvitation(InvitationMailEvent invitationMailEvent) {
        log.info("Received invitation mail event: {}", invitationMailEvent);
        sendMail(invitationMailEvent.getMail(), invitationMailEvent.getOriginalMessage());
        log.info("Invitation mail event processed with success");
    }

    private void sendMail(Mail mail, Message originalMessage) {
        log.info("Sending email message to: {} from: {} with subject: {}",
                mail.to(), mail.from(), mail.subject());

        try {
            mailSender.execute(mail);
            log.info("Email message sent with success");
        } catch (Exception e) {
            log.error("Error sending email message to: {} from: {} with subject: {}",
                    mail.to(), mail.from(), mail.subject(), e);
            applicationEventPublisher.publishEvent(new MailMessageErrorEvent(this, originalMessage));
        }
    }
}