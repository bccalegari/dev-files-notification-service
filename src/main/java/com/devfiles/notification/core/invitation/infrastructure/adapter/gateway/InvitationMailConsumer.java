package com.devfiles.notification.core.invitation.infrastructure.adapter.gateway;

import com.devfiles.notification.core.application.publisher.InvitationPublisher;
import com.devfiles.notification.core.invitation.infrastructure.adapter.dto.InvitationMessageDto;
import com.devfiles.notification.enterprise.infrastructure.adapter.gateway.RabbitMqConsumer;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class InvitationMailConsumer extends RabbitMqConsumer {
    private final InvitationPublisher eventPublisher;
    private final Environment environment;

    @Override
    protected <T> void processMessage(Message message, T messageDto) {
        eventPublisher.publishMailEvent(message, (InvitationMessageDto) messageDto);
    }

    @Override
    protected String getRetryExchange() {
        return environment.getProperty("message.broker.registration-invite-retry-exchange");
    }

    @Override
    protected String getRetryRoutingKey() {
        return environment.getProperty("message.broker.registration-invite-retry-key");
    }

    @Override
    protected Class<?> getMessageClass() {
        return InvitationMessageDto.class;
    }

    @Override
    @RabbitListener(queues = "${message.broker.registration-invite-queue}")
    public void consumeMessage(Message message, Channel channel) {
        super.consumeMessage(message, channel);
    }
}
