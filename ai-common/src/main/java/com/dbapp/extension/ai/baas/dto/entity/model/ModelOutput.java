package com.dbapp.extension.ai.baas.dto.entity.model;

import com.dbapp.extension.ai.baas.dto.entity.OutputParams;
import com.dbapp.extension.ai.baas.dto.entity.output.OutputField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName ModelOutput
 * @Description 基础输出配置，除AI模型都包含的字段
 * @Author joker.tong
 * @Date 2019/12/6 9:36
 * @Version 1.0
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class ModelOutput extends OutputParams {
    //告警名称
    protected OutputField alarmName;
    //告警类型
    protected OutputField catagory;
    //子告警类型
    protected OutputField subCategory;
    //攻击链
    protected OutputField killChain;
    //告警描述
    protected OutputField alarmDescription;
    //威胁等级
    protected OutputField threatSeverity;
    //告警标签
    protected OutputField alarmTag;
    //处置建议
    protected OutputField suggestion;
    //受害者
    protected OutputField victim;

    protected OutputField attacker;
}
