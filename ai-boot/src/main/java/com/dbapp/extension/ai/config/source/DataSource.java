package com.dbapp.extension.ai.config.source;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Configuration
@Slf4j
@MapperScan(basePackages = {"com.dbapp.**.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory")
public class DataSource {

    /**
     * 数据库连接
     */
    @Value("${ailpha.datasource.jdbc-url}")
    private String url;
    @Value("${ailpha.datasource.username}")
    private String userName;
    @Value("${ailpha.datasource.password}")
    private String passWord;
    @Value("${ailpha.datasource.driver-class-name}")
    private String driverClass;
    @Value("${ailpha.datasource.type}")
    private String dataSourceType;

    /**
     * 主数据源
     */
    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "ailpha.datasource")
    public javax.sql.DataSource primaryDataSource() {
        javax.sql.DataSource dataSource = DataSourceBuilder.create().build();
        if (dataSource instanceof HikariDataSource) {
            //时区使用服务端系统时区
            ((HikariDataSource) dataSource).getDataSourceProperties().put("serverTimezone", TimeZone.getDefault().getID());
        }
        return dataSource;
    }

    /**
     * 配置多数据源管理
     */
    @Bean(name = "dynamicDataSource")
    public javax.sql.DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        //默认数据源
        dynamicDataSource.setDefaultTargetDataSource(primaryDataSource());
        //配置多数据源
        Map<Object, Object> dataBaseMap = new HashMap<>(16);
        dataBaseMap.put("mysql", primaryDataSource());
        dynamicDataSource.setTargetDataSources(dataBaseMap);
        return dynamicDataSource;
    }


    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager(@Qualifier("dynamicDataSource") javax.sql.DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    @Value("${mybatis.config-location}")
    private String location;

    @Primary
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(@Qualifier("dynamicDataSource") javax.sql.DataSource dataSource, ResourceLoader resourceLoader) {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setConfigLocation(resourceLoader.getResource(location));
        bean.setPlugins(mybatisPlusInterceptor());
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            String locations;
            if (StringUtils.isEmpty(dataSourceType)) {
                locations = DataSourceType.POSTGRESQL.getLocations();
            } else {
                DataSourceType dataSourceType = DataSourceType.valueOfName(StringUtils.upperCase(this.dataSourceType));
                if (dataSourceType == null) {
                    log.error("数据源类型配置错误，请检查application.properties中datasource.type配置");
                    throw new RuntimeException("数据源类型配置错误，请检查application.properties中datasource.type配置");
                }
                locations = dataSourceType.getLocations();
            }
            bean.setMapperLocations(resourcePatternResolver.getResources(locations));
            return bean.getObject();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Primary
    @Bean(name = "jdbcTemplate")
    public JdbcTemplate sqlSessionTemplate(@Qualifier("dynamicDataSource") javax.sql.DataSource dataSource) {
        return new JdbcTemplate(dataSource, true);
    }
}
