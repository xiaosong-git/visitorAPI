package com.goldccm.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Linyb
 * @Date 2017/1/10.
 */
public class BaseUtil {

    /**
     * 随机获取一串字符串
     * @Author Linyb
     * @Date 2017/1/10 16:16
     */
    public static String getRandomStr(int length){
        return UUID.randomUUID().toString().substring(0,4);
    }

    public static Integer objToInteger(Object obj, Integer def){
        if(obj == null) {
            return def;
        }
        return Integer.parseInt(obj.toString());
    }

    public static String objToStr(Object obj,String def){
        if(obj == null) return def;
        return obj.toString();
    }

    public static BigDecimal objToBigdecimal(Object obj, BigDecimal def){
        if(obj == null) return def;
        return new BigDecimal(obj.toString());
    }

    public static Long objToLong(Object obj, Long def){
        if(obj == null) return def;
        String s = obj.toString();
        return Long.valueOf(s);
    }

    public static Map<String,Object> remove(Map<String,Object> beRemoveMap ,String ... keys){
        for (String key : keys) {
            beRemoveMap.remove(key);
        }
        return beRemoveMap;
    }

    /**
     * 判断字符串中是否包含中文，存在中文->返回true,否则返回false
     * @param str 要判断的字符串
     * @return
     * LZ
     */
    public static boolean isContainChinese(String str) {
        if(str != null){
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            Matcher m = p.matcher(str);
            if (m.find()) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static Map<String, Object> obj2Map(Object obj) {
        Map<String, Object> map=new HashMap<String, Object>();
        Field[] fields=obj.getClass().getDeclaredFields(); // 获取对象对应类中的所有属性域
        for (int i = 0; i < fields.length; i++) {
            String varName = fields[i].getName();
            varName=varName.toUpperCase();///将key置为大写，默认为对象的属性
            boolean accessFlag=fields[i].isAccessible(); // 获取原来的访问控制权限
            fields[i].setAccessible(true);// 修改访问控制权限
            try {
                Object object =fields[i].get(obj); // 获取在对象中属性fields[i]对应的对象中的变量
                if (object!=null) {
                    map.put(varName, object);
                }else {
                    map.put(varName, null);
                }
                fields[i].setAccessible(accessFlag);// 恢复访问控制权限
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return map;
    }
}
