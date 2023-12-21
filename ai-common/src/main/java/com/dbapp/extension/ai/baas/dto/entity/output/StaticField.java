package com.dbapp.extension.ai.baas.dto.entity.output;

import lombok.Data;

/**
 * @ClassName StaticField
 * @Description 常量
 * @Author joker.tong
 * @Date 2019/12/4 9:37
 * @Version 1.0
 **/
@Data
public class StaticField<T> extends OutputField {
    public StaticField(){
        super(STATIC);
    }

    public StaticField(T value) {
        super(STATIC);
        this.value = value;
    }
    private T value;
}
