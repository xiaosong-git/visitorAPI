package com.goldccm.service.news;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * Description:新闻相关
 * Author: LZ
 * Date:2018/1/22 14:21
 */
public interface INewsService extends IBaseService {
    /**
     * 通过状态查询
     * @param status
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    Result findByStatus(Map<String, Object> status, Integer pageNum, Integer pageSize) throws Exception;

    Result findNews(Map<String, Object> paramMap);
}
