package com.dbapp.extension.ai.baas.dto.entity.model.ai;

import com.dbapp.extension.ai.baas.dto.entity.DetectionParams;
import com.dbapp.extension.ai.baas.dto.entity.ModelWindow;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName AIDetection
 * @Description AI模型检测条件配置
 * @Author joker.tong
 * @Date 2019/12/4 10:35
 * @Version 1.0
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class AIDetection extends DetectionParams {
    //指标
    private String metric;
    //算法
    private String[] algorithm;
    @JsonUnwrapped
    //窗口
    private ModelWindow window;
}
