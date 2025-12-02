package com.avialex.api.model.enums;

public enum ProcessStatus {
    CREATED,
    IN_PROGRESS,
    COMPLETED    
    ;

    public String toPtBr() {
        return switch (this) {
            case CREATED -> "Criado";
            case IN_PROGRESS -> "Em Progresso";
            case COMPLETED -> "Finalizado";
            default -> this.name();
        };
    }
}