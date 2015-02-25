package be.c4j.springsquad.config;

import be.c4j.springsquad.infrastructure.audit.AuditLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import javax.jms.ConnectionFactory;

@Configuration
public class JmsConfig {

    @Value("${spring.jms.queue.destination}")
    private String destination;

    @Bean
    public MessageListenerAdapter adapter(AuditLogger auditLogger) {
        MessageListenerAdapter messageListener
                = new MessageListenerAdapter(auditLogger);
        messageListener.setDefaultListenerMethod("log");
        return messageListener;
    }

    @Bean
    public SimpleMessageListenerContainer container(MessageListenerAdapter messageListener,
                                             ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setMessageListener(messageListener);
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(destination);
        return container;
    }

}
