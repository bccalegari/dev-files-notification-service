package com.devfiles.notification.enterprise.application.listener;

import com.devfiles.notification.core.domain.event.InvitationMailEvent;
import com.devfiles.notification.enterprise.domain.event.MailMessageErrorEvent;
import com.devfiles.notification.enterprise.domain.valueobject.Mail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailListener {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JavaMailSender mailSender;

    @EventListener(InvitationMailEvent.class)
    public void onInvitation(InvitationMailEvent invitationMailEvent) {
        log.info("Received invitation mail event: {}", invitationMailEvent);
        sendMail(invitationMailEvent.getMail(), invitationMailEvent.getOriginalMessage());
        log.info("Invitation mail event processed with success");
    }

    private void sendMail(Mail mail, Message originalMessage) {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, "UTF-8");

        log.info("Sending email message to: {} from: {} with subject: {}",
                mail.to(), mail.from(), mail.subject());

        try {
            helper.setFrom(mail.from());
            helper.setTo(mail.to());
            helper.setSubject(mail.subject());
            helper.setText(mail.text().text(), mail.text().isHtml());
            mailSender.send(message);
            log.info("Email message sent with success");
            MDC.clear();
        } catch (Exception e) {
            log.error("Error sending email message to: {} from: {} with subject: {}",
                    mail.to(), mail.from(), mail.subject(), e);
            applicationEventPublisher.publishEvent(new MailMessageErrorEvent(this, originalMessage));
        }
    }
}