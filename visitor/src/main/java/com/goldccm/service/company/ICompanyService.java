package com.goldccm.service.company;


import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
public interface ICompanyService extends IBaseService {

    /**
     * 大厦请求公司
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result requestCompany(Map<String, Object> paramMap) throws Exception;
}
