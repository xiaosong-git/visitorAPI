package com.goldccm.service.errorLog.impl;

import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.errorLog.IErrorLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: visitor
 * @description: 错误日志上传
 * @author: cwf
 * @create: 2019-08-26 23:32
 **/
@Service("errorLogService")
public class IErrorLogServiceImpl extends BaseServiceImpl implements IErrorLogService {
    Logger log = LoggerFactory.getLogger(IErrorLogServiceImpl.class);

    @Override
    public Result saveErrorLog(Map<String, Object> paramMap) throws Exception {

        int save = save(TableList.ERROR_LOG, paramMap);
        return save>0?Result.success():Result.fail();
    }
}
