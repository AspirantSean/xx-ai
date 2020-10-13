package com.dbapp.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * 国家简码工具类
 *
 * @author limu
 * @version CountryCodeUtil.java, v 0.1 2017年2月22日 下午4:32:49
 * @since 0.1
 */
public class CountryCodeUtil {

  private static JSONObject codeJson = new JSONObject();
  private static BiMap<String, String> biMap = HashBiMap.create();

  static {
    String json_path =
        SystemProperUtil.getConfPath() + SystemProperUtil.getFileSeparator() + "countryCode.json";
    File file = new File(json_path);
    try {
      String strbuf = new String();
      strbuf = FileUtils.readFileToString(file);
      codeJson = JSONObject.parseObject(strbuf);
      for (String key : codeJson.keySet()) {
        //强制写入：重复的key-value，后面插入的覆盖已经存在的
        biMap.forcePut(key, codeJson.getString(key));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * 根据国家名称获取简码
   *
   * @param country
   * @return
   */
  public static String getCode(String country) {
    /**
     * 当传入的国家为局域网，本机地址和保留地址均认为是中国
     */
    if ("局域网".equals(country) || "本机地址".equals(country) || "保留地址".equals(country)) {
      return "CN";
    }
    return biMap.inverse().get(country);
  }

  /**
   * 根据简码获取国家名称
   *
   * @param code
   * @return
   */
  public static String getCountry(String code) {
    return biMap.get(code);
  }
}
