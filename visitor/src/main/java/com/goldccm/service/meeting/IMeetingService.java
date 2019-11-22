package com.goldccm.service.meeting;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.math.BigDecimal;
import java.util.Map;

public interface IMeetingService extends IBaseService {
    /** 
     * 查询会议室信息 
     * @param paramMap	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/5 17:52
     */
    Result getMeeting (Map<String,Object> paramMap,Integer pageNum,Integer pageSize) throws  Exception;
    /** 
     * 查询会议室预定情况 
     * @param paramMap	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/5 17:53
     */
    Result getRoomStatus (Map<String,Object> paramMap) throws  Exception;
    /** 
     * 预定会议室 
     * @param paramMap	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/5 17:53
     */
    Result reserveMeeting (Map<String,Object> paramMap) throws  Exception;
    /**
     * 判断是否已被预定
     * @param room_id
     * @param apply_date
     * @param start_time
     * @param end_time
     * @return boolean
     * @throws Exception
     * @author chenwf
     * @date 2019/8/6 17:49
     */
    boolean isReserve (Integer room_id, String apply_date, BigDecimal start_time, BigDecimal end_time);
    /** 
     * 公司、大楼会议室预定情况 
     * @param paramMap	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/5 17:54
     */
    Result getMyReserveMeeting (Map<String,Object> paramMap,Integer pageNum,Integer pageSize) throws  Exception;
    /** 
     * 取消预定会议室 
     * @param paramMap	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/5 17:55
     */
    Result cancleMeeting (Map<String,Object> paramMap) throws  Exception;

    /** 
     * 重新预定会议室
     * @param paramMap	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/5 17:59
     */
    Result reTryReserve (Map<String,Object> paramMap) throws  Exception;

    /** 
     * 统计预定费用，时长 
     * @param paramMap	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/7 17:05
     */
    Result statistics(Map<String, Object> paramMap);
    /**
     * 查询预定者相关信息
     * @param userId
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/17 17:05
     */
    Map<String, Object> findByUserId(Integer userId);

    Result addRoom(Map<String, Object> paramMap);

    Result getFromOrgCode(Map<String, Object> paramMap,Integer pageNum, Integer pageSize);

    Result getFromOrgCodeConfirm(Map<String, Object> paramMap);
}
