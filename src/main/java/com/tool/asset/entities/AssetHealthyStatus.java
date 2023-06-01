package com.tool.asset.entities;

/**
 * 资产健康状态
 * <li>fallen:已失陷</li>
 * <li>high_risk:高风险</li>
 * <li>medium_risk:中风险</li>
 * <li>low_risk:低风险</li>
 * <li>healthy:健康</li>
 */
public enum AssetHealthyStatus {
    fallen("已失陷", 49, 0),
    high_risk("高风险", 69, 50),
    medium_risk("中风险", 79, 70),
    low_risk("低风险", 89, 80),
    healthy("健康", 100, 90),
    unknown("未知", -1, -1);

    private final String value;
    private final double maxScore;
    private final double minScore;


    AssetHealthyStatus(String value, double maxScore, double minScore) {
        this.value = value;
        this.maxScore = maxScore;
        this.minScore = minScore;
    }

    public String value() {
        return value;
    }

    public double getMaxScore() {
        return this.maxScore;
    }

    public double getMinScore() {
        return this.minScore;
    }
}
