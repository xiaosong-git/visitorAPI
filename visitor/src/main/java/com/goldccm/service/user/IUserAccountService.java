package com.goldccm.service.user;

import com.goldccm.model.compose.ResultData;
import com.goldccm.service.base.IBaseService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/10 21:20
 */
public interface IUserAccountService extends IBaseService {

    /**
     * 根据用户的id获取用户的账户信息
     * @Author linyb
     * @Date 2017/4/10 21:23
     */
    Map<String,Object> findUserAccountByUser(Integer userId);

    /**
     * 判断用户输入的支付密码是否正确
     * @param userId 用户id
     * @param inputPwd 用户输入的密码
     * @return
     * @throws Exception
     */
    boolean checkPayPwd(Long userId, String inputPwd) throws Exception;

    /**
     * 判断用户是否可以提现
     * @param userId 用户Id
     * @return
     * @throws Exception
     */
    boolean isAllowWithdraw(Long userId) throws Exception;

    /**
     * 判断用户登录密码是否正确
     * @param userId 用户id
     * @param inputPwd 用户输入的密码
     * @return
     * @throws Exception
     */
    boolean checkSysPwd(Long userId, String inputPwd) throws Exception;

    /**
     * 根据用户的id获取用户的账户信息
     * @Author linyb
     * @Date 2017/4/10 21:23
     */
    Map<String,Object> findUserAccountByUserId(Integer userId);
}
