package com.goldccm.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by linyb on 2016/12/5.
 */
public class IPUtil {

    //IP地址正则表达式
    private static final String regValidatorIp = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$";

    /**
     * 获取请求的IP地址
     * @Author linyb
     * @Date 2016/12/5 15:22
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        // 如果客户端经过多级反向代理，则X-Forwarded-For的值并不止一个，而是一串IP值，
        // 取X-Forwarded-For中第一个非unknown的有效IP字符串即为用户真实IP
        String ipResult = getIp(ip);
        if (ipResult != null) {
            return ipResult;
        }

        ip = request.getHeader("Proxy-Client-IP");
        ipResult = getIp(ip);
        if (ipResult != null) {
            return ipResult;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        ipResult = getIp(ip);
        if (ipResult != null) {
            return ipResult;
        }
        ip = request.getRemoteAddr();
        ipResult = getIp(ip);
        if (ipResult != null) {
            return ipResult;
        }
        return "";
    }
    private static String getIp(String ips) {
        if(StringUtils.isBlank(ips)){
            return null;
        }
        String[] tokens = ips.split(",");
        for (String s : tokens) {
            if (isIp(s.trim())) {
                return s.trim();
            }
        }
        return null;
    }

    /**
     * 验证是否是有效的IP地址
     * @Author linyb
     * @Date 2016/12/5 15:21
     */
    public static boolean isIp(String value) {
        if (StringUtils.isBlank(value))
            return false;
        return value.matches(regValidatorIp);
    }
}
