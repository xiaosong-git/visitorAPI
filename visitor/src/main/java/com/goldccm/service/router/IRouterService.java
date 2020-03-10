package com.goldccm.service.router;

import com.goldccm.model.compose.Result;

import java.util.Map;

/**
 * @program: goldccm
 * @description: 路由接口
 * @author: cwf
 * @create: 2019-12-30 17:44
 **/
public interface IRouterService {
    Result router(Map<String, Object> paramMap, Integer pageNum, Integer pageSize);
}
