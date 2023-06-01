package com.tool.asset.config;

import com.alibaba.fastjson.JSON;
import com.tool.asset.mapper.AssetInformationMapper;
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
public class DBConfig {

    public static void main(String[] args) throws IOException {
        try (InputStream inputStream = Resources.getResourceAsStream("config/mybatis-config.xml");
             SqlSession sqlSession = new SqlSessionFactoryBuilder().build(inputStream).openSession()) {
            System.out.println(JSON.toJSONString(sqlSession.getMapper(AssetInformationMapper.class).getAssetRatingRules()));
        }
    }

    private static SqlSessionFactory sqlSessionFactory;

    static {
        try (InputStream inputStream = Resources.getResourceAsStream("config/mybatis-config.xml");) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            log.error("初始化db数据库连接池失败", e);
        }
    }

    private static AssetInformationMapper assetInformationMapper;

    public synchronized static AssetInformationMapper getAssetInformationMapper() {
        if (assetInformationMapper == null) {
            assetInformationMapper = sqlSessionFactory.openSession(true).getMapper(AssetInformationMapper.class);
        }
        return assetInformationMapper;
    }


}
