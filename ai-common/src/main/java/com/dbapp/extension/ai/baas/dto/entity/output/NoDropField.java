package com.dbapp.extension.ai.baas.dto.entity.output;

import lombok.Data;

@Data
public class NoDropField extends OutputField {
    public NoDropField() {
        super(NO_DROP);
    }
}
