package com.tool.asset.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.annotation.Nullable;
import java.sql.Timestamp;

/**
 * 资产评级、评分，一对多
 *
 * @author ytm
 * @version 2.0
 * @Classname AssetRating
 * @Description TODO
 * @since 2021/11/6 11:15
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetRating {

    // 资产评级/评分ID
    private String id;
    /**
     * 资产ID-资产唯一标识
     *
     */
    private String assetId;

    /**
     * 资产名称 发送实现资产系统消息加的，其他业务注意添加
     */
    @Nullable
    private String assetName;
    /**
     * 资产健康状态-fallen:已失陷;high_risk:高风险;medium_risk:中风险;low_risk:低风险;healthy:健康
     * <li>fallen:已失陷</li>
     * <li>high_risk:高风险</li>
     * <li>medium_risk:中风险</li>
     * <li>low_risk:低风险</li>
     * <li>healthy:健康</li>
     *
     * @see AssetHealthyStatus
     */
    private AssetHealthyStatus assetHealthState;
    // 资产评分-评分算法按照健康状态，0~100
    private Double assetScore;
    // 评级时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp ratingTime;
    // 定级标签、资产标签，逗号分隔
    private String tags;
    // 评级分组
    private String rateGroup;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;
    // 是否来自下级平台
    private Boolean fromSubordinatePlatform = Boolean.FALSE;

    private String netId;

    public static class Builder {
        private AssetRating assetRating = new AssetRating();

        public static Builder creator() {
            return new Builder();
        }

        public Builder id(String id) {
            this.assetRating.setId(id);
            return this;
        }

        public Builder assetId(String assetId) {
            this.assetRating.setAssetId(assetId);
            return this;
        }

        public Builder assetHealthState(AssetHealthyStatus assetHealthState) {
            this.assetRating.setAssetHealthState(assetHealthState);
            return this;
        }

        public Builder assetScore(Double assetScore) {
            this.assetRating.setAssetScore(assetScore);
            return this;
        }

        public Builder ratingTime(Timestamp ratingTime) {
            this.assetRating.setRatingTime(ratingTime);
            return this;
        }

        public Builder tags(String tags) {
            this.assetRating.setTags(tags);
            return this;
        }

        public Builder rateGroup(String rateGroup) {
            this.assetRating.setRateGroup(rateGroup);
            return this;
        }

        public Builder createTime(Timestamp createTime) {
            this.assetRating.setCreateTime(createTime);
            return this;
        }

        public AssetRating build() {
            return this.assetRating;
        }
    }
}
