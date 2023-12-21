package com.dbapp.extension.ai.baas.dto.entity.output;

import lombok.Data;

/**
 * @ClassName WordMapping
 * @Description 字段映射，el表达式支持
 * @Author joker.tong
 * @Date 2019/12/4 9:38
 * @Version 1.0
 **/
@Data
public class WordMapping<T> extends OutputField {
    public WordMapping(){
        super(WORD_MAPPING);
    }

    public WordMapping(T value) {
        super(WORD_MAPPING);
        this.value = value;
    }
    private T value;
}
