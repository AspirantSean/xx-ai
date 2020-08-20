package com.dbapp.model.ai.quartz;

import com.dbapp.utils.SystemProperUtil;
import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class QuartzConfig {
    private Logger logger = LoggerFactory.getLogger(QuartzConfig.class);
    @Resource
    private MyJobFactory myJobFactory;


    @Bean(name = "SchedulerFactory")
    public SchedulerFactoryBean schedulerFactoryBean() {
        //Spring提供SchedulerFactoryBean为Scheduler提供配置信息,并被Spring容器管理其生命周期
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        // 延时启动(秒)
        factory.setStartupDelay(5);
        //设置自定义Job Factory，用于Spring管理Job bean
        factory.setJobFactory(myJobFactory);
        try {
            factory.setQuartzProperties(quartzProperties());
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new FileSystemResource(SystemProperUtil.getConfPath() + SystemProperUtil.getFileSeparator() + "quartz.properties"));
        //在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    /*
     * quartz初始化监听器
     */
//    @Bean
    public QuartzInitializerListener executorListener() {
        return new QuartzInitializerListener();
    }

    /*
     * 通过SchedulerFactoryBean获取Scheduler的实例
     */
    @Bean(name = "Scheduler")
    public Scheduler scheduler() {
        return schedulerFactoryBean().getScheduler();
    }
}
