package com.dbapp.extension.ai.baas.dto.entity.metric;

import com.dbapp.extension.ai.baas.dto.entity.OutputParams;
import com.dbapp.extension.ai.baas.dto.entity.output.OutputField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName MetricOutput
 * @Description 指标输出配置
 * @Author joker.tong
 * @Date 2019/12/3 9:58
 * @Version 1.0
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper = true)
public class MetricOutput extends OutputParams {
    private boolean store;
    private OutputField name;
}
