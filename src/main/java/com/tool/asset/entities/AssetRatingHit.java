package com.tool.asset.entities;

import lombok.Data;

/**
 * @author ytm
 * @version 2.0
 * @since 2021/11/22 14:04
 */
@Data
public class AssetRatingHit {
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产满意度
     */
    private boolean satisfied;
}
