package com.dbapp.extension.sync.model.ao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformConfig {
    /**
     * 别名，映射后的重命名
     */
    private Map<String, String> rename;
    /**
     * elasticsearch的mapping配置，结构{"properties":{"id":{"type":"keyword}}}
     */
    private Map<String, Object> mapping;
}
