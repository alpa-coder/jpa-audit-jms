package be.c4j.springsquad.infrastructure;

import be.c4j.springsquad.domain.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PostLoad;

public class Audit {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostLoad
    public void auditUsage(GenericEntity entity) {

        log.debug("You are reading: " + entity);
    }
}