package com.goldccm.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.user.IUserAccountService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/10 21:20
 */
@Service("userAccountService")
public class UserAccountServiceImpl extends BaseServiceImpl implements IUserAccountService {
    @Autowired
    private IParamService paramService;
    @Autowired
    private IBaseDao baseDao;


    @Override
    public Map<String, Object> findUserAccountByUser(Integer userId) {
        if(userId == null || userId == 0){
            return null;
        }
        String sql = " select * from "+ TableList.USER_ACCOUNT +" where userId = "+userId;
        return baseDao.findFirstBySql(sql);
    }

    /**
     * 判断用户输入的支付密码是否正确
     * @param userId 用户id
     * @param inputPwd 用户输入的密码
     * @return
     * @throws Exception
     */
    @Override
    public boolean checkPayPwd(Long userId, String inputPwd) throws Exception {
        Map<String,Object> userAccount = this.findFirstBySql("select payPwd from "+ TableList.USER_ACCOUNT +" where userId = "+userId);
        String payPwdMD5 = BaseUtil.objToStr(userAccount.get("payPwd"),null);
        return payPwdMD5.equals(inputPwd);
    }

    /**
     * 判断用户是否可以提现
     * @param userId 用户Id
     * @return
     * @throws Exception
     */
    @Override
    public boolean isAllowWithdraw(Long userId) throws Exception {
        if(userId != null){
            String sql = " select * from "+ TableList.USER_ACCOUNT +" where userId = "+userId;
            Map<String, Object> account = baseDao.findFirstBySql(sql);
            String allowWithdraw = BaseUtil.objToStr(account.get("isAllowWithdraw"), "T");
            return allowWithdraw.equals("T");
        }
        return false;
    }

    @Override
    public boolean checkSysPwd(Long userId, String inputPwd) throws Exception {
        Map<String,Object> userAccount = this.findFirstBySql("select sysPwd from "+ TableList.USER_ACCOUNT +" where userId = "+userId);
        String payPwdMD5 = BaseUtil.objToStr(userAccount.get("sysPwd"),null);
        return payPwdMD5.equals(inputPwd);
    }

    @Override
    public Map<String, Object> findUserAccountByUserId(Integer userId) {
        if(userId == null || userId == 0){
            return null;
        }
        //用户余额
        String sql = " select * from "+ TableList.USER_ACCOUNT +" where userId = "+userId;
        Map<String, Object> userAccount = baseDao.findFirstBySql(sql);
        return userAccount;
    }
}
