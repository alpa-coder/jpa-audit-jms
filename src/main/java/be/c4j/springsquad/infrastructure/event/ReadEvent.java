package be.c4j.springsquad.infrastructure.event;

public class ReadEvent {
    private Object entity;

    public ReadEvent(Object entity) {
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }
}
