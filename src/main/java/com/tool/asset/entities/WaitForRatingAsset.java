package com.tool.asset.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author ytm
 * @version 2.0
 * @since 2022/4/7 17:31
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WaitForRatingAsset {
    public static final int RATE_CK_ALARM = 1;
    public static final int RATE_VULNERABILITY = 2;
    public static final int RATE_ALL = 3;

    private int rateType;
    private String assetId;
    private String assetType;

    public static WaitForRatingAsset create(int rateType, String assetId, String assetType) {
        return new WaitForRatingAsset(rateType, assetId, assetType);
    }

    public WaitForRatingAsset elevateRuleType(WaitForRatingAsset other) {
        return elevateRuleType(other.rateType);
    }

    public WaitForRatingAsset elevateRuleType(int type) {
        this.rateType = this.rateType | type;
        return this;
    }

    public boolean hasRuleType(int type) {
        return (this.rateType & type) != 0;
    }
}
