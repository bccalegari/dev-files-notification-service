package com.devfiles.notification.enterprise.domain.valueobject;

public record Mail(
        String from,
        String to,
        String subject,
        MailText text
) {}