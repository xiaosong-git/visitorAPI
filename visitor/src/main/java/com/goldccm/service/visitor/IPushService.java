package com.goldccm.service.visitor;

import com.goldccm.service.base.IBaseService;

public interface IPushService extends IBaseService {

    void wx_push(String openid, String title, String name, String phone, String date, String reason, String remarkValue, String url) throws Exception;
}
