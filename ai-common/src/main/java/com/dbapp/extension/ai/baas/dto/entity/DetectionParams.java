package com.dbapp.extension.ai.baas.dto.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @ClassName DetectionParams
 * @Description 模型指标检测条件
 * @Author joker.tong
 * @Date 2019/12/3 9:40
 * @Version 1.0
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class DetectionParams {
    //条件表达式
    protected String condition;
}
