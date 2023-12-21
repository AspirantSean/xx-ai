package com.dbapp.extension.ai.baas.dto.entity.model;

import com.dbapp.extension.ai.baas.dto.entity.DetectionParams;
import com.dbapp.extension.ai.baas.dto.entity.ModelMetric;
import com.dbapp.extension.ai.jackson.StringToObjectConverter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @ClassName Model
 * @Description 模型
 * @Author joker.tong
 * @Date 2019/12/3 14:01
 * @Version 1.0
 **/
public class Model<D extends DetectionParams, O extends ModelOutput> extends ModelMetric<D, O> {

    @JsonDeserialize(converter = OutputConverter.class)
    @Override
    public void setOutputParams(O outputParams) {
        this.outputParams = outputParams;
    }

    public static class OutputConverter extends StringToObjectConverter<ModelOutput> {

    }
}
