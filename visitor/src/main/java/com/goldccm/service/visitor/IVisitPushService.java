package com.goldccm.service.visitor;

import com.goldccm.service.base.IBaseService;

import java.util.Map;

public interface IVisitPushService extends IBaseService {

    void wxPush(String openid, String title, String name, String phone, String date, String reason, String remarkValue, String url) throws Exception;


    //访问推送
    void visitPush(String startDate, Map<String, Object> user, Map<String, Object> visitor, Map<String, Object> data, Integer shortType) throws Exception;
}
