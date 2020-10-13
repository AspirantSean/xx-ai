package com.dbapp.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * 二维码工具类
 * 
 * @author limu
 * @version BarcodeUtil.java, v 0.1 2017年5月10日 上午10:42:06 
 * @since 0.1
 */
public class BarcodeUtil {
    public static String generateQrCode() {
        try {
            String mac = getMacAddress();
            String info = mac+ "||AILPHA||"+mac+"||无||无||1";
            byte[] encoding = Base64.encodeBase64(info.getBytes(Charset.forName("UTF-8")));
            return new String(encoding, Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * 获取mac地址
     * 
     * @return
     */
    public static String getMacAddress() {
        try {
            String mac = getMacSignature();
            mac = Md5Util.md5To16Bit(mac).toUpperCase();
            mac = mac.substring(0, 4) + "-" + mac.substring(4, 8) + "-" + mac.substring(8, 12) + "-"
                  + mac.substring(12, 16);
           return mac;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取linux网卡
     * 
     * @return
     */
    public static String getMacSignature() {
        List<String> macs = new ArrayList<String>();

        try {

          Enumeration<NetworkInterface> networks = NetworkInterface
              .getNetworkInterfaces();
          while (networks.hasMoreElements()) {
            NetworkInterface network = networks.nextElement();
            byte[] mac = network.getHardwareAddress();

            if (mac != null) {

              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i],
                    (i < mac.length - 1) ? "-" : ""));
              }
              if (StringUtils.isNotEmpty(sb.toString())) {
                  macs.add(sb.toString());
              }
            }
          }
        } catch (SocketException e) {
          e.printStackTrace();
        }

        return StringUtils.join(macs, "--");
      }
}
