package com.goldccm.service.checkInWork;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * 打卡统计接口
 * @author cwf
 * @date 2019/11/22 17:41
 */
public interface StatisticsService extends IBaseService {
     //上下班统计
     Result offDutyStatistics(Map<String, Object> paramMap);
}
