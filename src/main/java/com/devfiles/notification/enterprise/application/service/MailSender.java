package com.devfiles.notification.enterprise.application.service;

import com.devfiles.notification.enterprise.domain.valueobject.Mail;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSender {
    private final JavaMailSender mailSender;

    public void execute(Mail mail) throws MessagingException {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, "UTF-8");

        helper.setFrom(mail.from());
        helper.setTo(mail.to());
        helper.setSubject(mail.subject());
        helper.setText(mail.text().text(), mail.text().isHtml());
        mailSender.send(message);
    }
}