package com.goldccm.util;

/**
 * 检查重复提交的工具类
 * Created by LZ on 2017/7/6.
 */
public class RepeatUtil {

    public static boolean isManualPayment(String key) throws Exception{
        //redis修改，原dbNum=9 现在dbNum=33
        Long num = RedisUtil.incr(key,33,300);
        if(num > 1){
            return true;
        }
        return false;
    }

    public static boolean isRepeat(String key) throws Exception{
        //redis修改，原dbNum=9 现在dbNum=33
//        Long num = RedisUtil.incr(key,9,30);
        Long num = RedisUtil.incr(key,33,30);
        if(num > 1){
            return true;
        }
        return false;
    }

    public static boolean isGetSignMsg(String key) throws Exception{
        //redis修改，原dbNum=9 现在dbNum=33
        Long num = RedisUtil.incr(key,33,60);
        if(num > 1){
            return true;
        }
        return false;
    }

    /**
     * 判断是否可以提现
     * @param key
     * @param successTime 成功时间限制
     * @param failTime 失败时间限制
     * @return
     * @throws Exception
     */
    public static int canWithdraw(String key, Integer successTime, Integer failTime) throws Exception{
        //redis修改，原dbNum=10 现在dbNum=34
        String value = RedisUtil.getStrVal(key, 34);
        if(value == null){
            return 0;
        }
        String[] values = value.split(",");
        String currTime = DateUtil.getCurrentDateTime("yyyy-MM-dd HH:mm");
        String lastTime = values[1];
        int minutes = DateUtil.minutesBetween(lastTime, currTime);
        if("success".equals(values[0])){
            return minutes>successTime ? 0:1;
        }else{
            return minutes>failTime ? 0:2;
        }


    }


}
