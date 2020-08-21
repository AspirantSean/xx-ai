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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@EnableAsync
@EnableCaching
@ServletComponentScan
@PropertySource("classpath:global.properties")
@EnableFeignClients(basePackages = {"com.dbapp.app.*.rpc","com.dbapp.model.ai.api"})
@MapperScan({"com.dbapp.model.ai.mapper"})
@ComponentScan(basePackages = "com.dbapp")
@EnableDiscoveryClient
//@EnableAilphaApp
public class AiApp {
    private static final Logger LOG = LoggerFactory.getLogger(AiApp.class);

    static {
        //springboot2.X，redis和es存在冲突，加参数解决
        System.setProperty("es.set.netty.runtime.available.processors","false");
    }

    public static void main(String[] args) {
        GlobalAttribute.init();
        SpringApplication.run(AiApp.class, args);
        LOG.info("app-ai application started!");
    }

    @Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(GlobalAttribute.getPropertyBoolean("swagger.enable", false))
                .groupName("bigdata-ui")
                .apiInfo(new ApiInfoBuilder().title("AI异常分析服务API").description("AI异常分析服务API列表").build())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

}
