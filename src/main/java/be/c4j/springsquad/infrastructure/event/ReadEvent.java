package be.c4j.springsquad.infrastructure.event;

import be.c4j.springsquad.domain.GenericEntity;

public class ReadEvent {
    private GenericEntity genericEntity;

    public ReadEvent(GenericEntity genericEntity) {
        this.genericEntity = genericEntity;
    }

    public GenericEntity getGenericEntity() {
        return genericEntity;
    }
}
