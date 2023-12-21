package com.dbapp.extension.ai.baas.dto.entity.metric;

import com.dbapp.extension.ai.baas.dto.entity.DetectionParams;
import com.dbapp.extension.ai.baas.dto.entity.ModelWindow;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName MetricDetection
 * @Description 指标检测条件
 * @Author joker.tong
 * @Date 2019/12/3 9:46
 * @Version 1.0
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class MetricDetection extends DetectionParams {
    //算子
    private String action;
    //聚合字段
    private String field;
    //group字段
    private String[] groupBy;
    @JsonUnwrapped
    private ModelWindow window;
}
