package com.goldccm.service.checkInWork;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Map;

/**
 * @program: visitor
 * @description:打卡接口
 * @author: cwf
 * @create: 2019-11-04 10:25
 **/
public interface CheckInWorkService extends IBaseService {
    //保存规则
    Result saveGroup(HttpServletRequest request);
    //获取用户打卡规则与数据
    Result gainWorkOne(Map<String, Object> paramMap) throws ParseException;
    //打卡
    Result saveWork(Map<String, Object> paramMap);

    Result gainMonthStatistics(Map<String, Object> paramMap);

    Result gainCalendarStatistics(Map<String, Object> paramMap);
}
