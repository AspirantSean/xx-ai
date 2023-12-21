package com.dbapp.extension.ai.baas.dto.entity.output;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ExpressionField
 * @Description 表达式配置
 * @Author joker.tong
 * @Date 2019/12/4 9:38
 * @Version 1.0
 **/
@Data
public class ExpressionField extends OutputField {
    public ExpressionField(){
        super(EXPRESSION);
    }
    public ExpressionField(List<ExpressionValue> value) {
        super(EXPRESSION);
        this.value = value;
    }
    //表达式
    private List<ExpressionValue> value;
}
