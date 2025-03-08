package com.devfiles.notification.enterprise.domain.valueobject;

public record MailText(
        String text,
        Boolean isHtml
) {}