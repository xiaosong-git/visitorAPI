package com.goldccm.service.visitor;


import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
public interface IVisitorRecordService extends IBaseService {

    /**
     * 访问我的人
     * @param paramMap
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */

    Result visitMyPeople(Map<String, Object> paramMap, Integer pageNum, Integer pageSize, Integer recordType) throws Exception;

    /**
     * 访问我公司的其他人
     * @param paramMap
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    Result visitMyCompany(Map<String, Object> paramMap, Integer pageNum, Integer pageSize) throws Exception;

    /**
     * 正在申请访问
     * @return
     * @throws Exception
     */
    Result adoptionAndRejection(Map<String, Object> paramMap) throws Exception;

    /**
     * 我访问的人
     * @param paramMap
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    Result peopleIInterviewed(Map<String, Object> paramMap, Integer pageNum, Integer pageSize) throws Exception;

    /**
     * 我访问的人通过记录
     * @param paramMap
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    Result peopleIInterviewedRecord(Map<String, Object> paramMap, Integer pageNum, Integer pageSize) throws Exception;

//
//    /**
//     * 添加访问请求
//     * @return
//     * @throws Exception
//     */
//    Result visitRequest(Map<String, Object> paramMap) throws Exception;

    /**
     * 查询是否已经提交访问
     * @param userId
     * @param visitorId
     * @param cstatus
     * @author chenwf
     * @date 2019/7/29 15:43
     * @return
     * @throws Exception
     *
     */
    Map<String,Object> findByRepeat(Integer userId,Integer visitorId,String cstatus) throws Exception;
//    Map<String,Object> findByRepeat(Integer userId,Integer visitorId, Integer companyId,String cstatus) throws Exception;

    /**
     * 批量获取授权访问
     * @param pospCode
     * @param orgCode
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    Result findOrgCode(String pospCode,String orgCode, Integer pageNum, Integer pageSize) throws Exception;

    /**
     * 获取单个授权访问
     * @param pospCode
     * @param orgCode
     * @param soleCode
     * @return
     * @throws Exception
     */
    Result findBySoleCode(String pospCode,String orgCode,String soleCode,String visitId) throws Exception;

    /**
     * 确认访问数据
     * @param pospCode
     * @param orgCode
     * @param isData
     * @return
     * @throws Exception
     */
    Result findOrgCodeConfirm(String pospCode,String orgCode,String isData) throws Exception;

    /**
     * 单条扫码结果记录
     * @param pospCode
     * @param orgCode
     * @param visitId
     * @param inOrOut
     * @param visitDate
     * @param visitTime
     *
     */
    Result uploadAccessRecord(String pospCode,String orgCode,String visitId,String inOrOut,String visitDate,String visitTime) throws Exception;
    /** 
     * 公司后台管理发送邀约
     * @param paramMap	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/21 15:34
     */
    Result sendShortMessage(Map<String, Object> paramMap) throws Exception;
    /**
     * 处理扫码邀约
     * @param paramMap
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/8/22 11:23
     */
    Result dealQrcodeUrl(Map<String, Object> paramMap) throws Exception;
    /** 
     * 保存访客申请记录
     * @param paramMap	 
     * @return int 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/7/29 15:43
     */
    int saveVisitRecord(Map<String, Object> paramMap)throws Exception;
    /**
     * 申请访问
     * @param paramMap
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/7/29 17:33
     */
    Result visit (Map<String, Object> paramMap) throws Exception;
    /**
     * webSocket回应邀约
     * @param session
     * @param msg
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/7/29 17:33
     */
    void visitReply (WebSocketSession session,JSONObject msg) throws Exception;
    /**
     * 处理邀约消息
     * @param session
     * @param msg
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/7/30 14:14
     */
    void receiveVisit (WebSocketSession session,JSONObject msg) throws Exception;

//    Result visitAgree (Map<String, Object> paramMap) throws Exception;


    Result visitRecord(Map<String, Object> paramMap, Integer pageNum, Integer pageSize,Integer recordType);

    Result findRecordFromId(Map<String, Object> paramMap);


    Result updateRecord(Map<String, Object> paramMap) throws Exception;
    
    Result forwarding(String visitor, String visitorBy, String companyId, String startDate) throws Exception;

    Result visitForwarding(Map<String, Object> paramMap) throws Exception;
    //访问时填写公司
    Result modifyCompanyFromId(Map<String, Object> paramMap) throws Exception;
    //接口回应邀约
    Result visitReply(Map<String, Object> paramMap) throws Exception;
}
