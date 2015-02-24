package be.c4j.springsquad.infrastructure.audit;

import be.c4j.springsquad.domain.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void log(GenericEntity entity) {
        log.debug(entity.toString());
    }
}
