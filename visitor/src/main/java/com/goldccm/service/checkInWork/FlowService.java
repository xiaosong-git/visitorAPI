package com.goldccm.service.checkInWork;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 *
 * @program: visitor
 * @description:流程控制接口
 * @author: cwf
 * @create: 2019-11-19 10:25
 */
public interface FlowService extends IBaseService {
    //创建流程
    Result createFlow(Map<String, Object> paramMap);
    //查看流程
    Result checkFlow(Map<String, Object> paramMap,Integer pageNum,Integer pageSize);
    //审批流程
    Result approveFlow(Map<String, Object> paramMap);
    //我审批的流程
    Result myApprove(Map<String, Object> paramMap, Integer pageNum, Integer pageSize);

    Result approveDetail(Map<String, Object> paramMap);
}
