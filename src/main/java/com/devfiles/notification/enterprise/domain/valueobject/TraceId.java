package com.devfiles.notification.enterprise.domain.valueobject;


import lombok.Getter;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessageProperties;

import java.util.UUID;

@Getter
public class TraceId {
    public static final String TRACE_ID_BROKER_HEADER = "x-trace-id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    private final String id;

    public TraceId(String id) {
        if (id == null || id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        } else {
            this.id = id;
        }
    }

    public void registerMdcLog() {
        if (MDC.get(TRACE_ID_MDC_KEY) != null) {
            return;
        }

        MDC.put(TRACE_ID_MDC_KEY, id);
    }

    public void registerMessageBrokerHeader(MessageProperties messageProperties) {
        messageProperties.setHeader(TRACE_ID_BROKER_HEADER, id);
    }
}
