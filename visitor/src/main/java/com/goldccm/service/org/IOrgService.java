package com.goldccm.service.org;


import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
public interface IOrgService extends IBaseService {

    /**
     * 地址请求大厦
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result requestMansion(Map<String, Object> paramMap) throws Exception;
    /**
     * 根据用户id获取大楼编号
     * @param userId
     * @return
     * @throws Exception
     */
    String findOrgCodeByUserId(Integer userId) throws Exception;

    String findOrgCodeByCompanyId(Integer companyId) throws Exception;
}
