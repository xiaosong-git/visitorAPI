package com.goldccm.service.errorLog;


import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * 插入錯誤日誌地址
 * @author chenwf
 * @date 2019/8/20 16:00
 */
public interface IErrorLogService extends IBaseService {
    /** 
     * 错误日志上传
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/21 15:04
     */
    Result saveErrorLog(Map<String, Object> request) throws Exception;

    Result test(Map<String, Object> paramMap);

//    List findErrorLog(Map<String,Object> paramMap) throws Exception;
}

