package com.goldccm.service.shortMessage;


import com.goldccm.model.compose.Result;

import java.util.Map;

/**
 * 短信接口
 * @author chenwf
 * @date 2019/7/22 17:38
 */
public interface IShortMessageService {

    public Result sendShortMessage(Map<String, Object> paramMap);

    Map<String, Object> findCompanybyId(Integer companyId);
    //友盟推送
    boolean YMNotification(String deviceToken,String deviceType,String notification_title ,String msg_content,String isOnline) throws Exception;
}
