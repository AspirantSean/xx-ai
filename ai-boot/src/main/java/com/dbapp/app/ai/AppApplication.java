package com.dbapp.app.ai;

import com.dbapp.utils.GlobalAttribute;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
@ServletComponentScan
@PropertySource("classpath:global.properties")
@EnableFeignClients(basePackages = {"com.dbapp.app.*.rpc", "com.dbapp.model.ai.api"})
@MapperScan({"com.dbapp.model.ai.mapper"})
@ComponentScan(basePackages = "com.dbapp")
@EnableDiscoveryClient
@EnableRedisHttpSession
public class AppApplication {
    private static final Logger LOG = LoggerFactory.getLogger(AppApplication.class);

    static {
        //springboot2.X，redis和es存在冲突，加参数解决
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        GlobalAttribute.init();
        SpringApplication.run(AppApplication.class, args);
        LOG.info("app-ai application started!");
    }

}
