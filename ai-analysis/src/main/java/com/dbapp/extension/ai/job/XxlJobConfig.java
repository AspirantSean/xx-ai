package com.dbapp.extension.ai.job;

import com.dbapp.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class XxlJobConfig {

    private final static Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

    @Value("${xxl.job.admin.addresses}")
    private String adminAddress;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.app-name}")
    private String appName;

    @Value("${server.port:9001}")
    private Integer port;

    @Value("${server.ssl.enabled:false}")
    private boolean ssl;

    @Value("${xxl.job.executor.log-path}")
    private String logPath;

    @Value("${xxl.job.executor.log-retention-days}")
    private int logRetentionDays;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Bean
    public XxlJobSpringExecutor xxlJobSpringExecutor() {
        String protocol = ssl ? "https://" : "http://";
        String address = protocol + getLocalIp() + ":" + port + contextPath;
        logger.info(">>>>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddress);
        xxlJobSpringExecutor.setAppName(appName);
        xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }

    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn(e.getMessage(), e);
            return "1.ai1";
        }
    }
}
