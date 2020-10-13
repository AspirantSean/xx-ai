package com.dbapp.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

    private static final String GLOBAL_PATH = SystemProperUtil.getConfPath() + SystemProperUtil.getFileSeparator() + "global.properties";

    private static Properties globalProperties;

    private GlobalAttribute() {
        throw new IllegalStateException("properties file class");
    }

    /**
     * 初始化配置文件
     */
    public static void init() {
        globalProperties = new Properties();
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(GLOBAL_PATH), StandardCharsets.UTF_8)) {
            //设置编码格式解决乱码
            globalProperties.load(in);
        } catch (Exception e) {
            System.err.println("配置文件加载失败了");
        }
    }

    /**
     * 修改global.properties文件配置
     *
     * @param key   配置的键
     * @param value 配置的值
     * @return 修改结果
     */
    public synchronized static boolean setValue(@NotNull String key, String value) {
        return setValue(GLOBAL_PATH, key, value);
    }

    /**
     * 修改path路径的文件配置信息
     *
     * @param path  文件路径
     * @param key   配置的键
     * @param value 配置的值
     * @return 修改结果
     */
    public synchronized static boolean setValue(@NotNull String path, @NotNull String key, String value) {
        boolean isNotChanged = true;
        File propertiesFile = new File(path);
        try {
            List<String> lines = FileUtils.readLines(propertiesFile);
            FileUtils.write(propertiesFile, "", StandardCharsets.UTF_8, false);// 清空原文件
            for (String line : lines) {
                int index;
                if (StringUtils.isBlank(line)
                        || (index = line.indexOf('=')) == -1) {
                    continue;
                }
                if (isNotChanged && line.substring(0, index).trim().equals(key.trim())) {
                    FileUtils.write(propertiesFile, String.format("%s=%s%n", key, value), StandardCharsets.UTF_8, true);
                    isNotChanged = false;
                } else {
                    FileUtils.write(propertiesFile, String.format("%s%n", line), StandardCharsets.UTF_8, true);
                }
            }
            if (isNotChanged) {
                FileUtils.write(propertiesFile, String.format("%s=%s%n", key, value), StandardCharsets.UTF_8, true);
                isNotChanged = false;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return !isNotChanged;
    }

    /**
     * 移除global.properties文件配置
     *
     * @param key 键
     * @return 移除结果
     */
    public static boolean removeKey(@NotNull String key) {
        boolean isNotRemoved = true;
        File propertiesFile = new File(GLOBAL_PATH);
        try {
            List<String> lines = FileUtils.readLines(propertiesFile);
            FileUtils.write(propertiesFile, "", StandardCharsets.UTF_8, false);// 清空原文件
            for (String line : lines) {
                int index;
                if (StringUtils.isBlank(line)
                        || (index = line.indexOf('=')) == -1) {
                    continue;
                }
                if (isNotRemoved && line.substring(0, index).trim().equals(key.trim())) {
                    isNotRemoved = false;
                } else {
                    FileUtils.write(propertiesFile, String.format("%s%n", line), StandardCharsets.UTF_8, true);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return !isNotRemoved;
    }

    /**
     * 获取global.properties配置副本
     *
     * @return 配置
     */
    public static Properties getGlobalProperties() {
        Properties properties = new Properties();
        properties.putAll(globalProperties);
        return properties;
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
     * 获取kye的配置内容，并转为double
     *
     * @param key        键
     * @param defaultVal 缺省值
     * @return 值
     */
    public static double getPropertyDouble(String key, double defaultVal) {
        String val = Objects.toString(globalProperties.getProperty(key), Double.toString(defaultVal));
        return Double.parseDouble(val);
    }

    /**
     * 获取kye的配置内容，并转为long
     *
     * @param key        键
     * @param defaultVal 缺省值
     * @return 值
     */
    public static long getPropertyLong(String key, long defaultVal) {
        String val = Objects.toString(globalProperties.getProperty(key), Long.toString(defaultVal));
        return Long.parseLong(val);
    }

    /**
     * 获取kye的配置内容，并转为boolean
     *
     * @param key        键
     * @param defaultVal 缺省值
     * @return 值
     */
    public static boolean getPropertyBoolean(String key, boolean defaultVal) {
        String val = globalProperties.getProperty(key);
        return StringUtils.isBlank(val) ? defaultVal : Boolean.parseBoolean(val);
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

    /**
     * 获取kye的配置内容
     *
     * @param key 键
     * @return 值
     */
    public static String getProperty(String key) {
        return globalProperties.getProperty(key);
    }

}
