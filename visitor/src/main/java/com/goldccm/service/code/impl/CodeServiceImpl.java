package com.goldccm.service.code.impl;

import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.Status;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.code.ICodeService;
import com.goldccm.service.param.impl.ParamServiceImpl;
import com.goldccm.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 手机验证码
 * @Author linyb
 * @Date 2017/4/3 15:55
 */
@Service("codeService")
public class CodeServiceImpl extends BaseServiceImpl implements ICodeService {
    @Autowired
    private ParamServiceImpl paramService;

    public Boolean verifyCode(String phone, String code, Integer type) {
       //判断参数完整性
        if ((!StringUtils.isEmpty(phone)) && (!StringUtils.isEmpty(code)) && (type != null)) {
            if(Constant.IS_DEVELOP&&"222333".equals(code)){
                System.out.println("测试专用");
                return true;
            }
            //从Redis中取出正确验证码
            //redis修改，原dbNum=7 现在dbNum=31
            Object obj = RedisUtil.getObject(phone.getBytes(),31);
            if(obj == null){
                return false;
            }
            String redisCode = (String)obj;
            //比对
            if ( code.equals(redisCode)) {
                //redis修改，原dbNum=7 现在dbNum=31
                if (type==2){
                    return true;
                }
                RedisUtil.delObject(phone.getBytes(), 31);
                return true;
            }
        }
        return false;
    }

    @Override
    public Result sendMsg(String phone,Integer type, String visitorResult, String visitorBy, String visitorDateTime, String visitor) {
        String code = NumberUtil.getRandomCode(6);
        String limit = paramService.findValueByName("maxErrorInputSyspwdLimit");
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String state = YunPainSmsUtil.sendSmsCode(code, phone, type, date, limit, visitorResult, visitorBy, visitorDateTime, visitor);
        if("0000".equals(state)){
            //redis修改，原dbNum=7 现在dbNum=31
            boolean flag = RedisUtil.setObject(phone.getBytes(), code,60*30, 31) != null;
            return flag ? Result.success() : Result.fail();
        }else {
            return Result.unDataResult("fail",state);
        }
    }
}
