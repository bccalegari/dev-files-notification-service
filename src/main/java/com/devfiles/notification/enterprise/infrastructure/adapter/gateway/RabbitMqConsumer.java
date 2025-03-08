package com.devfiles.notification.enterprise.infrastructure.adapter.gateway;

import com.devfiles.notification.enterprise.domain.valueobject.TraceId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;

@RequiredArgsConstructor
@Slf4j
public abstract class RabbitMqConsumer {
    protected abstract <T> void processMessage(Message originalMessage, T messageDto);
    protected abstract String getRetryExchange();
    protected abstract String getRetryRoutingKey();
    protected abstract Class<?> getMessageClass();

    protected void consumeMessage(Message message, Channel channel) {
        var traceId =  new TraceId(message.getMessageProperties().getHeader("x-trace-id"));
        traceId.registerMdcLog();

        var retryCount = getRetryCount(message);

        if (retryCount > 0) {
            log.info("Retrying message {} with retry count {}", message, retryCount);
        }

        try {
            if (retryCount >= 3) {
                log.info("Message retry count of {} exceeded", retryCount);
                rejectMessage(message, channel);
                return;
            }

            log.info("Message received: {}", message);
            var messageBody = new String(message.getBody());

            var messageDto = parseMessage(messageBody, getMessageClass());
            processMessage(message, messageDto);

            log.info("Message processed: {}", message);
        } catch (Exception e) {
            handleException(e, message, channel, retryCount);
        }
    }

    protected void sendMessageToRetryExchange(Message message, Channel channel) {
        try {
            var retryExchange = getRetryExchange();
            var retryRoutingKey = getRetryRoutingKey();

            log.info(
                    "Sending message {} to retry exchange: {} with routing key: {}",
                    message, retryExchange, retryRoutingKey
            );

            var messageConverter = new DefaultMessagePropertiesConverter();
            var basicProperties = messageConverter.fromMessageProperties(message.getMessageProperties(), "UTF-8");

            channel.basicPublish(retryExchange, retryRoutingKey, basicProperties, message.getBody());
        } catch (Exception e) {
            log.error("Error sending message to retry exchange", e);
            rejectMessage(message, channel);
        }
    }

    private int getRetryCount(Message message) {
        return message.getMessageProperties().getHeader("x-retry-count") != null ?
                (int) message.getMessageProperties().getHeader("x-retry-count") : 0;
    }

    private <T> T parseMessage(String messageBody, Class<T> messageClass) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        return objectMapper.readValue(messageBody, messageClass);
    }

    private void handleException(Exception e, Message message, Channel channel, int retryCount) {
        log.error(e.getMessage(), e);
        log.error("Error processing message: {}", message);
        try {
            retryCount++;
            message.getMessageProperties().setHeader("x-retry-count", retryCount);
            sendMessageToRetryExchange(message, channel);
        } catch (Exception ex) {
            log.error(e.getMessage(), e);
            log.error("Error sending message to retry exchange: {}", message);
        }
    }

    protected void rejectMessage(Message message, Channel channel) {
        try {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            log.info("Message rejected: {}", message);
        } catch (Exception e) {
            log.error("Error rejecting message: {}", message);
        }
    }
}
