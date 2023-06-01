package com.tool.asset.entities;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytm
 * @version 2.0
 * @since 2021/11/19 14:41
 */
@Data
public class AssetRatingGroup {
    /**
     * 分组名称
     */
    private String groupName;
    /**
     * 分组中评级条件列表
     *
     * @see AssetRatingRule
     */
    private List<AssetRatingRule> assetRatingRules;

    public AssetRatingGroup(String groupName, List<AssetRatingRule> assetRatingRules) {
        this.groupName = groupName;
        this.assetRatingRules = new ArrayList<>(assetRatingRules);
        this.assetRatingRules.forEach(assetRatingRule -> assetRatingRule.setGroup(this));
    }

    public AssetRatingGroup sortAssetRatingRules() {
        this.assetRatingRules.sort(AssetRatingRule::compareTo);
        return this;
    }

}
