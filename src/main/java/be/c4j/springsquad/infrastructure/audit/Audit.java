package be.c4j.springsquad.infrastructure.audit;

import be.c4j.springsquad.infrastructure.ContextHelper;
import be.c4j.springsquad.infrastructure.event.ReadEvent;

import javax.persistence.PostLoad;

public class Audit {

    @PostLoad
    public void auditUsage(Object entity) {
        ContextHelper.getPublisher().publishEvent(new ReadEvent(entity));
    }
}