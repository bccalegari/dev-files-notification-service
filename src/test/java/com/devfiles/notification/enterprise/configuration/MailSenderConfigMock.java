package com.devfiles.notification.enterprise.configuration;

import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

@Configuration
@Profile("test")
public class MailSenderConfigMock {
    @Bean
    public JavaMailSender javaMailSender() {
        var mailSender = new JavaMailSenderImpl();
        var spyMailSender = spy(mailSender);
        doNothing().when(spyMailSender).send(any(MimeMessage.class));
        return spyMailSender;
    }
}