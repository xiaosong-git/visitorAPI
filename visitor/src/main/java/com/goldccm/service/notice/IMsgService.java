package com.goldccm.service.notice;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

public interface IMsgService extends IBaseService {


    Result getMsg(Map<String, Object> paramMap);
}
