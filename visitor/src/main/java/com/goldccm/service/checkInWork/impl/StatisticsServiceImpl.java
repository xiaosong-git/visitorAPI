package com.goldccm.service.checkInWork.impl;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.checkInWork.StatisticsService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: goldccm
 * @description: 打卡统计接口实现类
 * @author: cwf
 * @create: 2019-11-22 17:42
 **/
@Service("statisticsService")
public class StatisticsServiceImpl  extends BaseServiceImpl implements StatisticsService {

    /**
     * 管理员上下班统计
     * @param paramMap
     * @return  normalCount--正常人数，exceptionCount--异常人数，lateCount--迟到人数，
     *          earlyCount--早退人数 absentCount--缺卡人数
     * @throws Exception
     * @author cwf
     * @date 2019/11/25 10:08
     */
    @Override
    public Result offDutyStatistics(Map<String, Object> paramMap) {

        return null;
    }
}
