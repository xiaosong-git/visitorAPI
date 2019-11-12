package com.goldccm.service.code;


import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

/**
 * @Author linyb
 * @Date 2017/4/3 15:55
 */
public interface ICodeService extends IBaseService {
    /**
     * 验证验证码
     * @Author linyb
     * @Date 2017/4/3 15:57
     */
    Boolean verifyCode(String phone,String code,Integer type);
    /**
     * 发送短信验证码
     * @Author linyb
     * @Date 2017/4/3 16:09
     */
    Result sendMsg(String phone, Integer type, String visitorResult, String visitorBy, String visitorDateTime, String visitor);
}
