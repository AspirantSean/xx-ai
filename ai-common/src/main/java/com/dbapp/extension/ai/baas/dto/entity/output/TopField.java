package com.dbapp.extension.ai.baas.dto.entity.output;

import lombok.Data;

@Data
public class TopField extends OutputField {
    public TopField(){
        super(TOP);
    }
    public TopField(Integer limit) {
        super(TOP);
        this.limit = limit;
    }
    private Integer limit;
}
