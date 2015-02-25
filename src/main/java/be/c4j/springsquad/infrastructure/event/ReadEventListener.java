package be.c4j.springsquad.infrastructure.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReadEventListener {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${spring.jms.queue.destination}")
    private String destination;

    @EventListener
    public void onRead(ReadEvent readEvent) {
        jmsTemplate.convertAndSend(destination, readEvent.getGenericEntity());
    }

}
