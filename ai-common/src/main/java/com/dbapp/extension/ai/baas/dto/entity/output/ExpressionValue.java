package com.dbapp.extension.ai.baas.dto.entity.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @ClassName ExpressionValue
 * @Description 表达式详情
 * @Author joker.tong
 * @Date 2019/12/4 9:39
 * @Version 1.0
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ExpressionValue<T> {
    //表达式
    private String expression;
    //输出数据类型，static，wordmapping
    private String valueType;
    //字段值
    private T value;
}
