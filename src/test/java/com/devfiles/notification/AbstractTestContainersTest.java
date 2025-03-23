package com.devfiles.notification;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractTestContainersTest {
    protected static final GenericContainer<?> RABBITMQ_CONTAINER = new GenericContainer<>(
            "rabbitmq:4.0-rc-management-alpine"
    )
            .withReuse(true)
            .withExposedPorts(5672)
            .withEnv("RABBITMQ_DEFAULT_USER", "notification")
            .withEnv("RABBITMQ_DEFAULT_PASS", "123");

    static {
        RABBITMQ_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBITMQ_CONTAINER::getHost);
        registry.add("spring.rabbitmq.port", () -> RABBITMQ_CONTAINER.getMappedPort(5672).toString());
        registry.add("spring.rabbitmq.username", () -> RABBITMQ_CONTAINER.getEnvMap().get("RABBITMQ_DEFAULT_USER"));
        registry.add("spring.rabbitmq.password", () -> RABBITMQ_CONTAINER.getEnvMap().get("RABBITMQ_DEFAULT_PASS"));
    }
}