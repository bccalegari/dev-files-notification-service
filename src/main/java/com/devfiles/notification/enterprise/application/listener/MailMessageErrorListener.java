package com.devfiles.notification.enterprise.application.listener;

import com.devfiles.notification.enterprise.domain.event.MailMessageErrorEvent;
import com.devfiles.notification.enterprise.domain.valueobject.TraceId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailMessageErrorListener {
    private final RabbitTemplate rabbitTemplate;
    private final Environment environment;

    @EventListener(MailMessageErrorEvent.class)
    public void onMailError(MailMessageErrorEvent mailMessageErrorEvent) {
        log.error("Message event error: {}", mailMessageErrorEvent);

        var originalMessage = mailMessageErrorEvent.getOriginalMessage();
        var exchange = environment.getProperty("message.broker.registration-invite-retry-exchange");
        var routingKey = environment.getProperty("message.broker.registration-invite-retry-key");

        if (exchange == null || routingKey == null) {
            log.error("Error sending message to dead letter exchange: {} with routing key: {}", exchange, routingKey);
            return;
        }

        var message = createMessage(originalMessage);

        log.info("Sending message {} to dead letter exchange: {} with routing key: {}", message, exchange, routingKey);

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Message sent to dead letter exchange: {} with routing key: {}", exchange, routingKey);
        } catch (Exception e) {
            log.error("Error sending message to dead letter exchange: {} with routing key: {}", exchange, routingKey, e);
        }
    }

    private Message createMessage(Message message) {
        var messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        messageProperties.setHeaders(message.getMessageProperties().getHeaders());
        setRetryCount(messageProperties);
        setXTraceId(messageProperties);
        return new Message(message.getBody(), messageProperties);
    }

    private int getRetryCount(MessageProperties messageProperties) {
        return messageProperties.getHeader("x-retry-count") != null ?
                (int) messageProperties.getHeader("x-retry-count") : 0;
    }

    private void setRetryCount(MessageProperties messageProperties) {
        var retryCount = getRetryCount(messageProperties);
        retryCount++;

        messageProperties.setHeader("x-retry-count", retryCount);
    }

    private void setXTraceId(MessageProperties messageProperties) {
        var traceId = new TraceId(messageProperties.getHeader(TraceId.TRACE_ID_BROKER_HEADER));
        traceId.registerMessageBrokerHeader(messageProperties);
    }
}