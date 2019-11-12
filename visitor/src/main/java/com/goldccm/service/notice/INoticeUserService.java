package com.goldccm.service.notice;

import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/5/9 14:46
 */
public interface INoticeUserService extends IBaseService {

    /**
     * 根据用户获取对应用户阅读的最新公告Id
     * @Author linyb
     * @Date 2017/5/10 12:27
     */
    Map<String, Object> findByUserId(Integer userId);
}
