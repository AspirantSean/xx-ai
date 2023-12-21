package com.dbapp.extension.ai.baas.dto.entity.metric;

import com.dbapp.extension.ai.baas.dto.entity.ModelMetric;
import com.dbapp.extension.ai.baas.dto.entity.output.OutputField;
import com.dbapp.extension.ai.jackson.StringToObjectConverter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import static com.dbapp.extension.ai.baas.dto.entity.ModelConstant.METRIC;


/**
 * @ClassName Metric
 * @Description 指标
 * @Author joker.tong
 * @Date 2019/12/3 9:43
 * @Version 1.0
 **/
@JsonDeserialize(converter = Metric.MetricConverter.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class Metric extends ModelMetric<MetricDetection, MetricOutput> {
    public Metric(){
        this.modelType = METRIC;
    }
    //淘汰类型
    private OutputField drop;
    //聚合字段
    @EqualsAndHashCode.Exclude
    private String aliasName;
    //索引
    @EqualsAndHashCode.Exclude
    private String index;
    private String action;
    private String metricHash;
    //统计指标涉及字段，统计模型引用时根据此字段过滤输出字典
    private String[] usedFields;

    @JsonDeserialize(converter = OutputConverter.class)
    @Override
    public void setOutputParams(MetricOutput outputParams) {
        this.outputParams = outputParams;
    }

    @JsonDeserialize(converter = DetectionConverter.class)
    @Override
    public void setDetectionParams(MetricDetection detectionParams) {
        this.detectionParams = detectionParams;
    }

    public static class DetectionConverter extends StringToObjectConverter<MetricDetection> {

    }

    public static class OutputConverter extends StringToObjectConverter<MetricOutput> {

    }

    public static class MetricConverter extends StdConverter<Metric, Metric> {
        @Override
        public Metric convert(Metric metric) {
            metric.setAliasName(getAliasName(metric));
            metric.setIndex(getIndex(metric));
            return metric;
        }

        /**
         * 拼接metric聚合字段名称
         *
         * @param metric
         * @return
         */
        public static String getAliasName(Metric metric) {
            String aliasName = metric.getAliasName();
            if (StringUtils.isNotEmpty(aliasName)) {
                return aliasName;
            }
            String action = metric.getDetectionParams().getAction();
            String field = metric.getDetectionParams().getField();
            String metricName = metric.getRuleId();
            return createAliasName(action, field, metricName);
        }

        /**
         * 拼接metric聚合字段名称
         *
         * @param action
         * @param field
         * @param metricName
         * @return
         */
        public static String createAliasName(String action, String field, String metricName) {
            if (field == null) {
                field = "";
            }
            return String.format("stat_%s_%s_%s", action, field, metricName);
        }

        /**
         * 获取指标索引
         *
         * @param metric
         * @return
         */
        public static String getIndex(Metric metric) {
            return metric.isCustom() ? "ailpha-statistics-alias-custom-*" : "ailpha-statistics-alias-all-*";
        }
    }
}
