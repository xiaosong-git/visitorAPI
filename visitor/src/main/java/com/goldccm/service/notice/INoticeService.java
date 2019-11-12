package com.goldccm.service.notice;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

/**
 * @Author linyb
 * @Date 2017/4/16 10:02
 */
public interface INoticeService extends IBaseService {
    /**
     * 根据用户获取对应的公告
     * @Author linyb
     * @Date 2017/4/16 15:51
     */
    Result findNoticeByUser(Integer userId, Integer pageNum, Integer pageSize);
    /**
     * 根据用户的公司Id orgId获取对应的公告
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/2 15:59
     */
    Result findBySidCompany(Integer userId, Integer pageNum, Integer pageSize) throws Exception ;
}
