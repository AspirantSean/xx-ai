package com.tool.asset.config;

import com.alibaba.fastjson.JSON;
import com.tool.asset.ckmapper.AssetStatisticsTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ytm
 * @version 2.0
 * @since 2023/5/29 16:48
 */
@Slf4j
public class CKConfig {

    public static void main(String[] args) throws IOException {
        try (InputStream inputStream = Resources.getResourceAsStream("config/mybatis-ck-config.xml");
             SqlSession sqlSession = new SqlSessionFactoryBuilder().build(inputStream).openSession()) {
            AssetStatisticsTaskMapper mapper = sqlSession.getMapper(AssetStatisticsTaskMapper.class);
//            System.out.println(JSON.toJSONString(mapper.selectAssetIdsWhichHasAlarms()));
            System.out.println(mapper.ratingHits("SELECT asset_id   AS assetId,\n" +
                    "               asset_type AS assetType,\n" +
                    "               1          AS rateType\n" +
                    "        FROM t_asset_information limit 10"));

        }
    }

    private static SqlSessionFactory sqlSessionFactory;

    static {
        try (InputStream inputStream = Resources.getResourceAsStream("config/mybatis-ck-config.xml");) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            log.error("初始化ck数据库连接池失败", e);
        }
    }

    private static AssetStatisticsTaskMapper assetStatisticsTaskMapper;

    public synchronized static AssetStatisticsTaskMapper getAssetStatisticsTaskMapper() {
        if (assetStatisticsTaskMapper == null) {
            assetStatisticsTaskMapper = sqlSessionFactory.openSession(true).getMapper(AssetStatisticsTaskMapper.class);
        }
        return assetStatisticsTaskMapper;
    }

}
