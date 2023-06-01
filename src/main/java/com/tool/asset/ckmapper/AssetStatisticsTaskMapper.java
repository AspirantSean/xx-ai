package com.tool.asset.ckmapper;

import com.tool.asset.entities.AssetRatingHit;
import com.tool.asset.entities.WaitForRatingAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ytm
 * @version 2.0
 * @since 2021/11/22 14:10
 */
public interface AssetStatisticsTaskMapper {

    /**
     * 通过归并告警表做资产评级
     *
     * @param ratingSql
     * @return
     */
    List<AssetRatingHit> ratingHits(@Param("ratingSql") String ratingSql);

    /**
     * 查询存在告警的资产
     *
     * @return WaitForRatingAsset.class类型实例对象，包含：
     * <li>assetId</li>
     * <li>assetType</li>
     * <li>rateType</li>
     */
    List<WaitForRatingAsset> selectAssetIdsWhichHasAlarms();
}
