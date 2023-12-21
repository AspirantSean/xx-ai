package com.dbapp.extension.ai.baas.dto.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @ClassName ModelWindow
 * @Description flink窗口配置
 * @Author joker.tong
 * @Date 2019/12/3 19:30
 * @Version 1.0
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ModelWindow {
    //窗口数值
    private Integer window;
    //窗口类型
    private String windowType;
    //窗口时间单位
    private String timeUnit;
}
