package be.c4j.springsquad;

import be.c4j.springsquad.infrastructure.audit.AuditLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import javax.jms.ConnectionFactory;

@Configuration
public class JmsConfig {

    public static final String QUEUE_DESTINATION_NAME = "audit-queue";

    @Bean
    MessageListenerAdapter adapter(AuditLogger auditLogger) {
        MessageListenerAdapter messageListener
                = new MessageListenerAdapter(auditLogger);
        messageListener.setDefaultListenerMethod("log");
        return messageListener;
    }

    @Bean
    SimpleMessageListenerContainer container(MessageListenerAdapter messageListener,
                                             ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setMessageListener(messageListener);
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(QUEUE_DESTINATION_NAME);
        return container;
    }

}
