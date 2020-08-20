package com.dbapp.model.ai.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIModel {

    @ApiParam("自增id")
    private Long id;
    @ApiParam("模型ID")
    private String ruleId;
    @ApiParam("模型名称")
    private String ruleName;
    @ApiParam("模型类型")
    private String modelType;
    @ApiParam("描述")
    private String description;
    @ApiParam("统计指标")
    @JsonDeserialize(converter = DetectionConverter.class)
    private Detection detectionParams;
    @ApiParam("事件")
    private boolean event;
    /********** 无需比较是否修改的字段 **********/
    @ApiParam("自定义")
    private boolean custom;
    @ApiParam("告警")
    private boolean alarm;
    @ApiParam("创建时间")
    private String createTime;
    @ApiParam("修改时间")
    private String modifiedTime;

    public static class DetectionConverter extends StdConverter<Object, Detection> {

        @Override
        public Detection convert(Object o) {
            try {
                if (o instanceof String) {
                    return JSON.parseObject((String) o, Detection.class);
                } else if (o instanceof Detection) {
                    return (Detection) o;
                } else {
                    return JSON.parseObject(JSON.toJSON(o).toString(), Detection.class);
                }
            } catch (Exception e) {
                throw new RuntimeJsonMappingException(e.getMessage());
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Detection {
        @ApiParam("指标ID")
        private String metric;
        @ApiParam("间隔单位")
        private String timeUnit;
        @ApiParam("时间间隔")
        private int window;
        @ApiParam("算法")
        private List<String> algorithm;

        public void setAlgorithm(List<String> algorithm) {
            this.algorithm = new ArrayList<>(algorithm);
            Collections.sort(this.algorithm);
        }

        public List<String> getAlgorithm() {
            return new ArrayList<>(algorithm);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Detection detection = (Detection) o;
            return window == detection.window &&
                    Objects.equals(metric, detection.metric) &&
                    Objects.equals(timeUnit, detection.timeUnit) &&
                    Objects.equals(algorithm, detection.algorithm);
        }

        @Override
        public int hashCode() {
            return Objects.hash(metric, timeUnit, window, algorithm);
        }
    }
}
