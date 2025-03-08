package com.devfiles.notification.core.invitation.infrastructure.adapter.dto;

public record InvitationMessageDto(
        String slug, String username, String email, String invitationCode
) {}
