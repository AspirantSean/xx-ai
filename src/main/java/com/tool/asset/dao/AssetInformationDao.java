package com.tool.asset.dao;

import com.tool.asset.application.Starter;
import com.tool.asset.config.DBConfig;
import com.tool.asset.entities.AssetRating;
import com.tool.asset.entities.AssetRatingHit;
import com.tool.asset.entities.AssetRatingRule;
import com.tool.asset.entities.WaitForRatingAsset;
import com.tool.asset.mapper.AssetInformationMapper;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * 1、资产主表修改加锁；<br/>2、用于代码层面优化SQL
 *
 * @author ytm
 * @version 2.0
 * @since 2021/11/8 15:04
 */
@Slf4j
public class AssetInformationDao {

    private final AssetInformationMapper assetInformationMapper;

    private AssetInformationDao() {
        assetInformationMapper = DBConfig.getAssetInformationMapper();
    }

    private static AssetInformationDao assetInformationDao;

    public synchronized static AssetInformationDao getInstance() {
        if (assetInformationDao == null) {
            assetInformationDao = new AssetInformationDao();
        }
        return assetInformationDao;
    }

    public List<AssetRatingRule> getAssetRatingRules() {
        return assetInformationMapper.getAssetRatingRules();
    }

    public int insertAssetRating(List<AssetRating> assetRatings) {
        if (Starter.saveResult) {
            return assetInformationMapper.insertAssetRating(assetRatings);
        } else {
            return 0;
        }
    }

    public void changeAssetToHealth(Timestamp createTime, Timestamp startTime) {
        if (Starter.saveResult) {
            assetInformationMapper.changeAssetToHealth(createTime, startTime);
        }
    }

    public void deleteHealthAssets() {
        if (Starter.saveResult) {
            assetInformationMapper.deleteHealthAssets();
        }
    }

    public int deleteHealthAssetsById(List<String> assetIds, Timestamp createTime, Timestamp finishTime) {
        if (Starter.saveResult) {
            return assetInformationMapper.deleteHealthAssetsById(assetIds, createTime, finishTime);
        } else {
            return 0;
        }
    }


    public List<AssetRatingHit> rateAssetByVulnerability(String ratingSql) {
        return assetInformationMapper.rateAssetByVulnerability(ratingSql);
    }

    public List<WaitForRatingAsset> selectAssetIdsWhichHasVulnerability() {
        return assetInformationMapper.selectAssetIdsWhichHasVulnerability();
    }

    public List<WaitForRatingAsset> getRatingAssetByIds(Collection<String> assetIds) {
        return assetInformationMapper.getRatingAssetByIds(assetIds);
    }
}
