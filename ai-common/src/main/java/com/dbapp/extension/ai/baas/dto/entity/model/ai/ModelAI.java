package com.dbapp.extension.ai.baas.dto.entity.model.ai;

import com.dbapp.extension.ai.baas.dto.entity.model.Model;
import com.dbapp.extension.ai.baas.dto.entity.model.ModelOutput;
import com.dbapp.extension.ai.jackson.StringToObjectConverter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dbapp.extension.ai.baas.dto.entity.ModelConstant.MODEL_AI_3_5;


/**
 * @ClassName AIModel
 * @Description AI模型
 * @Author joker.tong
 * @Date 2019/12/3 14:17
 * @Version 1.0
 **/
public class ModelAI extends Model<AIDetection, ModelOutput> {
    public ModelAI(){
        this.modelType = MODEL_AI_3_5;
    }

    @JsonDeserialize(converter = DetectionConverter.class)
    @Override
    public void setDetectionParams(AIDetection detectionParams) {
        this.detectionParams = detectionParams;
    }

    public static class DetectionConverter extends StringToObjectConverter<AIDetection> {

    }
}
