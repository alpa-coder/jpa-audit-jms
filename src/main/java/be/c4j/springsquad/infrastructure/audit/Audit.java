package be.c4j.springsquad.infrastructure.audit;

import be.c4j.springsquad.JmsConfig;
import be.c4j.springsquad.domain.GenericEntity;
import be.c4j.springsquad.infrastructure.ContextHelper;
import org.springframework.jms.core.JmsTemplate;

import javax.persistence.PostLoad;

public class Audit {

    @PostLoad
    public void auditUsage(GenericEntity entity) {
        ContextHelper.getBean(JmsTemplate.class)
            .convertAndSend(JmsConfig.QUEUE_DESTINATION_NAME, entity);

    }
}