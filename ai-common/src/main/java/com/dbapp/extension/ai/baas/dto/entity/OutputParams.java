package com.dbapp.extension.ai.baas.dto.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName OutputParams
 * @Description 模型指标输出配置
 * @Author joker.tong
 * @Date 2019/12/3 9:40
 * @Version 1.0
 **/
@Data
public class OutputParams {
    //是否写入事件
    protected boolean event;
//    protected boolean store;
    protected boolean alarm;
    protected Map<String, Object> customOut;

    @JsonAnySetter
    public void addCustomOut(String key, Object value) {
        if (customOut == null) {
            customOut = new HashMap<>();
        }
        customOut.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getCustomOut() {
        return customOut;
    }
}
