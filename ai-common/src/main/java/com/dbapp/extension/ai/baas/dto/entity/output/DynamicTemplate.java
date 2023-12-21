package com.dbapp.extension.ai.baas.dto.entity.output;

import lombok.Data;

/**
 * @ClassName DynamicTemplate
 * @Description 动态模板
 * @Author joker.tong
 * @Date 2020/3/30 19:03
 * @Version 1.0
 **/
@Data
public class DynamicTemplate<T> extends OutputField {
    public DynamicTemplate(){
        super(DYNAMIC_TEMPLATE);
    }
    public DynamicTemplate(T value) {
        super(DYNAMIC_TEMPLATE);
        this.value = value;
    }

    private T value;
}
