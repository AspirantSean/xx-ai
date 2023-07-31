package com.dbapp.extension.sync.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

@Slf4j
@Configuration
@MapperScan(basePackages = {"com.dbapp.**.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory")
public class DataSourceConfiguration {

    @Value("${mybatis.config-location}")
    private String location;

    @Primary
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(ResourceLoader resourceLoader) throws Exception {
        return SqlSessionManager.newInstance(resourceLoader.getResource(location).getInputStream());
    }

    @Bean
    public org.apache.ibatis.session.Configuration configuration(SqlSessionFactory sqlSessionFactory) {
        return sqlSessionFactory.getConfiguration();
    }

    @Bean(name = "datasource")
    public DataSource dataSource(org.apache.ibatis.session.Configuration configuration) {
        return configuration.getEnvironment().getDataSource();
    }
}
