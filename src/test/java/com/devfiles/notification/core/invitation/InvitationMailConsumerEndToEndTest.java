package com.devfiles.notification.core.invitation;

import com.devfiles.notification.AbstractTestContainersTest;
import com.devfiles.notification.core.invitation.infrastructure.adapter.dto.InvitationMessageDto;
import com.devfiles.notification.core.invitation.infrastructure.adapter.gateway.InvitationMailConsumerMock;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class InvitationMailConsumerEndToEndTest extends AbstractTestContainersTest {
    @Autowired
    private InvitationMailConsumerMock invitationMailConsumer;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoSpyBean
    private JavaMailSender javaMailSender;

    @BeforeAll
    static void setUpAll(
            @Autowired ConnectionFactory connectionFactory
    ) {
        var rabbitAdmin = new RabbitAdmin(connectionFactory);

        rabbitAdmin.getQueueInfo("registration-invite-queue");
        var exchangeName = "registration-invite-exchange";
        var exchange = new ExchangeBuilder(exchangeName, "direct").build();
        rabbitAdmin.declareExchange(exchange);

        var queue = new Queue("registration-invite-queue", true);
        rabbitAdmin.declareQueue(queue);

        var routingKey = "registration-invite-key";
        var binding = BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
        rabbitAdmin.declareBinding(binding);
    }

    @Test
    void shouldProcessMessage() {
        var objectMapper = new ObjectMapper();
        var messageBody = new InvitationMessageDto(
                "slug", "schrute", "email", "code"
        );

        String message = null;

        try {
            message = objectMapper.writeValueAsString(messageBody);
        } catch (JsonProcessingException e) {
            fail("Error parsing message", e);
        }
        rabbitTemplate.convertAndSend(
                "registration-invite-exchange", "registration-invite-key", message
        );

        invitationMailConsumer.consumeMessage(rabbitTemplate.receive("registration-invite-queue"),
                null);

        var captor = ArgumentCaptor.forClass(MimeMessage.class);

        verify(javaMailSender, times(1)).send(captor.capture());

        var mimeMessage = captor.getValue();

        try {
            assertEquals("mock@mock.com", mimeMessage.getFrom()[0].toString());
            assertEquals("email", mimeMessage.getAllRecipients()[0].toString());
            assertEquals("Account Confirmation", mimeMessage.getSubject());
            assertThat(mimeMessage.getContent().toString()).contains("schrute");
            assertThat(mimeMessage.getContent().toString()).contains("code");
        } catch (Exception e) {
            fail("Error getting content", e);
        }
    }

}
