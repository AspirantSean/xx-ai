package com.dbapp.extension.sync.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 * 定义一个全局的属性类
 *
 * @author limu
 * @version GlobalAttribute.java, v 0.1 2017年2月21日 下午10:00:41
 * @since 0.1
 */
public final class GlobalAttribute {

    private static Properties globalProperties;

    private GlobalAttribute() {
    }

    static {
        init();
    }

    /**
     * 初始化配置文件
     */
    public static void init() {
        globalProperties = new Properties();
        try (InputStreamReader in = new InputStreamReader(
                SystemProperUtil.getGlobalPropertiesPathResource().getInputStream(),
                StandardCharsets.UTF_8)) {
            //设置编码格式解决乱码
            globalProperties.load(in);
        } catch (Exception e) {
            System.err.println("配置文件加载失败了");
            e.printStackTrace();
        }
    }

    /**
     * 获取kye的配置内容，并转为int
     *
     * @param key        键
     * @param defaultVal 缺省值
     * @return 值
     */
    public static int getPropertyInteger(String key, int defaultVal) {
        String val = Objects.toString(globalProperties.getProperty(key), Integer.toString(defaultVal));
        return Integer.parseInt(val);
    }

    /**
     * 获取kye的配置内容
     *
     * @param key        键
     * @param defaultVal 缺省值
     * @return 值
     */
    public static String getPropertyString(String key, String defaultVal) {
        String val = globalProperties.getProperty(key);
        return StringUtils.isBlank(val) ? defaultVal : val;
    }

}
