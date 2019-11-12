package com.goldccm.util;

/**
 * 密码输入错误限制
 * Created by Administrator on 2017/7/26.
 */
public class PwdErrorLimitUtil {
    /**
     * 限制用户在一定时间内密码输入错误的次数
     * @param userId 用户Id
     * @param pwdType 密码类型
     * @param limit 限制次数次数
     * @return
     */
    public static boolean isErrInputOutOfLimit(String userId, String pwdType, Integer limit){
        //redis修改，原dbNum=9 现在dbNum=33
        String num = RedisUtil.getStrVal("ErrInputOutOfLimit_" + pwdType + "_"+userId, 33);
        if(num == null){
            return false;
        }
        if(Long.valueOf(num) > limit){
            return true;
        }
        return false;
    }

    /**
     * 累加错误输入密码次数
     * @param userId 用户Id
     * @param pwdType 密码类型
     * @param time 时间(分钟)
     * @param limit 次数
     * @return
     */
    public static Long addErrInputNum(String userId, String pwdType, Integer time, Integer limit){
        //redis修改，原dbNum=9 现在dbNum=33
        return RedisUtil.incr("ErrInputOutOfLimit_" + pwdType + "_"+userId, 33, time*60);
    }

}
