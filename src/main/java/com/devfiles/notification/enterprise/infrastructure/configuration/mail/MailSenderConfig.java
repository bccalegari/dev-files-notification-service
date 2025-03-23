package com.devfiles.notification.enterprise.infrastructure.configuration.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Objects;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class MailSenderConfig {
    private final Environment environment;

    @Bean
    public JavaMailSender javaMailSender() {
        var mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getProperty("spring.mail.host"));
        mailSender.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.mail.port"))));
        mailSender.setUsername(environment.getProperty("spring.mail.username"));
        mailSender.setPassword(environment.getProperty("spring.mail.password"));

        var props = mailSender.getJavaMailProperties();
        props.put("spring.mail.smtp.auth", environment.getProperty("spring.mail.smtp.auth"));
        props.put("spring.mail.smtp.starttls.enable", environment.getProperty("spring.mail.smtp.starttls.enable"));

        return mailSender;
    }
}