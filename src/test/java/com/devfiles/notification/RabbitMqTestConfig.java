package com.devfiles.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRabbit
@Slf4j
@Profile({"test"})
public class RabbitMqTestConfig {
    private final String HOST;
    private final String VIRTUAL_HOST;
    private final int PORT;
    private final String USERNAME;
    private final String PASSWORD;

    private final int MAX_ATTEMPTS;
    private final long INITIAL_INTERVAL_IN_MS;
    private final double MULTIPLIER;
    private final long MAX_INTERVAL_IN_MS;

    public RabbitMqTestConfig(
            @Value("${spring.rabbitmq.host}") String HOST,
            @Value("${spring.rabbitmq.virtual-host}") String VIRTUAL_HOST,
            @Value("${spring.rabbitmq.port}") int PORT,
            @Value("${spring.rabbitmq.username}") String USERNAME,
            @Value("${spring.rabbitmq.password}") String PASSWORD,
            @Value("${spring.rabbitmq.template.retry.max-attempts}") int maxAttempts,
            @Value("${spring.rabbitmq.template.retry.initial-interval}") long initialInterval,
            @Value("${spring.rabbitmq.template.retry.multiplier}") double multiplier,
            @Value("${spring.rabbitmq.template.retry.max-interval}") long maxInterval
    ) {
        this.HOST = HOST;
        this.VIRTUAL_HOST = VIRTUAL_HOST;
        this.PORT = PORT;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.MAX_ATTEMPTS = maxAttempts;
        this.INITIAL_INTERVAL_IN_MS = initialInterval;
        this.MULTIPLIER = multiplier;
        this.MAX_INTERVAL_IN_MS = maxInterval;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        var rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory());
        rabbitTemplate.setRetryTemplate(retryTemplate());
        return rabbitTemplate;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(HOST);
        connectionFactory.setVirtualHost(VIRTUAL_HOST);
        connectionFactory.setPort(PORT);
        connectionFactory.setUsername(USERNAME);
        connectionFactory.setPassword(PASSWORD);
        return connectionFactory;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        var retryTemplate = RetryTemplate.builder()
                .maxAttempts(MAX_ATTEMPTS)
                .exponentialBackoff(INITIAL_INTERVAL_IN_MS, MULTIPLIER, MAX_INTERVAL_IN_MS)
                .build();


        retryTemplate.setListeners(new RetryListener[]{new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(
                    RetryContext context, RetryCallback<T, E> callback
            ) {
                if (context.getRetryCount() == 0) {
                    log.info("Processing message for the first time");
                    return true;
                }

                log.info("Retrying message on attempt: {}", context.getRetryCount());
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(
                    RetryContext context, RetryCallback<T, E> callback, Throwable throwable
            ) {
                if (context.getRetryCount() == 0) {
                    if (throwable != null) {
                        log.error(
                                "Failed to process message for the first time with error: {}",
                                throwable.getMessage()
                        );
                    } else {
                        log.info("Successfully processed message for the first time");
                    }
                    return;
                }

                if (throwable != null) {
                    log.error(
                            "Failed to process message after {} attempts with error: {}",
                            context.getRetryCount(), throwable.getMessage()
                    );
                } else {
                    log.info("Successfully processed message after {} attempts", context.getRetryCount());
                }
            }

            @Override
            public <T, E extends Throwable> void onError(
                    RetryContext context, RetryCallback<T, E> callback, Throwable throwable
            ) {
                if (context.getRetryCount() == 0) {
                    log.error(
                            "Error processing message for the first time with error: {}",
                            throwable.getMessage()
                    );
                    return;
                }

                log.error(
                        "Error processing message on attempt {} with error: {}",
                        context.getRetryCount(), throwable.getMessage()
                );
            }
        }});
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
        return retryTemplate;
    }
}