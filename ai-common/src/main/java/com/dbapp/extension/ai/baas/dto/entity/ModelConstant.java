package com.dbapp.extension.ai.baas.dto.entity;

/**
 * @author steven.zhu
 * @version 1.0.0
 * @date 2023/12/20
 */
public class ModelConstant {

    public static final String MODEL_STATISTICS = "Statistics Engine";
    public static final String MODEL_RULE = "Rule Engine";
    public static final String MODEL_CORRELATION = "Correlation Engine";
    public static final String MODEL_IOC = "IntelligenceCollision";
    public static final String MODEL_WAF = "WafRule";
    public static final String MODEL_AI = "AIE: Anomaly";
    public static final String MODEL_OFFLINE = "Offline Engine";
    public static final String MODEL = "model";
    /**
     * 指标
     */
    public static final String METRIC = "metric";
    /**
     * 统计模型
     */
    public static final String MODEL_STATISTICS_3_5 = "statistics";
    /**
     * 规则模型
     */
    public static final String MODEL_RULE_3_5 = "signature";
    /**
     * 关联模型
     */
    public static final String MODEL_CORRELATION_3_5 = "correlation";
    /**
     * 情报模型
     */
    public static final String MODEL_IOC_3_5 = "intelligence";
    /**
     * WAF模型
     */
    public static final String MODEL_WAF_3_5 = "waf";
    /**
     * AI模型
     */
    public static final String MODEL_AI_3_5 = "AI";
    /**
     * 离线模型
     */
    public static final String MODEL_OFFLINE_3_5 = "batch";
    /**
     * SOAR处置联动类型，waf阻断
     */
    public static final String WAF_BLOCKING = "wafBlocking";
    /**
     * SOAR处置联动类型，edr联动
     */
    public static final String EDR_LINKAGE = "edrLinkage";
    /**
     * SOAR处置联动类型，通报
     */
    public static final String NOTIFICATION = "notification";
    /**
     * SOAR处置联动类型，人工查验
     */
    public static final String MANUAL_INSPECTION = "manualInspection";
    /**
     * SOAR处置联动类型，告警
     */
    public static final String WARNING = "warning";
    /**
     * SOAR任务管理
     */
    public static final String SOAR_TASK = "task";
    /**
     * SOAR数据源
     */
    public static final String DATA_SOURCE = "dataSource";
    /**
     * 模型指标
     */
    public static final String MODEL_METRIC = "modelMetric";
}
