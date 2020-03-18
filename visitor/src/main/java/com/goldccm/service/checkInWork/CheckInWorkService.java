package com.goldccm.service.checkInWork;

import com.alibaba.fastjson.JSONObject;
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
    //添加规则
    Result addGroup(JSONObject jsonObject);

    //管理员获取规则
    Result gainGroupIndex(Map<String, Object> paramMap);

    //管理员获取规则
    Result gainGroupDetail(Map<String, Object> paramMap);

    //获取用户打卡规则与数据
    Result gainWork(Map<String, Object> paramMap) throws ParseException;
    //打卡
    Result saveWork(Map<String, Object> paramMap);

    Result outWork(Map<String, Object> paramMap);

    Result gainMonthStatistics(Map<String, Object> paramMap) throws ParseException;

    Result companyUser(Map<String, Object> paramMap);

    Result gainCalendarStatistics(Map<String, Object> paramMap);

    Result effective(Map<String, Object> paramMap) throws ParseException;

    Result gainDay(Map<String, Object> paramMap);
}
