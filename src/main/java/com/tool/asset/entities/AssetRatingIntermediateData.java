package com.tool.asset.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tool.asset.application.Starter;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author ytm
 * @version 2.0
 * @since 2021/11/19 18:53
 */
public class AssetRatingIntermediateData {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 资产ID
     */
    @Getter
    private final String assetId;
    /**
     * 满足资产评级的条件
     */
    private final List<AssetRatingRule> assetRatingRules = new ArrayList<>();

    public AssetRatingIntermediateData(String assetId) {
        this.assetId = assetId;
    }

    public void addAssetRatingRule(AssetRatingRule assetRatingRule) {
        this.assetRatingRules.add(assetRatingRule);
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 是否能评级
     *
     * @return
     */
    public boolean canGrading() {
        return !assetRatingRules.isEmpty();
    }

    /**
     * 评级结果
     *
     * @return
     */
    public AssetRating grading() {
        assetRatingRules.sort(AssetRatingRule::compareTo);
        List<AssetRatingRule> logRules = new ArrayList<>(assetRatingRules);
        AssetRatingRule gradeRatingContent = assetRatingRules.remove(0);
        LocalDateTime createTime = gradeRatingContent.getRatingTime().toLocalDate().atStartOfDay();// 当天日期
        Double deductPoint = 0.0;
        StringJoiner tags = new StringJoiner(",");
        StringJoiner rateGroup = new StringJoiner(",");
        AssetHealthyStatus rankAssetHealthyStatus = gradeRatingContent.getRank();
        if (AssetHealthyStatus.unknown == rankAssetHealthyStatus
                || AssetHealthyStatus.healthy == rankAssetHealthyStatus) {// 定级条件为健康时，仅评分
            assetRatingRules.add(0, gradeRatingContent);
        } else {// 定级，且定级条件不用于评分
            tags.add(gradeRatingContent.getGradingLabel());
            if (StringUtils.isNotBlank(gradeRatingContent.getOtherTag())) {
                tags.add(gradeRatingContent.getOtherTag());
            }
            if (StringUtils.isNotBlank(gradeRatingContent.getGroup().getGroupName())) {
                rateGroup.add(gradeRatingContent.getGroup().getGroupName());
            }
        }
        for (AssetRatingRule assetRatingRule : assetRatingRules) {// 仅评分时为所有条件，定级时去除定级条件
            deductPoint += assetRatingRule.getScore();
            if (StringUtils.isNotBlank(assetRatingRule.getGradingLabel())) {// 需求确认所有定级标签也要记录
                tags.add(assetRatingRule.getGradingLabel());
            }
            if (StringUtils.isNotBlank(assetRatingRule.getOtherTag())) {
                tags.add(assetRatingRule.getOtherTag());
            }
            if (StringUtils.isNotBlank(assetRatingRule.getGroup().getGroupName())) {
                rateGroup.add(assetRatingRule.getGroup().getGroupName());
            }
        }
        try {
            Starter.logger.info("资产({})满足评级规则：{}", this.assetId, objectMapper.writeValueAsString(logRules));
        } catch (JsonProcessingException e) {
            Starter.logger.error("资产评级规则序列化失败", e);
        }
        return AssetRating.Builder.creator()
                .id(createTime.format(dateFormatter) + this.assetId)
                .assetId(this.assetId)
                .assetHealthState(rankAssetHealthyStatus)
                .assetScore(Math.max(rankAssetHealthyStatus.getMaxScore() - deductPoint, rankAssetHealthyStatus.getMinScore()))
                .ratingTime(Timestamp.valueOf(gradeRatingContent.getRatingTime()))// 评级任务时间
                .tags(tags.toString())
                .rateGroup(rateGroup.toString())
                .createTime(Timestamp.valueOf(createTime))// 当天日期
                .build();
    }

}
