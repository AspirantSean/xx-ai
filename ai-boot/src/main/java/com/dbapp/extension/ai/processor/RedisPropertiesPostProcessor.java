package com.dbapp.extension.ai.processor;

import com.dbapp.extension.ai.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RedisPropertiesPostProcessor implements BeanPostProcessor {

    private final static String REDIS_PROPERTIES_BEAN_NAME = "spring.redis-org.springframework.boot.autoconfigure.data.redis.RedisProperties";
    @Override
    public Object postProcessAfterInitialization(@Nullable Object bean, @Nullable String beanName) throws BeansException {
        if (bean == null || beanName == null) {
            // 不做处理
            return bean;
        }
        if (REDIS_PROPERTIES_BEAN_NAME.equals(beanName) && bean.getClass() == RedisProperties.class) {
            // 根据配置调整 redisProperties 的配置
            RedisProperties redisProperties = (RedisProperties) bean;
            // 从配置文件中读取主从集群的配置
            String sentinelMaster = PropertiesUtil.fromCoreProperties("redis.sentinel.master");
            if (StringUtils.isBlank(sentinelMaster)) {
                log.info("No redis sentinel master from auth core properties, no change.");
                return redisProperties;
            }
            String sentinelNodes = PropertiesUtil.fromCoreProperties("redis.sentinel.nodes");
            if (StringUtils.isBlank(sentinelMaster)) {
                log.info("No redis sentinel nodes from auth core properties, no change.");
                return redisProperties;
            }
            List<String> nodes = Arrays.stream(sentinelNodes.split(",")).collect(Collectors.toList());
            // 更新连接信息
            RedisProperties.Sentinel sentinel = redisProperties.getSentinel();
            sentinel.setMaster(sentinelMaster);
            sentinel.setNodes(nodes);
            log.info("redis sentinel master {} and nodes {} has changed from auth core properties.", sentinelMaster, sentinelNodes);
            return redisProperties;
        } else {
            // 不做处理
            return bean;
        }
    }
}
