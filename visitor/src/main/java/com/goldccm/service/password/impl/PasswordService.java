package com.goldccm.service.password.impl;

import com.goldccm.model.compose.Status;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.password.IPasswordService;
import com.goldccm.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/7/26.
 */
@Service("passwordService")
public class PasswordService extends BaseServiceImpl implements IPasswordService {
    @Autowired
    private IParamService paramService;
    /**
     * 限制用户在一定时间内密码输入错误的次数
     * @param userId 用户Id
     * @param pwdType 密码类型
     * @return
     */
    @Override
    public boolean isErrInputOutOfLimit(String userId, String pwdType) {
        Integer limit = null;
        if(Status.PWD_TYPE_SYS.equals(pwdType)){
            limit = Integer.valueOf(paramService.findValueByName("maxErrorInputSyspwdLimit"));
        }else{
            limit = Integer.valueOf(paramService.findValueByName("maxErrorInputPaypwdLimit"));
        }
        //redis修改，原dbNum=9 现在dbNum=33
        String num = RedisUtil.getStrVal("ErrInputOutOfLimit_" + pwdType + "_"+userId, 33);
        if(num == null){
            return false;
        }
        if(Long.valueOf(num) >= limit){
            return true;
        }
        return false;
    }

    @Override
    public Long addErrInputNum(String userId, String pwdType) {
        /**
         * 获取限制参数
         */
        Integer time = null;
        Integer limit = null;
        if(Status.PWD_TYPE_SYS.equals(pwdType)){
            time = Integer.valueOf(paramService.findValueByName("errorInputSyspwdWaitTime"));
            limit = Integer.valueOf(paramService.findValueByName("maxErrorInputSyspwdLimit"));
        }else{
            time = Integer.valueOf(paramService.findValueByName("errorInputPaypwdWaitTime"));
            limit = Integer.valueOf(paramService.findValueByName("maxErrorInputPaypwdLimit"));
        }
        //redis修改，原dbNum=9 现在dbNum=33
        Long num  = RedisUtil.incr("ErrInputOutOfLimit_" + pwdType + "_"+userId, 33, time*60);
        return limit - num;
    }
    /**
     * 重置允许用户输入错误密码的次数
     * @param userId 用户Id
     * @param pwdType 密码类型
     */
    @Override
    public void resetPwdInputNum(String userId, String pwdType) {
        /**
         * 获取限制参数
         */
        Integer time = null;
        if(Status.PWD_TYPE_SYS.equals(pwdType)){
            time = Integer.valueOf(paramService.findValueByName("errorInputSyspwdWaitTime"));
        }else{
            time = Integer.valueOf(paramService.findValueByName("errorInputPaypwdWaitTime"));
        }
        //redis修改，原dbNum=9 现在dbNum=33
        RedisUtil.setStr("ErrInputOutOfLimit_" + pwdType + "_"+userId, "0", 33, time*60);
    }


}
