package com.tool.asset.mapper;

import com.tool.asset.entities.AssetRating;
import com.tool.asset.entities.AssetRatingHit;
import com.tool.asset.entities.AssetRatingRule;
import com.tool.asset.entities.WaitForRatingAsset;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * @author ytm
 * @version 2.0
 * @since 2021/11/8 15:04
 */
public interface AssetInformationMapper {

    /**
     * 查询资产评级规则
     *
     * @return 资产评级规则列表
     */
    List<AssetRatingRule> getAssetRatingRules();

    /**
     * 插入资产评级
     *
     * @return
     */
    int insertAssetRating(@Param("assetRatings") List<AssetRating> assetRatings);

    /**
     * 未评级的资产置为健康
     *
     * @param createTime
     * @param startTime
     * @return
     */
    int changeAssetToHealth(@Param("createTime") Timestamp createTime, @Param("startTime") Timestamp startTime);

    /**
     * 删除健康且100分的资产
     *
     * @return
     */
    int deleteHealthAssets();

    /**
     * 通过资产id删除健康评级资产
     *
     * @return
     */
    int deleteHealthAssetsById(@Param("assetIds") List<String> assetIds, @Param("createTime") Timestamp createTime, @Param("finishTime") Timestamp finishTime);

    /**
     * 通过漏洞表做资产评级
     *
     * @param ratingSql
     * @return
     */
    List<AssetRatingHit> rateAssetByVulnerability(@Param("ratingSql") String ratingSql);

    /**
     * 查询存在漏洞的资产
     *
     * @return
     */
    List<WaitForRatingAsset> selectAssetIdsWhichHasVulnerability();

    /**
     * 根据资产id查询评级资产
     *
     * @return
     */
    List<WaitForRatingAsset> getRatingAssetByIds(@Param("assetIds") Collection<String> assetIds);

}
