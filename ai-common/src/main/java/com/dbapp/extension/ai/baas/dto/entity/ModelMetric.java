package com.dbapp.extension.ai.baas.dto.entity;

import com.dbapp.extension.ai.baas.dto.entity.metric.Metric;
import com.dbapp.extension.ai.baas.dto.entity.model.ai.ModelAI;
import com.dbapp.extension.ai.jackson.ArrTransConverter;
import com.dbapp.extension.ai.jackson.StringToObjectConverter;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static com.dbapp.extension.ai.baas.dto.entity.ModelConstant.*;

/**
 * @ClassName ModelMetric
 * @Description 模型指标，baas模型api数据类型
 * @Author joker.tong
 * @Date 2019/12/3 9:33
 * @Version 1.0
 **/
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "modelType", visible = true, defaultImpl = ModelMetric.class)
@JsonSubTypes({
        @JsonSubTypes.Type(name = MODEL_AI_3_5, value = ModelAI.class),
        @JsonSubTypes.Type(name = METRIC, value = Metric.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ModelMetric<D extends DetectionParams,O extends OutputParams> {
    protected Long id;

    /**
     * 模型类别<br>
     * AI:AI模型<br>
     * metric:指标<br>
     */
    protected String modelType;
    /**
     * 模型中文名
     */
    protected String ruleName;
    /**
     * 模型英文名，唯一约束
     */
    protected String ruleId;
    /**
     * 描述
     */
    protected String description;
    /**
     * 数据源
     */
    protected String dataSource;
    /**
     * 误报备注
     */
    protected String falsePositives;
    /**
     * 标签
     */
    @JsonDeserialize(converter = TagToStringArrConverter.class)
    protected List<String> tags;
    /**
     * 数据源过滤
     */
    @JsonDeserialize(converter = SourceFilterToStringArrConverter.class)
    protected List<String> sourceFilters;
    /**
     * 检测条件
     */
    @JsonDeserialize(converter = DetectionConverter.class)
    protected D detectionParams;
    /**
     * 输出参数
     */
    @JsonDeserialize(converter = OutputConverter.class)
    protected O outputParams;
    /**
     * 其余可选字段是否输出
     */
    protected boolean fieldOut;
    /**
     * 是否定制
     */
    protected boolean custom;
    /**
     * 是否写入事件
     */
    @EqualsAndHashCode.Exclude
    protected boolean event;

    protected boolean store;

    @EqualsAndHashCode.Exclude
    protected boolean alarm;

    @EqualsAndHashCode.Exclude
    protected boolean defaultEvent;

    @EqualsAndHashCode.Exclude
    protected boolean defaultStore;

    @EqualsAndHashCode.Exclude
    protected boolean defaultAlarm;
    /**
     * 模型指标是否是剧本编排创建
     */
    protected boolean soar;
    /**
     * 是否是引用的模型
     */
    @JsonProperty("isHas")
    protected boolean isHas;
    /**
     * 是否被soar引用
     */
    @EqualsAndHashCode.Exclude
    protected boolean referBySoar;
    /**
     * 模型作用域
     */
    protected String operatorId;

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setSourceFilters(List<String> sourceFilters) {
        this.sourceFilters = sourceFilters;
    }

    public void setDetectionParams(D detectionParams) {
        this.detectionParams = detectionParams;
    }

    public void setOutputParams(O outputParams) {
        this.outputParams = outputParams;
    }

    public static class DetectionConverter extends StringToObjectConverter<DetectionParams> {

    }

    public static class OutputConverter extends StringToObjectConverter<OutputParams> {

    }

    public static class TagToStringArrConverter extends ArrTransConverter<String> {
        public TagToStringArrConverter() {
            super(o -> (String) o.get("value"));
        }
    }

    public static class SourceFilterToStringArrConverter extends ArrTransConverter<String> {
        public SourceFilterToStringArrConverter() {
            super(o -> (String) o.get("sourceFilterKey"));
        }
    }
}
