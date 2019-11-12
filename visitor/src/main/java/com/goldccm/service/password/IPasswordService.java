package com.goldccm.service.password;

import com.goldccm.service.base.IBaseService;

/**
 * 错误输入密码的Service
 * Created by LZ on 2017/7/26.
 */
public interface IPasswordService extends IBaseService {
    /**
     * 限制用户在一定时间内密码输入错误的次数
     * @param userId 用户Id
     * @param pwdType 密码类型
     * @return
     */
    boolean isErrInputOutOfLimit(String userId, String pwdType);
    /**
     * 累加错误输入密码次数
     * @param userId 用户Id
     * @param pwdType 密码类型
     * @return
     */
    Long addErrInputNum(String userId, String pwdType);

    /**
     * 重置允许用户输入错误密码的次数
     * @param userId 用户Id
     * @param pwdType 密码类型
     */
    void resetPwdInputNum(String userId, String pwdType);
}
