package com.goldccm.service.visitor.impl;

import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.*;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.WebSocket.IWebSocketService;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.code.ICodeService;
import com.goldccm.service.companyUser.ICompanyUserService;
import com.goldccm.service.org.IOrgService;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.shortMessage.impl.ShortMessageServiceImpl;
import com.goldccm.service.user.IUserService;
import com.goldccm.service.visitor.IVisitPushService;
import com.goldccm.service.visitor.IVisitorRecordService;
import com.goldccm.util.Base64;
import com.goldccm.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
@Service("visitorRecordService")
public class VisitorRecordServiceImpl extends BaseServiceImpl implements IVisitorRecordService {
    Logger logger = LoggerFactory.getLogger(VisitorRecordServiceImpl.class);
    @Autowired
    public JdbcTemplate jdbcTemplate;

    @Autowired
    private ICodeService codeService;

    @Autowired
    private IUserService userService;
    @Autowired
    private IWebSocketService webSocketService;
    @Autowired
    private IBaseDao baseDao;

    @Autowired
    private IParamService paramService;
    @Autowired
    public ShortMessageServiceImpl shortMessageService;
    @Autowired
    public IOrgService orgService;
    @Autowired
    public ICompanyUserService companyUserService;

    @Autowired
    public IVisitPushService visitPushService;

    @Override
    public Result visitMyPeople(Map<String, Object> paramMap, Integer pageNum, Integer pageSize, Integer recordType) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if (userId == null) {
            return Result.unDataResult("fail", "缺少参数!");
        }
        Map<String, Object> user = this.findById(TableList.USER, userId);
        if (user.get("companyId") == null) {
            return Result.unDataResult("fail", "暂无公司数据!");
        }
        String columnSql = "select vr.*,IF(u.realName IS NULL or u.realName=\"\",remarkName,u.realName) realName,o.province province,o.city city,o.org_name org_name,c.companyName companyName";

        String fromSql = " from " + TableList.VISITOR_RECORD + " vr " + " left join " + TableList.USER
                + " u on vr.userId=u.id" + " left join " + TableList.COMPANY + " c on u.companyId=c.id" + " left join "
                + TableList.ORG + " o on c.orgId=o.id" + " where vr.visitorId = '" + userId + "' and recordType=" + recordType + "  " +
                "ORDER BY startDate>NOW() desc,  IF(startDate > NOW(), FIELD(cstatus,'Cancle','applyFail',  'applySuccess','applyConfirm'), startDate ) desc,startDate desc,endDate asc";
        logger.info(columnSql + fromSql);
        PageModel pageModel = this.findPage(columnSql, fromSql, pageNum, pageSize);
        //查看未过期记录的条数
        Map<String, Object> countMap = findFirstBySql("select count(*) num from " + TableList.VISITOR_RECORD + "  where visitorId = " + userId + " and cstatus='applyConfirm' and endDate>SYSDATE() and recordType=" + recordType + "  ");
        String count = BaseUtil.objToStr(countMap.get("num"), "0");
        return pageModel != null ? ResultData.dataResultCount("success", "获取成功", pageModel, count)
                : ResultData.dataResult("success", "暂无数据", new PageModel(pageNum, pageSize));
    }

    //查看访问我的公司的人
    @Override
    public Result visitMyCompany(Map<String, Object> paramMap, Integer pageNum, Integer pageSize) throws Exception {

        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if (userId == null) {
            return Result.unDataResult("fail", "缺少参数!");
        }
        Map<String, Object> user = this.findById(TableList.USER, userId);
        if (user.get("companyId") == null) {
            return Result.unDataResult("fail", "暂无公司数据!");
        }
        if (!"manage".equals(user.get("role"))) {
            return Result.unDataResult("fail", "非管理者无权查看!");
        }
        String columnSqlCompany = " select u.* ";
        String fromSqlCompany = "  from " + TableList.USER + " u " + " left join " + TableList.COMPANY_USER
                + " cu on u.companyId=cu.companyId" + " where u.companyId = '" + user.get("companyId") + "' and u.id!="
                + userId;
        List<Map<String, Object>> list = this.findList(columnSqlCompany, fromSqlCompany);
        String userUrl = "";
        if (list.size() < 1) {
            return Result.unDataResult("fail", "暂无同事数据!");
        }
        for (int i = 0; i < list.size(); i++) {
            userUrl = userUrl + list.get(i).get("id") + ",";
        }
        userUrl = userUrl.substring(0, userUrl.length() - 1);
        String columnSql = "select vr.*,u.realName userRealName,v.realName vistorRealName,o.province province,o.city city,o.org_name org_name,c.companyName companyName";
        String fromSql = " from " + TableList.VISITOR_RECORD + " vr " + " left join " + TableList.USER
                + " u on vr.userId=u.id" + " left join " + TableList.USER + " v on vr.visitorId=v.id" + " left join "
                + TableList.COMPANY + " c on vr.companyId=c.id" + " left join " + TableList.ORG + " o on c.orgId=o.id"
                + " where vr.visitorId in (" + userUrl
                + ") and vr.cstatus='applyConfirm' and vr.orgCode is not null and vr.companyId  = '" + user.get("companyId") + "' order by cstatus,visitDate desc,visitTime desc";
        PageModel pageModel = this.findPage(columnSql, fromSql, pageNum, pageSize);
        logger.info(columnSql + fromSql);
        //查看未过期记录的条数
        Map<String, Object> countMap = findFirstBySql("select count(*) num from " + TableList.VISITOR_RECORD + "  where visitorId in(" + userUrl + ") and cstatus='applyConfirm' and endDate>SYSDATE() and orgCode is not null and companyId  = '" + user.get("companyId") + "'");
        String count = BaseUtil.objToStr(countMap.get("num"), "0");
        System.out.println("select count(*) num from " + TableList.VISITOR_RECORD + "  where visitorId in(" + userUrl + ") and cstatus='applyConfirm' and endDate>SYSDATE() and orgCode is not null and companyId  = '" + user.get("companyId") + "'");
        return pageModel != null ? ResultData.dataResultCount("success", "获取成功", pageModel, count)
                : ResultData.dataResult("success", "暂无数据", new PageModel(pageNum, pageSize));
    }

    /**
     * 流程  帮助审核，公司管理员进行帮助审核
     *
     * @param paramMap
     * @return
     * @throws Exception
     * @author cwf
     * @date 2019/10/17 12:44
     */
//	@Override
//	public Result adoptionAndRejection(Map<String, Object> paramMap) throws Exception {
//		//审核人Id
//		Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
//		Integer id = BaseUtil.objToInteger(paramMap.get("id"), null);
//		String cstatus = BaseUtil.objToStr(paramMap.get("cstatus"), null);
//		String answerContent = BaseUtil.objToStr(paramMap.get("answerContent"), null);
//		if (id == null || StringUtils.isBlank(cstatus)) {
//			return Result.unDataResult("fail", "缺少参数!");
//		}
//		Map<String, Object> visitorRecord = findById(TableList.VISITOR_RECORD, id);
//		Integer companyId = BaseUtil.objToInteger(visitorRecord.get("companyId"), 0);
//		if (companyId == 0) {
//			return Result.unDataResult("fail", "登入人公司数据不全!");
//		}
//		Map<String, Object> orgComMap=new HashMap<>();
//		 //审核人所在公司ID，审核人所在公司角色
//		orgComMap= findFirstBySql("select org_code,org_name,accessType,companyName,c.addr,roleType from  "+TableList.ORG+" o " +
//				"left join "+TableList.COMPANY+" c on c.orgId=o.id left join "+TableList.COMPANY_USER+" cu on cu.companyId=c.id " +
//				" where c.id=" + companyId+" and userId="+userId);
//		String roleType = BaseUtil.objToStr(orgComMap.get("roleType"), null);
//		if (orgComMap == null||roleType == null) {
//			return Result.unDataResult("fail", "登入人公司数据不全!");
//		}
//		if (!"manage".equals(roleType)) {
//			return Result.unDataResult("fail", "你没有审核权限!");
//		}
//		// 判断数据
//		Map<String, Object> visitor = findById(TableList.VISITOR_RECORD, id);
//		if (!"applyConfirm".equals(visitor.get("cstatus"))) {
//			return Result.unDataResult("fail", "非申请中状态!");
//		}
//		// update by cwf  2019/10/25 16:13 Reason:修改审核接口
//		//访客信息
//		Map<String, Object> userUser = userService.getUserByUserId(Integer.parseInt(visitor.get("userId").toString()));
//		// 访客信息deviceToken
//		String deviceToken = BaseUtil.objToStr(userUser.get("deviceToken"),null);
//		String isOnlineApp = BaseUtil.objToStr(userUser.get("isOnlineApp"),"T");
//			//时间已过期
//			if (isMakePlanTime(visitor.get("endDate").toString())) {
//				Map<String, Object> visitorCancle = new HashMap<String, Object>();
//				visitorCancle.put("id", id);
//				visitorCancle.put("cstatus", "Cancle");
//				this.update(TableList.VISITOR_RECORD, visitorCancle);
//
//
//				if (deviceToken!=null&&!"".equals(deviceToken)) {
//                    String notification_title = "访客-访问过期提醒";
//                    String msg_content = "【朋悦比邻】您好，您有一条预约访客申请已过期无效，请重新发起!";
//                    String deviceType = BaseUtil.objToStr(userUser.get("deviceType"), "0");
//					shortMessageService.YMNotification(deviceToken,deviceType,notification_title,msg_content,isOnlineApp);
//                }
//				return Result.unDataResult("fail", "已经过了有效期，请通知您的客户重新预约!");
//			}
//			//访客记录
//		String visitorResult = "";
//			if ("applySuccess".equals(cstatus)) {
//			String dateType = BaseUtil.objToStr(paramMap.get("dateType"), null);
//			String startDate = BaseUtil.objToStr(paramMap.get("startDate"), null);
//			String endDate = BaseUtil.objToStr(paramMap.get("endDate"), null);
//			visitorRecord.put("cstatus", cstatus);
//			visitorRecord.put("dateType", dateType);
//			visitorRecord.put("startDate", startDate);
//			visitorRecord.put("endDate", endDate);
//			visitorResult = "接受访问";
//		} else if ("applyFail".equals(cstatus)) {
//			visitorRecord.put("cstatus", cstatus);
//			visitorRecord.put("answerContent", answerContent);
//			visitorResult = "拒绝访问";
//		}
//		String visitorDateTime = BaseUtil.objToStr(paramMap.get("startDate"), null);
//		// 被访者信息visitorBy
//		Map<String, Object> visitorUser = userService
//				.getUserByUserId(Integer.parseInt(visitor.get("visitorId").toString()));
//		String visitorBy = visitorUser.get("realName").toString();
//		// 访客信息phone
//		String phone = userUser.get("phone").toString();
//
//		visitorRecord.put("id", id);
//
//		Integer update = this.update(TableList.VISITOR_RECORD, visitorRecord);
//
//		if (update > 0) {
//			// 审核结果
//			String vitype=BaseUtil.objToStr(visitor.get("vitype"),"");
//			String notification_title = "访客-审核结果";
//			String msg_content = "【朋悦比邻】您好，您有一条预约访客申请已审核，审核结果：" + visitorResult + "，被访者:" + visitorBy + ",访问时间:"
//					+ visitorDateTime;
//			//增加根据type进行消息推送 “B”代表由网页发起访问 发送二维码地址 modifyTime:2019/9/20 9:53
//			String sid =Base64.encode((String.valueOf(id)).getBytes("UTF-8"));
//			if("B".equals(vitype)){
//				String sendMsg =	YunPainSmsUtil.sendSmsCode(Constant.URL+sid,phone,YunPainSmsUtil.MSG_TYPE_VISITORBY_QRCODE,null,null,
//						visitorResult,visitorBy,visitorDateTime,null);
//				return Result.unDataResult("success","审核成功");
//			}
//			//访客的微信号
//			String wxOpenId = BaseUtil.objToStr(userUser.get("wx_open_id"),null);
//			if (wxOpenId!=null&&!"".equals(wxOpenId)){
//				//审核结果发送给访问者微信公众号
//				if ("C".equals(vitype)){
//					Map<String,String> map=new HashMap<>();
//					map.put("wxOpenId",wxOpenId);
//					String orgName="无";
//					//进出方式
//					String accessType="1";
//					if (visitor.get("orgCode")!=null){
//						Map<String, Object> orgMap= findFirstBySql("select org_name,accessType from " + TableList.ORG + " where org_code='" + visitor.get("orgCode") + "'");
//						orgName =BaseUtil.objToStr(orgMap.get("org_name"),"无");
//						accessType =BaseUtil.objToStr(orgMap.get("accessType"),"0");
//					}
//					map.put("orgName",orgName);
//					map.put("companyName",BaseUtil.objToStr(company.get("companyName"),"0"));
//					map.put("companyFloor",BaseUtil.objToStr(company.get("companyFloor"),"0"));
//					map.put("startDate",visitorDateTime);
//					map.put("endDate",BaseUtil.objToStr(visitor.get("endDate"),"0"));
//					map.put("accessType",accessType);
//					map.put("visitorBy", visitorBy);
//					map.put("qrcodeUrl",Constant.URL+sid);
//					map.put("visitResult",visitorResult);
//					String s = HttpClientUtil.sendPost("http://localhost/weixin/wx/sendTempMsg", map, "application/x-www-form-urlencoded");
//					return Result.unDataResult("success","审核成功");
//				}
//
//			}
//			//友盟连接成功
//			boolean isYmSuc=false;
//			if (deviceToken!=null&&!"".equals(deviceToken)) {
//				String deviceType = BaseUtil.objToStr(userUser.get("deviceType"), "0");
//				isYmSuc=shortMessageService.YMNotification(deviceToken,deviceType,notification_title,msg_content,isOnlineApp);
//			}
//			// 消息推送不成功时，以短信的方式提醒用户
//			if (!isYmSuc) {
//				codeService.sendMsg(phone, 3, visitorResult, visitorBy, visitorDateTime, null);
//			}
//			return Result.unDataResult("success", "审核成功");
//		}
//		return Result.unDataResult("fail", "审核失败");
//	}
    //旧接口改造
    @Override
    public Result adoptionAndRejection(Map<String, Object> paramMap) throws Exception {
        //登入人的id=被访者id
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        Integer id = BaseUtil.objToInteger(paramMap.get("id"), null);
        String cstatus = BaseUtil.objToStr(paramMap.get("cstatus"), null);
        String answerContent = BaseUtil.objToStr(paramMap.get("answerContent"), null);

        if (id == null || StringUtils.isBlank(cstatus)) {
            return Result.unDataResult("fail", "缺少参数!");
        }
        Map<String, Object> user = this.findById(TableList.USER, userId);
        if (user == null || user.get("companyId") == null || user.get("role") == null) {
            return Result.unDataResult("fail", "数据不全!");
        }
        Map<String, Object> company = this.findById(TableList.COMPANY,
                Integer.parseInt(user.get("companyId").toString()));
        if (company.get("applyType").toString().equals("manage") && !("manage").equals(user.get("role").toString())) {
            return Result.unDataResult("fail", "你没有审核权限!");
        } else if (company.get("applyType").toString().equals("front") && ("staff").equals(user.get("role").toString())) {
            return Result.unDataResult("fail", "你没有审核权限!");
        }
        // 判断数据
        Map<String, Object> visitor = findById(TableList.VISITOR_RECORD, id);
        if (!"applyConfirm".equals(visitor.get("cstatus"))) {
            return Result.unDataResult("fail", "非申请中状态!");
        }
        //访客信息
        Map<String, Object> userUser = userService.getUserByUserId(Integer.parseInt(visitor.get("userId").toString()));
        // 访客信息deviceToken
        String deviceToken = BaseUtil.objToStr(userUser.get("deviceToken"), "");
//		String isOnlineApp = BaseUtil.objToStr(userUser.get("isOnlineApp"),"T");
        //时间已过期
        if (isMakePlanTime(visitor.get("endDate").toString())) {
            Map<String, Object> visitorCancle = new HashMap<String, Object>();
            visitorCancle.put("id", id);
            visitorCancle.put("cstatus", "Cancle");
            this.update(TableList.VISITOR_RECORD, visitorCancle);
//				if (deviceToken!=null&&!"".equals(deviceToken)) {
//                    String notification_title = "访客-访问过期提醒";
//                    String msg_content = "【朋悦比邻】您好，您有一条预约访客申请已过期无效，请重新发起!";
//                    String deviceType = BaseUtil.objToStr(userUser.get("deviceType"), "0");
//					shortMessageService.YMNotification(deviceToken,deviceType,notification_title,msg_content,isOnlineApp);
//                }
            return Result.unDataResult("fail", "已经过了有效期，请通知您的客户重新预约!");
        }
        //访客记录
        Map<String, Object> visitorRecord = new HashMap<String, Object>();
        visitorRecord.put("replyDate", DateUtil.getCurDate());
        visitorRecord.put("replyTime", DateUtil.getCurTime());

        String visitorResult = "";
        if ("applySuccess".equals(cstatus)) {
            String dateType = BaseUtil.objToStr(paramMap.get("dateType"), null);
            String startDate = BaseUtil.objToStr(paramMap.get("startDate"), null);
            String endDate = BaseUtil.objToStr(paramMap.get("endDate"), null);
            visitorRecord.put("cstatus", cstatus);
            visitorRecord.put("dateType", dateType);
            visitorRecord.put("startDate", startDate);
            visitorRecord.put("endDate", endDate);
            visitorResult = "接受访问";
        } else if ("applyFail".equals(cstatus)) {
            visitorRecord.put("cstatus", cstatus);
            visitorRecord.put("answerContent", answerContent);
            visitorResult = "拒绝访问";
        }
        String visitorDateTime = BaseUtil.objToStr(paramMap.get("startDate"), null);
        // 被访者信息visitorBy
        Map<String, Object> visitorUser = userService
                .getUserByUserId(Integer.parseInt(visitor.get("visitorId").toString()));
        String visitorBy = visitorUser.get("realName").toString();
        // 访客信息phone
        String phone = userUser.get("phone").toString();

        visitorRecord.put("id", id);

        Integer update = this.update(TableList.VISITOR_RECORD, visitorRecord);

        if (update > 0) {
            // 审核结果
            String vitype = BaseUtil.objToStr(visitor.get("vitype"), "");
            String notification_title = "访客-审核结果";
            String msg_content = "【朋悦比邻】您好，您有一条预约访客申请已审核，审核结果：" + visitorResult + "，被访者:" + visitorBy + ",访问时间:"
                    + visitorDateTime;
            //增加根据type进行消息推送 “B”代表由网页发起访问 发送二维码地址 modifyTime:2019/9/20 9:53
            String sid = Base64.encode((String.valueOf(id)).getBytes("UTF-8"));
            if ("B".equals(vitype)) {
                String sendMsg = YunPainSmsUtil.sendSmsCode(Constant.URL + sid, phone, YunPainSmsUtil.MSG_TYPE_VISITORBY_QRCODE, null, null,
                        visitorResult, visitorBy, visitorDateTime, null);
                return Result.unDataResult("success", "审核成功");
            }
            //访客的微信号
            String wxOpenId = BaseUtil.objToStr(userUser.get("wx_open_id"), null);
            if (wxOpenId != null && !"".equals(wxOpenId)) {
                //审核结果发送给访问者微信公众号
                if ("C".equals(vitype)) {
                    logger.info("访问type为C，返回微信");
                    Map<String, String> map = new HashMap<>();
                    map.put("wxOpenId", wxOpenId);
                    String orgName = "无";
                    //进出方式
                    String accessType = "1";
                    if (visitor.get("orgCode") != null) {
                        Map<String, Object> orgMap = findFirstBySql("select org_name,accessType from " + TableList.ORG + " where org_code='" + visitor.get("orgCode") + "'");
                        orgName = BaseUtil.objToStr(orgMap.get("org_name"), "无");
                        accessType = BaseUtil.objToStr(orgMap.get("accessType"), "0");
                        logger.info("进出方式：" + accessType);
                    }
                    map.put("orgName", orgName);
                    map.put("companyName", BaseUtil.objToStr(company.get("companyName"), "0"));
                    map.put("companyFloor", BaseUtil.objToStr(company.get("companyFloor"), "0"));
                    map.put("startDate", visitorDateTime);
                    map.put("endDate", BaseUtil.objToStr(visitor.get("endDate"), "0"));
                    map.put("accessType", accessType);
                    map.put("visitorBy", visitorBy);
                    map.put("qrcodeUrl", Constant.URL + sid);
                    map.put("visitResult", visitorResult);
                    String s = HttpClientUtil.sendPost(ParamDef.findDirByName("wxUrl"), map, "application/x-www-form-urlencoded");
                    return Result.unDataResult("success", "审核成功");
                }

            }
            //个推推送

//				String deviceType = BaseUtil.objToStr(userUser.get("deviceType"), "0");
            //退出登入
            String isOnlineApp = BaseUtil.objToStr(userUser.get("isOnlineApp"), "F");
            if ("F".equals(isOnlineApp)) {
                //短信推送
                codeService.sendMsg(phone, 3, visitorResult, visitorBy, visitorDateTime, null);
            } else {
                boolean single = GTNotification.Single(deviceToken, phone, notification_title, msg_content, msg_content);
//				isYmSuc=shortMessageService.YMNotification(deviceToken,deviceType,notification_title,msg_content,isOnlineApp);
                if (!single) {
                    codeService.sendMsg(phone, 3, visitorResult, visitorBy, visitorDateTime, null);
                }
            }
            // 消息推送不成功时，以短信的方式提醒用户

            return Result.unDataResult("success", "审核成功");
        }
        return Result.unDataResult("fail", "审核失败");
    }

    //判断当前时间是否大于输入时间
    public boolean isMakePlanTime(String time) {
        try {
            Calendar curr = Calendar.getInstance();
            //curr.add(Calendar.DATE, -1);
            Calendar start = Calendar.getInstance();
            start.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time));
            return curr.after(start);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Result peopleIInterviewed(Map<String, Object> paramMap, Integer pageNum, Integer pageSize) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if (userId == null) {
            return Result.unDataResult("fail", "缺少参数!");
        }
        String columnSql = "select vr.*,u.realName realName,o.province province,o.city city,o.org_name org_name,c.companyName companyName";
        String fromSql = " from " + TableList.VISITOR_RECORD + " vr " + " left join " + TableList.USER
                + " u on vr.visitorId=u.id" + " left join " + TableList.COMPANY + " c on vr.companyId=c.id"
                + " left join " + TableList.ORG + " o on c.orgId=o.id" + " where vr.userId = '" + userId
                + "'and substr(vr.startDate,1,10)>=SUBSTR(SYSDATE(),1,10) order by visitDate desc,visitTime desc,cstatus";
        PageModel pageModel = this.findPage(columnSql, fromSql, pageNum, pageSize);
        return pageModel != null ? ResultData.dataResult("success", "获取成功", pageModel)
                : ResultData.dataResult("success", "暂无数据", new PageModel(pageNum, pageSize));
    }

    @Override
    public Result peopleIInterviewedRecord(Map<String, Object> paramMap, Integer pageNum, Integer pageSize)
            throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        String cstatus = BaseUtil.objToStr(paramMap.get("cstatus"), null);
        if (userId == null || StringUtils.isBlank(cstatus)) {
            return Result.unDataResult("fail", "缺少参数!");
        }
        String columnSql = "select vr.*,u.realName realName,o.province province,o.city city,o.org_name org_name,c.companyName companyName";
        String fromSql = " from " + TableList.VISITOR_RECORD + " vr " + " left join " + TableList.USER
                + " u on vr.visitorId=u.id" + " left join " + TableList.COMPANY + " c on vr.companyId=c.id"
                + " left join " + TableList.ORG + " o on c.orgId=o.id" + " where vr.userId = '" + userId
                + "' and vr.cstatus = '" + cstatus + "' order by visitDate desc,visitTime desc";
        PageModel pageModel = this.findPage(columnSql, fromSql, pageNum, pageSize);
        return pageModel != null ? ResultData.dataResult("success", "获取成功", pageModel)
                : ResultData.dataResult("success", "暂无数据", new PageModel(pageNum, pageSize));
    }
//	//访客访问  已废弃
//	@Override
//	public Result visitRequest(Map<String, Object> paramMap) throws Exception {
//		Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
//		Integer visitorId = BaseUtil.objToInteger(paramMap.get("visitorId"), null);
//		Integer companyId = BaseUtil.objToInteger(paramMap.get("companyId"), null);
//		String reason = BaseUtil.objToStr(paramMap.get("reason"), null);
//		String dateType = BaseUtil.objToStr(paramMap.get("dateType"), null);
//		String startDate = BaseUtil.objToStr(paramMap.get("startDate"), null);
//		String endDate = BaseUtil.objToStr(paramMap.get("endDate"), null);
//		String orgCode = BaseUtil.objToStr(paramMap.get("orgCode"), null);
//		String cstatus = "applyConfirm";
//
//		Map<String, Object> save = new HashMap<String, Object>();
//		Date date = new Date();
//		save.put("visitDate", new SimpleDateFormat("yyyy-MM-dd").format(date));
//		save.put("visitTime", new SimpleDateFormat("HH:mm:ss").format(date));
//
//		if (visitorId != null) {
//			save.put("visitorId", visitorId);
//		}
//		if (userId != null) {
//			save.put("userId", userId);
//		}
//		Map<String, Object> repeat = findByRepeat(userId, visitorId,  cstatus);
//		if (repeat != null) {
//			if(repeat.containsKey("endDate")) {
//				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm");
//				Date de =new Date();
//				Date d1 = df.parse(df.format(de));
//				Date d2 = df.parse(repeat.get("endDate").toString());
//				if(d1.getTime()<=d2.getTime()) {
//					return Result.unDataResult("fail", "已经向此人提交过申请，请勿重复提交!");
//				}else {
//					Map<String, Object> record = new HashMap<String, Object>();
//					record.put("id", repeat.get("id"));
//					record.put("cstatus","Cancle");
//					Integer num=this.update(TableList.VISITOR_RECORD,record);
//					if(num<=0) {
//						return Result.unDataResult("fail", "上条访问申请未过期，请联系客服!");
//					}
//				}
//			}
//		}
//		if (!StringUtils.isBlank(reason)) {
//			save.put("reason", reason);
//		}
//		save.put("cstatus", cstatus);
//		if (!StringUtils.isBlank(dateType)) {
//			save.put("dateType", dateType);
//		}
//		if (!StringUtils.isBlank(startDate)) {
//			save.put("startDate", startDate);
//		}
//		if (!StringUtils.isBlank(endDate)) {
//			save.put("endDate", endDate);
//		}
//		if (companyId != null) {
//			save.put("companyId", companyId);
//		}
//		/*
//		 * Map<String,Object> user = findById(TableList.USER, visitorId);
//		 * Map<String,Object> org = findById(TableList.ORG,
//		 * Integer.parseInt(user.get("orgId").toString())); String orgCode =
//		 * org.get("org_code").toString();
//		 */
//		save.put("orgCode", orgCode);
//		save.put("recordType", "1");
//		// update by cwf  2019/11/25 17:50 Reason:vitype=A
//		save.put("vitype", "A");
//		Integer saveResult = this.save(TableList.VISITOR_RECORD, save);
//		if (saveResult > 0) {
//
//			String visitorDateTime = "";
//			if ("Indefinite".equals(dateType)) {
//				visitorDateTime = "无期限";
//			} else {
//				System.out.println("startDate:" + startDate);
//				visitorDateTime = startDate;
//			}
//			// 访客信息phone
//			Map<String, Object> userUser = userService.getUserByUserId(userId);
//			String visitor = BaseUtil.objToStr(userUser.get("realName"),null);
//
//			// 被访者信息visitorBy
//			Map<String, Object> visitorUser = userService.getUserByUserId(visitorId);
//			String visitorBy = BaseUtil.objToStr(visitorUser.get("realName"),null);
//			String phone = BaseUtil.objToStr(visitorUser.get("phone"),null);
//			String deviceToken = BaseUtil.objToStr(visitorUser.get("deviceToken"),null);
//			Map<String, Object> conpany = this.findById(TableList.COMPANY,
//					Integer.parseInt(visitorUser.get("companyId").toString()));
//			if (conpany.get("applyType").toString().equals("manage")
//					&& !("manage").equals(visitorUser.get("role").toString())) {
//				System.out.println("你没有审核权限!");
//			} else if (conpany.get("applyType").toString().equals("front")
//					&& ("staff").equals(visitorUser.get("role").toString())) {
//				System.out.println("你没有审核权限!");
//			} else {
//				// 被访者有管理权限
//				String notification_title = "访客-审核通知";
//				String msg_content = "【朋悦比邻】您好，您有一条预约访客需审核，访问者:" + visitor + "，被访者:" + visitorBy + ",访问时间:"
//						+ visitorDateTime;
//
//				boolean isYmSuc=false;
//				//友盟的token
//				if (deviceToken!=null&&!"".equals(deviceToken)) {
//					String deviceType = BaseUtil.objToStr(visitorUser.get("deviceType"), "0");
//					String isOnlineApp = BaseUtil.objToStr(visitorUser.get("isOnlineApp"),"T");
//					isYmSuc=shortMessageService.YMNotification(deviceToken,deviceType,notification_title,msg_content,isOnlineApp);
//				}// 被访者
//				if (!isYmSuc) {
//					codeService.sendMsg(phone, 5, null, null, visitorDateTime, visitor);
//				}
//				return Result.unDataResult("success", "发起请求成功");
//			}
////			// 被访者无管理权限查询出该公司的所有管理人员
//			 return  forwarding(visitor, visitorBy, BaseUtil.objToStr(visitorUser.get("companyId"), "0"), startDate);
//
//		}
//		return Result.unDataResult("fail", "发起请求失败");
//	}

    //	@Override
//	public Map<String, Object> findByRepeat(Integer userId, Integer visitorId, Integer companyId, String cstatus)
//			throws Exception {
//		String sql = " select * from " + TableList.VISITOR_RECORD + " where userId = '" + userId + "' and visitorId ='"
//				+ visitorId + "' and companyId = '" + companyId + "' and cstatus ='" + cstatus + "'";
//		return findFirstBySql(sql);
//	}
    @Override
    public Map<String, Object> findByRepeat(Integer userId, Integer visitorId, String cstatus)
            throws Exception {
        String sql = " select * from " + TableList.VISITOR_RECORD + " where userId = '" + userId + "' and visitorId ='"
                + visitorId + "' and cstatus ='" + cstatus + "'";
        return findFirstBySql(sql);
    }

    @Override
    public Result findOrgCode(String pospCode, String orgCode, Integer pageNum, Integer pageSize) throws Exception {
        if (pageNum != 1) {
            return Result.unDataResult("fail", "页数不对!");
        }
        // 判断上位机是否正常
        String orgSql = " select * from " + TableList.ORG + " where org_code = '" + orgCode + "'";

        Map<String, Object> org = findFirstBySql(orgSql);
        if (org == null) {
            logger.error("异常sql{}", orgSql);
            return Result.unDataResult("fail", "数据异常!");
        }
        String orgId = org.get("id").toString();
        String pospSql = " select * from " + TableList.POSP + " where orgId = '" + orgId + "' and pospCode ='"
                + pospCode + "' and cstatus='normal'";

        Map<String, Object> posp = findFirstBySql(pospSql);
        if (posp == null) {
            logger.error("无此上位机编码或者无此大楼编码: sql{}", pospSql);
            return Result.unDataResult("fail", "无此上位机编码" + pospCode + "或者无此大楼编码" + orgCode);
        }
//		System.out.println(posp.get("id"));
        String maxIdSql = " select * from " + TableList.VISITOR_RECORD_MAXID + " where pospId = '" + posp.get("id")
                + "'";

        Map<String, Object> maxId = findFirstBySql(maxIdSql);
        PageModel pageModel;
        Date date = new Date();
        String today = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
//		System.out.println("maxID=:"+BaseUtil.objToStr(maxId.get("maxVisitorId"),null));
        if (maxId == null || maxId.isEmpty() || maxId.get("maxVisitorId") == null) {
//			System.out.println("111");
            // 增量问题
            String columnSql = "select vr.id visitId,vr.userId,vr.visitDate,vr.visitTime,vr.orgCode,vr.dateType,vr.startDate,vr.endDate,u.realName userRealName,u.idType userIdType,u.idNO userIdNO,u.soleCode soleCode,u.idHandleImgUrl idHandleImgUrl,c.companyFloor companyFloor,v.realName vistorRealName,v.idType vistorIdType,v.idNO visitorIdNO,o.province province,o.city city";
            String fromSql = " from " + TableList.VISITOR_RECORD + " vr " + " left join " + TableList.USER
                    + " v on vr.visitorId=v.id" + " left join " + TableList.USER + " u on vr.userId=u.id" + " left join " + TableList.COMPANY + " c on vr.companyId=c.id"
                    + " left join " + TableList.ORG + " o on v.orgId=o.id"
                    + " where vr.cstatus='applySuccess' and vr.orgCode = '" + orgCode + "'"
                    + " and (vr.dateType='Indefinite' or (vr.dateType='limitPeriod' and " + "vr.startDate<= '" + today
                    + "' and vr.endDate>='" + today + "')) order by vr.id";
            pageModel = this.findPage(columnSql, fromSql, pageNum, pageSize);
            System.out.println(columnSql+fromSql);
            if (pageModel.getRows() != null && !pageModel.getRows().isEmpty()) {
//				System.out.println("22222");
                String sql = " select vr.*  from " + TableList.VISITOR_RECORD
                        + " vr where vr.cstatus='applySuccess' and vr.orgCode = '" + orgCode
                        + "' and (vr.dateType='Indefinite' or (vr.dateType='limitPeriod' and vr.startDate<= '" + today
                        + "' and vr.endDate>='" + today + "')) order by vr.id limit " + (pageNum - 1) * pageSize + ", "
                        + pageSize;
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
                List<Map<String, Object>> maps = pageModel.getRows();
                for (int i = 0; i < maps.size(); i++) {
                    Map<String, Object> map = maps.get(i);
                    String idHandleImgUrl = (String) map.get("idHandleImgUrl");
                    if (idHandleImgUrl != null && idHandleImgUrl.length() != 0) {
                        String imageServerUrl = paramService.findValueByName("imageServerUrl");
                        String photo = Base64.encode(FilesUtils.getImageFromNetByUrl(imageServerUrl + idHandleImgUrl));
                        maps.get(i).put("photo", photo);
                    }
                }
                if (maxId != null) {
                    /*
                     * String sql = " select vr.*  from "
                     * +TableList.VISITOR_RECORD +
                     * " vr where vr.cstatus='applySuccess' and vr.orgCode = '"
                     * +orgCode+
                     * "' and (vr.dateType='Indefinite' or (vr.dateType='limitPeriod' and vr.startDate<= '"
                     * +today+"' and vr.endDate>='"+today+
                     * "')) order by vr.id limit "+(pageNum - 1)*pageSize + ", "
                     * +pageSize; List<Map<String,Object>> list =
                     * jdbcTemplate.queryForList(sql);
                     */
                    if (list.size() > 0) {
                        Map<String, Object> maxVisitorId = list.get(list.size() - 1);
                        Map<String, Object> maxVisitor = new HashMap<String, Object>();
                        maxVisitor.put("id", maxId.get("id"));
                        maxVisitor.put("tempId", maxVisitorId.get("id"));
                        this.update(TableList.VISITOR_RECORD_MAXID, maxVisitor);
                    }
                } else {
                    // Integer maxVisitorId = (Integer)
                    // baseDao.queryForObject("select max(id) from
                    // "+TableList.VISITOR_RECORD + " vr where
                    // vr.cstatus='applySuccess' and vr.orgCode = '"+orgCode+"'
                    // and (vr.dateType='Indefinite' or
                    // (vr.dateType='limitPeriod' and vr.startDate<= '"+today+"'
                    // and vr.endDate>='"+today+"')) order by
                    // vr.visitDate,vr.visitTime "+" limit "+(pageNum -
                    // 1)*pageSize + ", "+pageSize ,Integer.class);
                    /*
                     * String sql = " select vr.*  from "
                     * +TableList.VISITOR_RECORD +
                     * " vr where vr.cstatus='applySuccess' and vr.orgCode = '"
                     * +orgCode+
                     * "' and (vr.dateType='Indefinite' or (vr.dateType='limitPeriod' and vr.startDate<= '"
                     * +today+"' and vr.endDate>='"+today+
                     * "')) order by vr.id limit "+(pageNum - 1)*pageSize + ", "
                     * +pageSize; List<Map<String,Object>> list =
                     * jdbcTemplate.queryForList(sql);
                     */
                    if (list.size() > 0) {
//						System.out.println("33333");
                        Map<String, Object> maxVisitorId = list.get(list.size() - 1);
                        Map<String, Object> maxVisitor = new HashMap<String, Object>();
                        maxVisitor.put("pospId", posp.get("id"));
                        maxVisitor.put("tempId", maxVisitorId.get("id"));
                        this.save(TableList.VISITOR_RECORD_MAXID, maxVisitor);
                    }
                }
            }
        } else {
//			System.out.println("4444");
            String columnSql = "select vr.id visitId,vr.userId,vr.visitDate,vr.visitTime,vr.orgCode,vr.dateType,vr.startDate,vr.endDate,u.realName userRealName,u.idType userIdType,u.idNO userIdNO,u.soleCode soleCode,u.idHandleImgUrl idHandleImgUrl,c.companyFloor companyFloor,v.realName vistorRealName,v.idType vistorIdType,v.idNO visitorIdNO,o.province province,o.city city";
            String fromSql = " from " + TableList.VISITOR_RECORD + " vr " + " left join " + TableList.USER
                    + " v on vr.visitorId=v.id" + " left join " + TableList.USER + " u on vr.userId=u.id" + " left join " + TableList.COMPANY + " c on vr.companyId=c.id"
                    + " left join " + TableList.ORG + " o on v.orgId=o.id" + " where vr.id>" + maxId.get("maxVisitorId")
                    + " and vr.cstatus='applySuccess' and vr.orgCode = '" + orgCode
                    + "' and (vr.dateType='Indefinite' or (vr.dateType='limitPeriod' and vr.startDate<= '" + today
                    + "' and vr.endDate>='" + today + "')) order by vr.id";
//			System.out.println(columnSql+fromSql);
            pageModel = this.findPage(columnSql, fromSql, pageNum, pageSize);
            if (pageModel.getRows() != null && !pageModel.getRows().isEmpty()) {
//				System.out.println("55555");
                String sql = " select vr.*  from " + TableList.VISITOR_RECORD + " vr where vr.id>"
                        + maxId.get("maxVisitorId") + " and vr.cstatus='applySuccess' and vr.orgCode = '" + orgCode
                        + "' and (vr.dateType='Indefinite' or (vr.dateType='limitPeriod' and vr.startDate<= '" + today
                        + "' and vr.endDate>='" + today + "')) order by vr.id limit " + (pageNum - 1) * pageSize + ", "
                        + pageSize;
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
                List<Map<String, Object>> maps = pageModel.getRows();
                for (int i = 0; i < maps.size(); i++) {
                    Map<String, Object> map = maps.get(i);
                    String idHandleImgUrl = (String) map.get("idHandleImgUrl");
                    if (idHandleImgUrl != null && idHandleImgUrl.length() != 0) {
                        String imageServerUrl = paramService.findValueByName("imageServerUrl");

                        String photo = Base64.encode(FilesUtils.getImageFromNetByUrl(imageServerUrl + idHandleImgUrl));
                        maps.get(i).put("photo", photo);
                    }
                }
                if (list.size() > 0) {
                    Map<String, Object> maxVisitorId = list.get(list.size() - 1);
                    Map<String, Object> maxVisitor = new HashMap<String, Object>();
                    maxVisitor.put("tempId", maxVisitorId.get("id"));
                    maxVisitor.put("id", maxId.get("id"));
                    this.update(TableList.VISITOR_RECORD_MAXID, maxVisitor);
                }
            }
        }
        return pageModel.getRows() != null && !pageModel.getRows().isEmpty()
                ? ResultData.dataResult("success", "获取授权访问信息成功", pageModel)
                : ResultData.dataResult("success", "暂无数据", new PageModel(pageNum, pageSize));
    }

    @Override
    public Result findBySoleCode(String pospCode, String orgCode, String soleCode, String visitId) throws Exception {
        if (StringUtils.isBlank(visitId)) {
            return Result.unDataResult("fail", "缺少数据!");
        }
        // 判断上位机是否正常
        String orgSql = " select * from " + TableList.ORG + " where org_code = '" + orgCode + "'";
        Map<String, Object> org = findFirstBySql(orgSql);
        if (org == null) {
            return Result.unDataResult("fail", "数据异常!");
        }
        String orgId = org.get("id").toString();
        String pospSql = " select * from " + TableList.POSP + " where orgId = '" + orgId + "' and pospCode ='"
                + pospCode + "' and cstatus='normal'";
        Map<String, Object> posp = findFirstBySql(pospSql);
        if (posp == null) {
            return Result.unDataResult("fail", "无此上位机编码" + pospCode + "或者无此大楼编码" + orgCode);
        }
        Date date = new Date();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(date);
        String coloumSql = "select vr.id visitId,vr.userId,vr.visitDate,vr.visitTime,vr.orgCode,vr.dateType,vr.startDate,vr.endDate,u.realName userRealName,u.soleCode soleCode,v.realName vistorRealName,o.province province,o.city city";
        String fromSql = " from " + TableList.VISITOR_RECORD + " vr " + " left join " + TableList.USER
                + " v on vr.visitorId=v.id" + " left join " + TableList.USER + " u on vr.userId=u.id" + " left join "
                + TableList.ORG + " o on v.orgId=o.id" + " where vr.cstatus='applySuccess' and vr.orgCode = '" + orgCode
                + "' and u.soleCode='" + soleCode + "' and vr.id =" + visitId
                + " and (vr.dateType='Indefinite' or (vr.dateType='limitPeriod' and vr.startDate<= '" + today
                + "' and vr.endDate>='" + today + "'))  order by vr.visitDate,vr.visitTime";
        System.out.println(coloumSql + " " + fromSql);
        List<Map<String, Object>> list = this.findList(coloumSql, fromSql);
        if (list.size() < 1) {
            return Result.unDataResult("success", "二维码以失效！");
        }
        return ResultData.dataResult("success", "获取单个授权访问信息", list.get(0));
    }

    @Override
    public Result findOrgCodeConfirm(String pospCode, String orgCode, String isData) throws Exception {
        // 判断上位机是否正常
        String orgSql = " select * from " + TableList.ORG + " where org_code = '" + orgCode + "'";
        Map<String, Object> org = findFirstBySql(orgSql);
        if (org == null) {
            return Result.unDataResult("fail", "数据异常!");
        }
        String orgId = org.get("id").toString();
        String pospSql = " select * from " + TableList.POSP + " where orgId = '" + orgId + "' and pospCode ='"
                + pospCode + "' and cstatus='normal'";
        Map<String, Object> posp = findFirstBySql(pospSql);
        if (posp == null) {
            return Result.unDataResult("fail", "无此上位机编码" + pospCode + "或者无此大楼编码" + orgCode);
        }
        if ("T".equals(isData)) {
            String maxIdSql = " select * from " + TableList.VISITOR_RECORD_MAXID + " where pospId = '" + posp.get("id")
                    + "'";
            Map<String, Object> maxId = findFirstBySql(maxIdSql);
            Map<String, Object> maxVisitor = new HashMap<String, Object>();
            maxVisitor.put("maxVisitorId", maxId.get("tempId"));
            maxVisitor.put("id", maxId.get("id"));
            this.update(TableList.VISITOR_RECORD_MAXID, maxVisitor);
        }
        return Result.unDataResult("success", "数据已接收!");
    }

    @Override
    public Result uploadAccessRecord(String pospCode, String orgCode, String visitId, String inOrOut, String visitDate,
                                     String visitTime) throws Exception {

        Map<String, Object> save = new HashMap<String, Object>();
        save.put("pospCode", pospCode);
        save.put("orgCode", orgCode);
        save.put("visitId", Integer.valueOf(visitId));
        save.put("inOrOut", inOrOut);
        save.put("visitDate", visitDate);
        save.put("visitTime", visitTime);
        Integer saveResult = this.save(TableList.VISITOR_ACCESS_RECORD, save);

        if (saveResult > 0) {
            return Result.unDataResult("success", "数据发送成功!");
        } else {
            return Result.unDataResult("fail", "数据发送失败!");
        }
    }

    /**
     * 发送短信邀约
     *
     * @author chenwf
     * @date 2019/8/21 17:03
     * update by cwf  2019/8/21 17:21
     */
    @Override
    public Result sendShortMessage(Map<String, Object> paramMap) throws Exception {

        //邀约人姓名
        String name = BaseUtil.objToStr(paramMap.get("name"), "");
        //登入人ID=visitorId
        String loginId = BaseUtil.objToStr(paramMap.get("loginId"), "0");
        //电话
        String phone = BaseUtil.objToStr(paramMap.get("phone"), "");
        Map<String, Object> user = userService.FindFriendByPhoneAndRealName(phone, name);
        //访问者id
        long userId = BaseUtil.objToLong(user.get("id"), null);
        if (user == null) {
            return Result.unDataResult("fail", "用户姓名与手机不匹配!");
        }
        //备注
        String desc = BaseUtil.objToStr(paramMap.get("desc"), "");
        //邀约时间
        String startTime = BaseUtil.objToStr(paramMap.get("startTime"), "");
        //结束时间
        String endTime = BaseUtil.objToStr(paramMap.get("endTime"), "");
        //开始时间与结束时间必须大于当前时间
//		if(startTime<DateUtil.getSystemTime())
        //公司id
        Integer companyId = BaseUtil.objToInteger(paramMap.get("companyId"), null);
        //登入人姓名
        String loginName = BaseUtil.objToStr(paramMap.get("loginName"), "");
        //获取公司名称地点
        String companySql = "select c.addr,c.name,o.org_code from " + TableList.COMPANY + " c" +
                " left join " + TableList.ORG + " o on c.orgId=o.id " +
                "where c.id=" + companyId;
        Map<String, Object> company = findFirstBySql(companySql);
        //判断数据库中是否有邀约信息
//        String recordSql = "select * from " + TableList.VISITOR_RECORD +
//                " where userId=" + userId + " and " +
//                "visitorId= " + loginId + " and startDate='" + startTime + "' and endDate='" + endTime + "'";
//        System.out.println(recordSql);
//        Map<String, Object> record = findFirstBySql(recordSql);
//        if (record != null) {
//            if ("T".equals(record.get("isReceive"))) {
//                return Result.unDataResult("fail", "已存在的邀约申请！请勿重复提交！");
//            }
//        }
        Map<String, Object> saveMap = new HashMap<>();
        saveMap.put("userId", userId);
        saveMap.put("visitorId", loginId);
        saveMap.put("visitDate", DateUtil.getCurDate());
        saveMap.put("visitTime", DateUtil.getCurTime());
        saveMap.put("reason", desc);
        saveMap.put("cstatus", "applyConfirm");
        saveMap.put("dateType", "limitPeriod");
        saveMap.put("startDate", startTime);
        saveMap.put("endDate", endTime);
        saveMap.put("vitype", "B");
        saveMap.put("recordType", Constant.RECORDTYPE_INVITE);
        saveMap.put("orgCode", BaseUtil.objToStr(company.get("org_code"), ""));
        int saveId = save(TableList.VISITOR_RECORD, saveMap);
//		后台生成二维码
//		String qrCodeUrl = visitDes(saveId,String.valueOf(userId));
        String encode = Base64.encode(String.valueOf(saveId).getBytes("UTF-8"));
        //发送短信
        String sendMsg = YunPainSmsUtil.sendSmsCode(Constant.INVITE_URL + encode, phone, 6, (String) company.get("addr"), (String) company.get("name"), endTime, loginName, startTime, name);

        return Result.unDataResult("success", sendMsg);
    }

//	public String visitDes(Integer id,String userId) throws Exception{
//		String key = "ASDFGHJK";
//		String str =String.valueOf(id);
//		String url = Constant.URL +"id="+ DESUtil.encode(key,str);
//		System.out.println(url);
//		String destPath = "qrcodeimgs";
//		String jpgName=userId+DateUtil.getSystemTimeFourteen()+".jpg";
//		QRCodeUtil.encode(url, null, destPath+jpgName, true);
//		return destPath+jpgName;
//	}

    /**
     * update by cwf  2019/8/22 11:22
     */
    @Override
    public Result dealQrcodeUrl(Map<String, Object> paramMap) throws Exception {
        String id = BaseUtil.objToStr(paramMap.get("id"), "");
        String cstatus = BaseUtil.objToStr(paramMap.get("cstatus"), "");
        if (cstatus.equals("1")) {
            cstatus = "applySuccess";
        } else {
            cstatus = "applyFail";
        }
        String desId = new String(Base64.decode(id), "UTF-8");
        String findSql = "select * from " + TableList.VISITOR_RECORD + " where id= " + desId + " and" +
                " cstatus  in ('applySuccess','applyFail')";
        Map<String, Object> record = findFirstBySql(findSql);
        if (record != null) {
            if ("applySuccess".equals(record.get("cstatus"))) {
                return Result.unDataResult("success", Constant.URL + id);
            } else if ("applyFail".equals(record.get("cstatus"))) {
                return Result.unDataResult("fail", "已拒绝邀请");
            }
        }
        String sql = "update " + TableList.VISITOR_RECORD + " set cstatus='" + cstatus + "'" +
                " where id=" + desId;

        int update = deleteOrUpdate(sql);
        if (update > 0) {
            return Result.unDataResult("success", Constant.URL + id);
        }
        return Result.fail();

    }

    /**
     * 两种访问方式 1.知道对方姓名与手机号码 2、知道对方id
     * 非好友访问+好友访问整合
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    @Override
    public Result visit(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        Integer visitorId = BaseUtil.objToInteger(paramMap.get("visitorId"), null);
        String phone = BaseUtil.objToStr(paramMap.get("phone"), "");
        String realName = BaseUtil.objToStr(paramMap.get("realName"), "");
        String startDate = BaseUtil.objToStr(paramMap.get("startDate"), "");
        String endDate = BaseUtil.objToStr(paramMap.get("endDate"), "");
        String reason = BaseUtil.objToStr(paramMap.get("reason"), "");
        // update by cwf  2020-08-19 15:30 Reason:访问邀约新增随行人员以及车辆的接口
        String userIds= BaseUtil.objToStr(paramMap.get("userIds"), null);
        String carNumber= BaseUtil.objToStr(paramMap.get("carNumber"), null);
        String sql;
        if (userId == null) {
            return Result.unDataResult(ConsantCode.FAIL, "缺少用户参数!");
        }
        if (visitorId == null) {//进入手机姓名模式
            if ("".equals(phone) || "".equals(realName)) {
                return Result.unDataResult(ConsantCode.FAIL, "请输入手机号与姓名!");
            }
            if (userService.checkPhone(phone) == null) {
                return Result.unDataResult(ConsantCode.FAIL, "手机号不存在!");
            }
            sql = "select id,companyId,realName,isAuth,deviceToken,deviceType,isOnlineApp,wx_open_id,phone from " + TableList.USER + " " +
                    "where phone='" + phone + "' and realName='" + realName + "'";
            logger.info("被访者sql：{}", sql);
        } else {//进入id模式

            sql = "select id,companyId,realName,isAuth,deviceToken,deviceType,isOnlineApp,wx_open_id,phone from " + TableList.USER + " " +
                    "where id=" + visitorId;
            logger.info("被访者sql：{}", sql);
        }
        //被访者
        Map<String, Object> visitor = findFirstBySql(sql);
        if (visitor == null) {
            return Result.unDataResult("fail", "用户不存在!");
        }
        //查看访客是否实名
        //被访者id
        visitorId = BaseUtil.objToInteger(visitor.get("id"), null);
        String isAuth = BaseUtil.objToStr(visitor.get("isAuth"), "");
        if (userId.equals(visitorId)) {
            return Result.unDataResult("fail", "请不要对自己发起访问！");
        }
        Integer companyId = BaseUtil.objToInteger(visitor.get("companyId"), null);
        if (!"T".equals(isAuth)) {
            logger.info("被访者未实名！:{}", visitorId);
            return Result.unDataResult("fail", "受访人未实名！");
        }
        if (companyId == null) {
            sql = "select 1 from " + TableList.COMPANY_USER + " where userId=" + visitorId + " and currentStatus" +
                    "='normal' and status='applySuc'";
            Map<String, Object> existUser = findFirstBySql(sql);
            if (existUser == null) {
                return Result.unDataResult("fail", "受访人无归属公司！");
            }
        }
        sql = "select id,realName,phone,idHandleImgUrl,headImgUrl from " + TableList.USER + " where id =" + userId;
        logger.info("访者sql：{}", sql);
        Map<String, Object> user = findFirstBySql(sql);
        //如果是访问recordType=1
        //查询内部是否有邀约信息
//        Map<String, Object> check = check(userId, visitorId, 1, startDate, endDate);
//        //如果是邀约recordType=2 访客与被访者在数据库中位置调换
//        if (check != null) {
//            //发送回消息
//            logger.info(startDate + "该时间段" + endDate + "内已存在访问信息");
//            return Result.unDataResult("fail", "在" + startDate + "——" + endDate + "内已存在访问信息");
//        }
        Map<String, Object> visitRecord = new HashMap<>();
        visitRecord.put("userId", userId);
        visitRecord.put("visitorId", visitorId);
        visitRecord.put("cstatus", "applyConfirm");
        visitRecord.put("visitDate", DateUtil.getCurDate());
        visitRecord.put("visitTime", DateUtil.getCurTime());
        visitRecord.put("reason", reason);
        visitRecord.put("startDate", startDate);
        visitRecord.put("endDate", endDate);
        visitRecord.put("vitype", "F");
        visitRecord.put("recordType", 1);
        //todo 新增车牌号
        if (carNumber!=null&&!"".equals(carNumber)){
            visitRecord.put("carNumber",carNumber);
        }
        //记录访问记录
        //todo 访问邀约新增随行人员以及车辆
        int saveVisitRecord = save(TableList.VISITOR_RECORD, visitRecord);
        if (saveVisitRecord > 0) {
            visitRecord.put("id",saveVisitRecord);

            for (Map.Entry<String, Object> entry : visitRecord.entrySet()) {
                if (entry.getValue() == null) {
                    visitRecord.put(entry.getKey(), "无");
                }
            }
            if (userIds!=null){
                StringBuffer prefixSql = new StringBuffer("insert into " + TableList.VISITOR_RECORD + "(userId,visitorId,cstatus,visitDate,visitTime,reason,startDate,endDate,vitype,recordType,pid) values");
                StringBuffer suffixSql = new StringBuffer();
                String[] split = userIds.split(",");
                for (String s : split) {
                    suffixSql.append("(" + s + "," + visitorId + ","+"applyConfirm,"+DateUtil.getCurDate()+","+DateUtil.getCurTime()+","+reason+","+startDate+","+endDate+",'F',"+"1,"+saveVisitRecord+"),");
                }
                //todo 批量生成访问记录
                int[] ints = baseDao.batchUpdate(prefixSql + suffixSql.substring(0, suffixSql.length() - 1));
            }
            //访问
            visitPushService.visitPush(startDate,user,visitor,visitRecord,5);
            return Result.unDataResult("success", "访问成功");
        } else {
            return Result.unDataResult("fail", "访问失败");
        }

    }
    /**
     * 回应访问接口整合
     *
     * @param paramMap
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/10/24 9:38
     */
    //被访者修改公司并同意拒绝，判断权限
    @Override
    public Result replay(Map<String, Object> paramMap) throws Exception {
        Integer id = BaseUtil.objToInteger(paramMap.get("id"), 0);
        Integer visitorId = BaseUtil.objToInteger(paramMap.get("userId"), 0);//登入人id=记录表中的visitorId
        Integer companyId = BaseUtil.objToInteger(paramMap.get("companyId"), 0);
        String cstatus = BaseUtil.objToStr(paramMap.get("cstatus"), "applyConfirm");
        String answerContent = BaseUtil.objToStr(paramMap.get("answerContent"), "无");
        if (id == 0) {
            return Result.unDataResult("fail", "缺少参数!");
        }
        Map<String, Object> visitorRecord = findById(TableList.VISITOR_RECORD, id);

        String startDate = BaseUtil.objToStr(visitorRecord.get("startDate"), "");
        String reason = BaseUtil.objToStr(visitorRecord.get("reason"), "");
//        String endDate = BaseUtil.objToStr(visitorRecord.get("endDate"), "");
        Integer userId = BaseUtil.objToInteger(visitorRecord.get("userId"), 0);//记录表中的userId
        if (visitorId.equals(userId)) {
            return Result.unDataResult("fail", "访问者与被访者不能为同一人!");
        }

        //访客记录
        if (!"applyConfirm".equals(visitorRecord.get("cstatus"))) {
            return Result.unDataResult("fail", "非申请中状态!");
        }
        visitorRecord.put("answerContent", answerContent);
        visitorRecord.put("cstatus", cstatus);
        visitorRecord.put("replyDate", DateUtil.getCurDate());
        visitorRecord.put("replyTime", DateUtil.getCurTime());
        visitorRecord.put("replyUserId", visitorId);
        visitorRecord.put("companyId", companyId);
        //访客Id 访问者
        //访客信息
        Map<String, Object> visitor = userService.getUserByUserId(visitorId);


        Map<String, Object> user = findById(TableList.USER, userId);//访客

        int update;
        Long longUserId = Long.valueOf(userId);
        boolean isUserOnline = Constant.SESSIONS.containsKey(longUserId);
        if (isUserOnline) {
            visitorRecord.put("isReceive", "T");
        }
        //更新
        if ("applyFail".equals(cstatus)) {
            update = update(TableList.VISITOR_RECORD, visitorRecord);
        } else {
            Map<String, Object> orgComMap = new HashMap<>();
            if (companyId != 0) {
                orgComMap = findFirstBySql("select org_code,org_name,accessType,companyName,c.addr,roleType from  " + TableList.ORG + " o " +
                        "left join " + TableList.COMPANY + " c on c.orgId=o.id left join " + TableList.COMPANY_USER + " cu on cu.companyId=c.id " +
                        " where c.id=" + companyId + " and userId=" + visitorId + " and cu.currentStatus ='normal'  and cu.status ='applySuc' ");
                logger.info("select org_code,org_name,accessType,companyName,c.addr,roleType from  " + TableList.ORG + " o " +
                        "left join " + TableList.COMPANY + " c on c.orgId=o.id left join " + TableList.COMPANY_USER + " cu on cu.companyId=c.id " +
                        " where c.id=" + companyId + " and userId=" + visitorId + " and cu.`currentStatus` ='normal' and cu.status ='applySuc' ");
            }
            if (orgComMap == null || orgComMap.isEmpty()) {
                return Result.unDataResult("fail", "用户不存在该公司");
            }
            String orgCode = BaseUtil.objToStr(orgComMap.get("org_code"), "无");
//                String orgName = BaseUtil.objToStr(orgComMap.get("org_name"), "无");
//                String companyName = BaseUtil.objToStr(orgComMap.get("companyName"), "无");
//                String addr = BaseUtil.objToStr(orgComMap.get("addr"), "无");
//                String companyFloor = BaseUtil.objToStr(orgComMap.get("companyFloor"), "无");
////            String roleType = BaseUtil.objToStr(orgComMap.get("roleType"), "无");
//                String accessType = BaseUtil.objToStr(orgComMap.get("accessType"), "0");
//            logger.info("accessType=" + accessType);
            visitorRecord.put("orgCode", orgCode);
            visitorRecord.put("isCompanyFlag","F");
            update = update(TableList.VISITOR_RECORD, visitorRecord);
        }
        if (update > 0) {
            JSONObject msg = new JSONObject();
            msg.put("type", Constant.MASSEGETYPE_REPLY);
            for (Map.Entry<String, Object> entry : visitorRecord.entrySet()) {
                if (entry.getValue() == null) {
                    visitorRecord.put(entry.getKey(), "无");
                }
                msg.put(entry.getKey(), entry.getValue());
            }
            visitPushService.visitPush(startDate,visitor,user,visitorRecord,3);
            return Result.unDataResult("success", "操作成功");
        }
        return Result.unDataResult("success", "操作失败");
    }
    /**
     * 非好友邀约
     * 判断对方是否为平台用户，如果是，则发送普通邀约，如果不是则发送短信
     * 目前只发送短信，不进行判断用户存在
     *
     * @author cwf
     * @date 2019/12/17 11:33
     */
    @Override
    public Result inviteStranger(Map<String, Object> paramMap) throws Exception {
        //邀请者id
        Integer visitorId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        Integer userId = BaseUtil.objToInteger(paramMap.get("visitorId"), null);//对方id 记录中的userId

        String phone = BaseUtil.objToStr(paramMap.get("phone"), "");
        String realName = BaseUtil.objToStr(paramMap.get("realName"), "");
        String startDate = BaseUtil.objToStr(paramMap.get("startDate"), "");
        String endDate = BaseUtil.objToStr(paramMap.get("endDate"), "");
        String reason = BaseUtil.objToStr(paramMap.get("reason"), "");
        //我的公司id
        Integer companyId = BaseUtil.objToInteger(paramMap.get("companyId"), 0);
        Map<String, Object> user;
        if (userId==null){
            if ("".equals(phone) || "".equals(realName)){
                return Result.unDataResult(ConsantCode.FAIL, "缺少用户参数!");
            }else {
                user = userService.getUserByPhone(phone);
                if (user == null) { //如果用户不存在
                    //生成新用户isReceive
                    userService.createUser(phone, realName);
                    String sql = "select id,companyId,realName,isAuth,deviceToken,deviceType,isOnlineApp,token,idHandleImgUrl,headImgUrl,wx_open_id from " + TableList.USER + " " +
                            "where phone='" + phone + "' and realName='"+realName+"' limit 1";
                    user=findFirstBySql(sql);
                }
            }
        }else {
            user=findById(TableList.USER,userId);
        }
        //被邀者==访问者
        String userName = BaseUtil.objToStr(user.get("realName"),"");
        String token = BaseUtil.objToStr(user.get("token"),"");
        //被邀者==访问者id
        userId = BaseUtil.objToInteger(user.get("id"), 0);
        if (userId.equals(visitorId)) {
            return Result.unDataResult("fail", "请不要对自己发起邀约！");
        }
        // 查询登入者姓名
        Map<String, Object> visitor = findById(TableList.USER,visitorId);
        String visitorName = BaseUtil.objToStr(visitor.get("realName"), "");

        //查询登入者org
        String sql = "select o.org_name,o.org_code,c.companyName from " + TableList.ORG + " o left join " + TableList.COMPANY + " c" +
                " on o.id=c.orgId where c.id='" + companyId + "'";
        Map<String, Object> orgMap = findFirstBySql(sql);
        String orgCode = BaseUtil.objToStr(orgMap.get("org_code"), "无");
        String orgName = BaseUtil.objToStr(orgMap.get("org_name"), "无");
        String companyName = BaseUtil.objToStr(orgMap.get("companyName"), "无");
        //登入者公司信息 地址等等
        String companySql = "select c.addr,c.name,o.org_code from " + TableList.COMPANY + " c" +
                " left join " + TableList.ORG + " o on c.orgId=o.id " +
                "where c.id=" + companyId;
        Map<String, Object> company = findFirstBySql(companySql);
        String addr = BaseUtil.objToStr(company.get("addr"), "");
        //查看是否重复邀约 如果是邀约recordType=2 访客与被访者在数据库中位置调换
//        Map<String, Object> check = check(userId, visitorId, 2, startDate, endDate);
//
//        if (check != null) {
//            //发送回消息
//            logger.info(startDate + "该时间段" + endDate + "内已经有邀约信息存在");
//            return Result.unDataResult("fail", "在" + startDate + "——" + endDate + "内已经有邀约信息存在");
//        }
        //被邀约者/访客Id
        Map<String, Object> visitRecord = new HashMap<>();
        visitRecord.put("userId", userId);
        visitRecord.put("visitorId", visitorId);
        //直接传入已审核同意进出
        if ("F".equals(token)){
            visitRecord.put("cstatus", "applySuccess");
            visitRecord.put("remarkName", realName);
        }else {
            visitRecord.put("cstatus", "applyConfirm");
        }
        visitRecord.put("visitDate", DateUtil.getCurDate());
        visitRecord.put("visitTime", DateUtil.getCurTime());
        visitRecord.put("reason", reason);
        visitRecord.put("startDate", startDate);
        visitRecord.put("endDate", endDate);
        visitRecord.put("vitype", "F");
        visitRecord.put("orgCode", orgCode);
        visitRecord.put("companyId", companyId);
        visitRecord.put("recordType", 2);
        //提示为非好友邀约
//        visitRecord.put("answerContent", "非好友邀约");
        //记录访问记录
        int saveVisitRecord = save(TableList.VISITOR_RECORD, visitRecord);

        if (saveVisitRecord > 0) {
            visitRecord.put("id",saveVisitRecord);
            for (Map.Entry<String, Object> entry : visitRecord.entrySet()) {
                if (entry.getValue() == null) {
                    visitRecord.put(entry.getKey(), "无");
                }
            }
            if ("F".equals(token)){//邀约非平台用户
                String encode = Base64.encode(String.valueOf(saveVisitRecord).getBytes("UTF-8"));
                String url = Constant.URL + encode;
                YunPainSmsUtil.sendSmsCode(url, phone, 6, addr, companyName, endDate, userName, startDate, visitorName);
            }else{

                visitPushService.visitPush(startDate,visitor,user,visitRecord,8);
            }
            return Result.unDataResult("success", "邀约成功");

        } else {
            return Result.unDataResult("fail", "邀约失败");
        }

    }

    /**
     * 通过接口回应邀约 1.更新访客日志表 2.发送推送给邀约人
     *
     * @author cwf
     * @date 2019/12/4 18:03
     */
    public Result visitReply(Map<String, Object> paramMap) {
        try {
            Integer id = BaseUtil.objToInteger(paramMap.get("id"), null);
            //登入人
            Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
            String cstatus = BaseUtil.objToStr(paramMap.get("cstatus"), null);
            String answerContent = BaseUtil.objToStr(paramMap.get("answerContent"), "");
            //更新邀约信息
            Map<String, Object> updateMap = new HashMap<>();
            if (id == null || userId == null || cstatus == null) {
                return Result.unDataResult("fail", "缺少参数");
            }
            updateMap.put("id", id);
            updateMap.put("replyUserId", userId);
            updateMap.put("replyDate", DateUtil.getCurDate());
            updateMap.put("replyTime", DateUtil.getCurTime());
            updateMap.put("cstatus", cstatus);
            updateMap.put("answerContent", answerContent);
            updateMap.put("isReceive", "F");

            int update = update(TableList.VISITOR_RECORD, updateMap);
            String apply = "同意";
            if ("applyFail".equals(cstatus)) {
                apply = "拒绝";
            }
            if (update > 0) {
                //返回回消息
                Map<String, Object> visitorRecord =findById(TableList.VISITOR_RECORD, id);
                Integer visitorId = BaseUtil.objToInteger(visitorRecord.get("visitorId"), null);
                Map<String, Object> user = findById(TableList.USER, userId);
                Map<String, Object> visitor = findById(TableList.USER, visitorId);
                String startDate=BaseUtil.objToStr(visitorRecord.get("startDate"),"");
                System.out.println("用户" + visitorId + "是否在线：" + Constant.SESSIONS.containsKey(visitorId));
//                //发送websocket
//                if (Constant.SESSIONS.containsKey(visitorId)) {
//                    JSONObject obj = new JSONObject();
//                    obj.put("orgName", "无");
//                    obj.put("companyId", "无");
//                    Integer companyId = BaseUtil.objToInteger(visitorRecord.get("companyId"), 0);
//                    String orgCode = BaseUtil.objToStr(visitorRecord.get("orgCode"), null);
//                    visitorRecord.remove("companyId");
//                    visitorRecord.remove("orgCode");
                    for (Map.Entry<String, Object> entry : visitorRecord.entrySet()) {
                        if (entry.getValue() == null) {
                            visitorRecord.put(entry.getKey(), "无");
                        }
//                        obj.put(entry.getKey(), entry.getValue());
                    }
//                    if (companyId != 0) {
//                        Map<String, Object> comMap = baseDao.findById(TableList.COMPANY, companyId);
//                        System.out.println(comMap);
//                        visitorRecord.put("companyName", comMap.get("companyName"));
//                    }
//                    if (orgCode != null) {
//                        String sql = "select org_name from " + TableList.ORG + " where org_code='" + orgCode + "'";
//                        Map<String, Object> corgMap = baseDao.findFirstBySql(sql);
//                        visitorRecord.put("orgName", corgMap.get("org_name"));
//                    }
//                    obj.put("type", Constant.MASSEGETYPE_REPLY);
//                    obj.put("fromUserId", userId);
//                    obj.put("visitorId", visitorId);
//                    System.out.println("发送给toUser的消息为+" + obj);
//                    Constant.SESSIONS.get(visitorId).sendMessage(new TextMessage(obj.toJSONString()));
//                } else {
//                    //发送推送
//                    Map<String, Object> toUser = baseDao.findById(TableList.USER, visitorId.intValue());
//                    String notification_title = "邀约回应信息提醒";
//                    String deviceToken = BaseUtil.objToStr(toUser.get("deviceToken"), "");
//                    String msg_content = "【朋悦比邻】您好，您有一条邀约已回应，请登入app查收!";
//                    boolean single = false;
//
////						String deviceType = BaseUtil.objToStr(toUser.get("deviceType"), "0");
//
//                    String phone = BaseUtil.objToStr(toUser.get("phone"), "0");
//                    //个推
//                    single = GTNotification.Single(deviceToken, phone, notification_title, msg_content, msg_content);
////						isYmSuc=shortMessageService.YMNotification(deviceToken,deviceType,notification_title,msg_content,isOnlineApp);
//                    logger.info("发送个推 推送成功? 设备号{}", single);
//
//                }
                visitPushService.visitPush(startDate,user,visitor,visitorRecord,9);
                return Result.unDataResult("success", apply + "邀约成功！");
            } else {
                return Result.unDataResult("fail", apply + "邀约失败！");
            }
        } catch (Exception e) {
            logger.error("邀请回应报错！", e);
            return Result.unDataResult("fail", "系统错误！");
        }
    }
    /**
     * 回应邀约
     * update by cwf  2019/9/11 11:05 Reason: 回应时将isReceive 字段改为'F'
     */
    @Override
    public void visitReply(WebSocketSession session, JSONObject msg) throws Exception {
        //根据Id获取需要更新的类容
        try {
            String replyDate = DateUtil.getCurDate();
            String replyTime = DateUtil.getCurTime();
            Integer id = msg.getInteger("id");
            System.out.println("visitReply:" + id);
            //登入人
            long userId = (long) session.getAttributes().get("userId");
            String cstatus = BaseUtil.objToStr(msg.get("cstatus"), null);
            System.out.println("cstatus:" + cstatus);
            String answerContent = BaseUtil.objToStr(msg.get("answerContent"), null);
            String sql = "update " + TableList.VISITOR_RECORD +
                    " set cstatus='" + cstatus + "', replyDate='" + replyDate + "', replyTime='" + replyTime + "' " +
                    ",answerContent='" + answerContent + "',replyUserId=" + userId + ",isReceive='F' " +
                    " where id = " + id;
            int update = baseDao.deleteOrUpdate(sql);
            if (update > 0) {
                //返回回消息
                session.sendMessage(new TextMessage(Result.ResultCodeType("success", "审核成功", "200", 3)));

                long toUserId = msg.getLong("toUserId");
                System.out.println("用户" + toUserId + "是否在线：" + Constant.SESSIONS.containsKey(toUserId));
                //
                Map<String, Object> visitorMap = baseDao.findById(TableList.VISITOR_RECORD, id);
                if (Constant.SESSIONS.containsKey(toUserId)) {
                    long fromUserId = (long) session.getAttributes().get("userId");
                    JSONObject obj = new JSONObject();
                    obj.put("orgName", "无");
                    obj.put("companyId", "无");
                    obj.put("fromUserId", fromUserId);

                    Integer companyId = BaseUtil.objToInteger(visitorMap.get("companyId"), 0);
                    String orgCode = BaseUtil.objToStr(visitorMap.get("orgCode"), null);
                    visitorMap.remove("companyId");
                    visitorMap.remove("orgCode");
                    for (Map.Entry<String, Object> entry : visitorMap.entrySet()) {
                        if (entry.getValue() == null) {
                            visitorMap.put(entry.getKey(), "无");
                        }
                        obj.put(entry.getKey(), entry.getValue());
                    }
                    if (companyId != 0) {
                        Map<String, Object> comMap = baseDao.findById(TableList.COMPANY, companyId);
                        System.out.println(comMap);
                        obj.put("companyName", comMap.get("companyName"));
                    }
                    if (orgCode != null) {
                        sql = "select org_name from " + TableList.ORG + " where org_code='" + orgCode + "'";
                        Map<String, Object> corgMap = baseDao.findFirstBySql(sql);
                        obj.put("orgName", corgMap.get("org_name"));
                    }
                    obj.put("type", Constant.MASSEGETYPE_REPLY);
                    obj.put("fromUserId", userId);
                    obj.put("toUserId", msg.get("toUserId"));
                    obj.put("updatetime",DateUtil.getSystemTime());
                    System.out.println("发送给toUser的消息为+" + obj);
                    Constant.SESSIONS.get(toUserId).sendMessage(new TextMessage(obj.toJSONString()));
                    //不在线推送
                } else {
                    Map<String, Object> toUser = baseDao.findById(TableList.USER, (int) toUserId);
                    String notification_title = "预约访问信息提醒";
                    String deviceToken = BaseUtil.objToStr(toUser.get("deviceToken"), null);
                    String msg_content = "【朋悦比邻】您好，您有一条预约访问已审核，请登入app查收!";
                    String realName = BaseUtil.objToStr(toUser.get("realName"), "");
                    String startDate = BaseUtil.objToStr(visitorMap.get("startDate"), "");
//						String deviceType = BaseUtil.objToStr(toUser.get("deviceType"), "0");
                    String isOnlineApp = BaseUtil.objToStr(toUser.get("isOnlineApp"), "F");
                    String phone = BaseUtil.objToStr(toUser.get("phone"), "0");
                    String visitorResult = "审核不成功";
                    if ("applySuccess".equals(cstatus)) {
                        System.out.println("审核成功");
                        visitorResult = "审核成功";
                    }
                    if ("F".equals(isOnlineApp)) {
                        codeService.sendMsg(phone, 3, visitorResult, realName, startDate, null);
                        logger.info(realName + "：发送短信推送成功");
                    } else {
                        boolean single = GTNotification.Single(deviceToken, phone, notification_title, msg_content, msg_content);
                        if (!single) {
                            codeService.sendMsg(phone, 3, visitorResult, realName, startDate, null);
                        }
                    }
                    logger.info("发送个推推送成功设备号{}", deviceToken);

                }
            } else {
                logger.info("发送失败");
                session.sendMessage(new TextMessage(Result.ResultCodeType("success", "审核失败", "-1", 3)));
            }
        } catch (Exception e) {

            e.printStackTrace();
            session.sendMessage(new TextMessage(Result.ResultCodeType("fail", "系统错误", "500", 3)));
            return;
        }

    }

    //邀约与访问的用户位置变化
    public int saveVisitRecord(Map<String, Object> paramMap) throws Exception {
        Integer recordType = BaseUtil.objToInteger(paramMap.get("recordType"), null);
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        Integer visitorId = BaseUtil.objToInteger(paramMap.get("visitorId"), null);
        Map<String, Object> saveMap = new HashMap<>();
        //如果是邀约，则访客为对方userId，被访问者为己方visitorId
        if (Constant.RECORDTYPE_INVITE == recordType) {
            userId = visitorId;
            visitorId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        }
        paramMap.put("userId", userId);
        paramMap.put("visitorId", visitorId);
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            if (entry.getValue() != null) {
                saveMap.put(entry.getKey(), entry.getValue());
            }
        }
        return save(TableList.VISITOR_RECORD, saveMap);
    }

    /**
     * 访问或者邀约 根据recordType进行判断
     *
     * @param session websocket信息
     * @param msg
     * @return void
     * @throws Exception
     * @author cwf
     * @date 2019/9/24 10:56
     */
    @Override
    public void receiveVisit(WebSocketSession session, JSONObject msg) throws Exception {

        try {
            //获取登入人ID
            long userId = BaseUtil.objToLong(session.getAttributes().get("userId"), 0L);
            //获取msg中的消息转存数据库
            //recordType 1--访问 2--邀约
            Integer recordType = BaseUtil.objToInteger(msg.get("recordType"), 0);
            //被访人或被约人
            Integer toUserId = BaseUtil.objToInteger(msg.get("toUserId"), 0);
            String recordStr = recordType == 1 ? "您访问的用户" : "您邀请的用户";
            String Str = recordType == 1 ? "访问" : "邀约";
            if (BaseUtil.objToInteger(session.getAttributes().get("userId"), 0).equals(toUserId)) {
                session.sendMessage(new TextMessage(Result.ResultCodeType("fail", "请不要对自己发起" + Str, "-1", BaseUtil.objToInteger(msg.get("type"), 2))));
                return;
            }
            String cstatus = BaseUtil.objToStr(msg.get("cstatus"), "applyConfirm");
            String startDate = BaseUtil.objToStr(msg.get("startDate"), "无");
            String endDate = BaseUtil.objToStr(msg.get("endDate"), "无");
            String reason = BaseUtil.objToStr(msg.get("reason"), "无");
            /** update by cwf  2019/9/24 10:33 Reason: 由被访人选择公司，*/
            Integer companyId = BaseUtil.objToInteger(msg.get("companyId"), 0);
            //查询登入人信息
            Map<String, Object> userMap = findById(TableList.USER, (int) userId);
            Map<String, Object> visitMap = findById(TableList.USER, toUserId);
            //登入人公司ID与大楼ID
            Map<String, Object> check = null;
            String notification_title = "访问信息提醒";
            //查询被访人或被约人是否实名
            boolean verify = userService.isVerify(toUserId);
            logger.info("被访者或被邀约者{}实名:{}", toUserId, verify);

            if (recordType == 1 && !verify) {
                logger.info(recordStr + "未实名,无法进行" + Str);
                session.sendMessage(new TextMessage(Result.ResultCodeType("fail", recordStr + "未实名", "-1", BaseUtil.objToInteger(msg.get("type"), 2))));
                return;
            }
            if (companyId == 0) {
                String sql = "select 1 from " + TableList.COMPANY_USER + " where userId=" + toUserId + " and currentStatus" +
                        "='normal' and status='applySuc'";
                Map<String, Object> existUser = findFirstBySql(sql);
                if (existUser == null) {
                    session.sendMessage(new TextMessage(Result.ResultCodeType("fail", recordStr + "无归属公司", "-1", BaseUtil.objToInteger(msg.get("type"), 2))));
                    return;
                }
            }
            //如果是访问recordType=1
            if (Constant.RECORDTYPE_VISITOR.equals(recordType)) {
                //查询内部是否有邀约信息
                check = check((int) userId, toUserId, recordType, startDate, endDate);
                //如果是邀约recordType=2 访客与被访者在数据库中位置调换
            } else if (Constant.RECORDTYPE_INVITE.equals(recordType)) {
                check = check(toUserId, (int) userId, recordType, startDate, endDate);
                notification_title = "邀约信息提醒";
            }
            if (check != null) {
                //发送回消息
                session.sendMessage(new TextMessage(Result.ResultCodeType("fail", "在" + startDate + "——" + endDate + "内已经有" + Str + "信息存在", "-1", BaseUtil.objToInteger(msg.get("type"), 2))));
                logger.info(startDate + "该时间段" + endDate + "内已经有" + Str + "信息存在");
                return;
            }
            Map<String, Object> paramMap = new HashMap<>();
            //查询大楼编码
            String orgName = null;
            String companyName = null;
            if (Constant.RECORDTYPE_INVITE.equals(recordType)) {
                String sql = "select o.org_name,o.org_code,c.companyName from " + TableList.ORG + " o left join " + TableList.COMPANY + " c" +
                        " on o.id=c.orgId where c.id='" + companyId + "'";

                Map<String, Object> orgMap = findFirstBySql(sql);
                String orgCode = BaseUtil.objToStr(orgMap.get("org_code"), "无");
                orgName = BaseUtil.objToStr(orgMap.get("org_name"), "无");
                companyName = BaseUtil.objToStr(orgMap.get("companyName"), "无");
                System.out.println(orgMap);
                paramMap.put("orgCode", orgCode);
            }
            paramMap.put("userId", userId);
            paramMap.put("visitorId", toUserId);
            paramMap.put("companyId", companyId);
            paramMap.put("cstatus", cstatus);
            paramMap.put("reason", reason);
            paramMap.put("startDate", startDate);
            paramMap.put("endDate", endDate);
            paramMap.put("visitDate", DateUtil.getCurDate());
            paramMap.put("visitTime", DateUtil.getCurTime());
            paramMap.put("recordType", recordType);
            paramMap.put("dateType", "limitPeriod");
            paramMap.put("vitype", "A");
            paramMap.put("isReceive", "F");
            //存入数据库
            int saveVisitRecord = saveVisitRecord(paramMap);
            JSONObject obj = new JSONObject();
            obj.put("sign", "success");
            obj.put("desc", "操作成功");
            obj.put("code", "200");
            obj.put("type", BaseUtil.objToInteger(msg.get("type"), 2));
            obj.put("id", saveVisitRecord);
            obj.put("userId", userId);
            obj.put("visitorId", toUserId);
            obj.put("startDate", startDate);
            obj.put("endDate", endDate);
            if (saveVisitRecord > 0) {
                System.out.println("储存数据成功");
                System.out.println(obj.toJSONString());
                //送还登入者 type=2
                session.sendMessage(new TextMessage(obj.toJSONString()));
            }
            //用户在线，调用发送接口
            if (Constant.SESSIONS.containsKey((long) toUserId)) {
                //获取邀约人/访问人地址给对方
//			if (companyId!=0){
//			Map<String,Object> comMap=baseDao.findById(TableList.COMPANY,companyId);
//			msg.put("companyName",comMap.get("companyName"));
//			}else {
//                msg.put("companyName","null");
//            }
//
//			if (orgId!=0){
//			Map<String,Object> corgMap=baseDao.findById(TableList.ORG,orgId);
//				System.out.println(corgMap);
//			msg.put("orgName",corgMap.get("org_name"));
//			}else {
//                msg.put("orgName","无");
//            }
                msg.put("orgName", orgName);
                msg.put("companyName", companyName);
                msg.put("fromUserId", userId);
                msg.put("id", saveVisitRecord);
                msg.put("visitDate", DateUtil.getCurDate());
                msg.put("visitTime", DateUtil.getCurTime());
                msg.put("updatetime",DateUtil.getSystemTime());
                msg.put("dateType", "limitPeriod");
                msg.put("answerContent", "null");
                msg.put("replyDate", "null");
                msg.put("replyTime", "null");
                msg.put("vitype", "A");
                msg.put("replyUserId", "null");
                msg.put("realName", BaseUtil.objToStr(userMap.get("realName"), ""));
                msg.put("idHandleImgUrl", BaseUtil.objToStr(userMap.get("idHandleImgUrl"), ""));
                msg.put("niceName", BaseUtil.objToStr(userMap.get("niceName"), ""));
                System.out.println(toUserId + "发送访问请求" + msg);
                Constant.SESSIONS.get((long) toUserId).sendMessage(new TextMessage(msg.toJSONString()));
                Map<String, Object> updateMap = new HashMap<>();
                //websocket发送完成后，改变下发状态
                updateMap.put("id", saveVisitRecord);
                updateMap.put("isReceive", "T");
                update(TableList.VISITOR_RECORD, updateMap);
            } else {
                //发送推送给用户
                String msg_content = "【朋悦比邻】您好，您有一条预约访客申请，请登入app查收!";
                String deviceToken = BaseUtil.objToStr(visitMap.get("deviceToken"), "");
//				String deviceType = BaseUtil.objToStr(userMap.get("deviceType"), "0");
//				String isOnlineApp = BaseUtil.objToStr(userMap.get("isOnlineApp"),"T");
                String phone = BaseUtil.objToStr(visitMap.get("phone"), "0");
                boolean single = GTNotification.Single(deviceToken, phone, notification_title, msg_content, msg_content);
                logger.info("发送个推 推送成功? {}", single);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.sendMessage(new TextMessage(Result.ResultCodeType("fail", "系统异常", "500", BaseUtil.objToInteger(msg.get("type"), 2))));
            return;
        }
    }

    @Override
    public Map<String, Object> check(Object userId, Object toUserId, Object recordType, String startDate, String endDate) {
        String sql = " select * from " + TableList.VISITOR_RECORD + " where userId = '" + userId + "' and visitorId ='"
                + toUserId + "' and recordType = " + recordType + " and cstatus<>'applyFail' and STR_TO_DATE(startDate,'%Y-%m-%d %H:%i')<STR_TO_DATE('" + endDate + "','%Y-%m-%d %H:%i')" +
                " and   STR_TO_DATE(endDate,'%Y-%m-%d %H:%i')>STR_TO_DATE('" + startDate + "','%Y-%m-%d %H:%i')";
        logger.info("检查是否时间段有邀约信息：{}", sql);
        return findFirstBySql(sql);
    }

    //查询是否已预约，增加了根据访客与被访者是否企业用户进行查看
    private Map<String, Object> check(Object userId, Object toUserId,
                                      Object recordType, Object startDate,
                                      Object endDate, Object originType, Object toType) {

        String sql = " select * from " + TableList.VISITOR_RECORD + " where userId = '" + userId + "' and visitorId ='"
                + toUserId + "' and recordType = " + recordType + " and cstatus<>'applyFail' and originType='" + originType + "' " +
                "and toType='" + toType + "' " +
                " and STR_TO_DATE(startDate,'%Y-%m-%d %H:%i')<STR_TO_DATE('" + endDate + "','%Y-%m-%d %H:%i')" +
                " and   STR_TO_DATE(endDate,'%Y-%m-%d %H:%i')>STR_TO_DATE('" + startDate + "','%Y-%m-%d %H:%i') ";
        logger.info("检查是否时间段有邀约信息：{}", sql);
        return findFirstBySql(sql);
    }

    //我的邀约 访问我的人
    @Override
    public Result myVisitOrInvite(Map<String, Object> paramMap, Integer pageNum, Integer pageSize, Integer recordType) {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if (userId == null) {
            return Result.unDataResult("fail", "缺少参数");
        }
        String coloumSql = "select * ";
        String fromSql = " from ((SELECT vr.id,IF(u.realName IS NULL or u.realName=\"\",remarkName,u.realName) realName,u.phone,u.headImgUrl,\n" +
                "\tvr.visitDate,vr.visitTime,vr.userId,vr.visitorId,vr.reason,vr.cstatus,vr.dateType\n" +
                ",vr.startDate,vr.endDate,vr.answerContent,vr.orgCode,vr.companyId,vr.recordType,\n" +
                "vr.replyDate,vr.replyTime,vr.vitype,vr.replyUserId,vr.isReceive,o.org_name,c.companyName,o.accessType" +
                " from " + TableList.VISITOR_RECORD + " vr\n" +
                "left join " + TableList.USER + " u on u.id=vr.visitorId\n" +
                "left join " + TableList.COMPANY + " c on vr.companyId=c.id\n" +
                "left join  " + TableList.ORG + " o on vr.orgCode=o.org_code " +
                "where userId=" + userId + " and recordType=" + recordType +
                "  ORDER BY startDate>NOW() desc,  IF(startDate > NOW(), FIELD(cstatus,'Cancle','applyFail',  'applySuccess','applyConfirm'), startDate ) desc,startDate desc,endDate asc limit 1000)";
        String union = " union \n" +
                "(SELECT vr.id,IF(u.realName IS NULL or u.realName=\"\",remarkName,u.realName) realName,u.phone,u.headImgUrl,\n" +
                "\tvr.visitDate,vr.visitTime,vr.userId,vr.visitorId,vr.reason,vr.cstatus,vr.dateType," +
                "vr.startDate,vr.endDate,vr.answerContent,vr.orgCode,vr.companyId,vr.recordType," +
                "vr.replyDate,vr.replyTime,vr.vitype,vr.replyUserId,vr.isReceive,o.org_name,c.companyName,o.accessType " +
                "FROM " + TableList.VISITOR_RECORD + " vr\n" +
                "left join " + TableList.USER + " u on u.id=vr.userId\n" +
                "left join " + TableList.COMPANY + " c on vr.companyId=c.id\n" +
                "left join  " + TableList.ORG + " o on vr.orgCode=o.org_code " +
                "where vr.visitorId=" + userId + " and recordType=" + recordType + " " +
                "ORDER BY startDate>NOW() desc,  IF(startDate > NOW(), FIELD(cstatus,'Cancle','applyFail',  'applySuccess','applyConfirm'), startDate ) desc,startDate desc,endDate asc limit 1000))x";
//        String oder=" ORDER BY visitDate desc,visitTime desc ";
        logger.info(coloumSql + fromSql + union);
        PageModel pageModel = findPage(coloumSql, fromSql + union, pageNum, pageSize);
        return ResultData.dataResult("success", "获取成功", pageModel);
    }

    /**
     * 通过记录id查询
     *
     * @param paramMap id
     * @author cwf
     * @date 2019/10/12 11:23
     */
    @Override
    public Result findRecordFromId(Map<String, Object> paramMap) {
        Integer id = BaseUtil.objToInteger(paramMap.get("id"), 0);
        if (id == 0) {
            Result.unDataResult("fail", "id不能为空");
        }
        String sql = "select vr.*,realName,niceName,sex,idHandleImgUrl,headImgUrl,c.companyName,c.addr from " + TableList.VISITOR_RECORD + " vr left join " + TableList.USER + " u" +
                " on u.id=vr.visitorId " +
                " left join " + TableList.COMPANY + " c on c.id=vr.companyId " +
                "where vr.id=" + id;
        Map<String, Object> recordMap = findFirstBySql(sql);

        return ResultData.dataResult("success", "获取成功", recordMap);
    }

    /**
     * 更新访客记录中的大楼与公司，并判断用户角色
     *
     * @param paramMap id
     * @return Result   公司角色
     * @author cwf
     * @date 2019/10/12 11:23
     */
    @Override
    public Result updateRecord(Map<String, Object> paramMap) throws Exception {
        Integer id = BaseUtil.objToInteger(paramMap.get("id"), 0);
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), 0);
        Integer companyId = BaseUtil.objToInteger(paramMap.get("companyId"), 0);

        if (id == 0 || companyId == 0) {
            return ResultData.unDataResult("fail", "缺少参数");
        }
        //更新访问记录的公司，大楼编号
        int update = updateRecordFromId(id, companyId);
        if (update <= 0) {
            return ResultData.unDataResult("fail", "修改公司失败");
        }
        //判断权限与转发
        Map<String, Object> visitRecord = findById(TableList.VISITOR_RECORD, id);
        Integer visitId = BaseUtil.objToInteger(visitRecord.get("visitorId"), 0);
        logger.info("通过访客记录查找被访者id：" + visitId);
        if (userId != visitId) {
            return ResultData.unDataResult("fail", "被访者与登入人不符");
        }
        //查询用户所在公司的角色
        Map<String, Object> userCompany = companyUserService.findByUserCompany(visitId, companyId);
        if (userCompany == null) {
            return ResultData.unDataResult("fail", "当前用户在不存在于此公司");
        }
        String roleType = BaseUtil.objToStr(userCompany.get("roleType"), "");
        return ResultData.dataResult("success", roleType, userCompany);
    }


    /**
     * 查询大楼编号并更新数据库
     *
     * @param id        主键id
     * @param companyId 公司id
     * @return int
     * @throws Exception
     * @author cwf
     * @date 2019/10/12 11:49
     */
    public int updateRecordFromId(Integer id, Integer companyId) throws Exception {

        String orgCode = orgService.findOrgCodeByCompanyId(companyId);
        String sql = "update " + TableList.VISITOR_RECORD + " set companyId=" + companyId + ",orgCode='" + orgCode + "'" +
                " where id=" + id;
        int update = deleteOrUpdate(sql);
        return update;
    }

    /**
     * 查看权限并转发给管理员
     *
     * @param visitor   被访者姓名
     * @param visitorBy 访问者姓名
     * @param companyId 被访者公司
     * @param startDate 开始时间
     * @return
     * @throws Exception
     * @author cwf
     * @date 2019/10/12 14:55
     */
    @Deprecated
    @Override
    public Result forwarding(String visitor, String visitorBy, String companyId, String startDate) throws Exception {
        // 被访者无管理权限查询出该公司的所有管理人员
        String deviceToken = "";
        String columnSql = " select u.* ";
        String fromSql = "  from " + TableList.USER + " u left join " + TableList.COMPANY_USER + " cu on u.id=cu.userId  where  cu.companyId = '" + companyId
                + "' and role = 'manage' and currentStatus='normal'";
        List<Map<String, Object>> list = this.findList(columnSql, fromSql);
        if (list == null || list.isEmpty()) {
            return Result.unDataResult("fail", "该公司没有管理者，无法推送审核信息");
        }
//        List<String> aliasList = null;
        boolean single = false;
        String managePhone = null;
        String phone = null;
        for (int i = 0; i < list.size(); i++) {
            managePhone = list.get(i).get("phone").toString();
            // 审核人员
//            aliasList = new ArrayList();
//            aliasList.add(managePhone);
            //批量发送给具有deviceToken的用户
            deviceToken = BaseUtil.objToStr(list.get(i).get("deviceToken"), "");
            String notification_title = "访客-审核通知";
            String msg_content = "【朋悦比邻】您好，您有一条预约访客需审核，访问者:" + visitor + "，被访者:" + visitorBy + ",访问时间:"
                    + startDate;


//					String deviceType = BaseUtil.objToStr(list.get(i).get("deviceType"), "0");
            String isOnlineApp = BaseUtil.objToStr(list.get(i).get("isOnlineApp"), "F");
            phone = BaseUtil.objToStr(list.get(i).get("phone"), "0");
//个推
            single = GTNotification.Single(deviceToken, phone, notification_title, msg_content, msg_content);
//					isYmSuc=shortMessageService.YMNotification(deviceToken,deviceType,notification_title,msg_content,isOnlineApp);
            logger.info(BaseUtil.objToStr(list.get(i).get("realName"), "无名") + "：发送友盟推送成功");
            if (!single || "F".equals(isOnlineApp)) {
                codeService.sendMsg(managePhone, 4, null, visitorBy, startDate, visitor);
                logger.info(BaseUtil.objToStr(list.get(i).get("realName"), "无名") + "：发送短信推送成功");
            }
        }
        return Result.unDataResult("success", "发起请求成功");
    }

    //通过id查询访客信息并转发给管理员
    @Override
    public Result visitForwarding(Map<String, Object> paramMap) throws Exception {
        Integer id = BaseUtil.objToInteger(paramMap.get("id"), 0);
        Integer uId = BaseUtil.objToInteger(paramMap.get("userId"), 0);
        if (id == 0 || uId == 0) {
            return Result.unDataResult("fail", "缺少参数");
        }

        Map<String, Object> record = findById(TableList.VISITOR_RECORD, id);
        //被访问者
        Integer visitorId = BaseUtil.objToInteger(record.get("visitorId"), 0);
        if (uId != visitorId) {
            return Result.unDataResult("fail", "被访者与记录不同！");
        }
        String startDate = BaseUtil.objToStr(record.get("startDate"), "");
        String companyId = BaseUtil.objToStr(record.get("companyId"), "");
        //访问者
        Integer userId = BaseUtil.objToInteger(record.get("userId"), 0);

        Map<String, Object> userUser = userService.getUserByUserId(userId);
        Map<String, Object> visitorUser = userService.getUserByUserId(visitorId);
        //访问者姓名
        String visitorBy = BaseUtil.objToStr(userUser.get("realName"), "");
        //被访问者姓名
        String visitor = BaseUtil.objToStr(visitorUser.get("realName"), "");

        return forwarding(visitor, visitorBy, companyId, startDate);
    }



    //		//访问推送处理
    public void visitPush
    (Map<String, Object> visitorRecord, Map<String, Object> userUser, Map<String, Object> visitorUser, Map<String, Object> updateMap, JSONObject
            msg, String visitorResult, Map<String, String> wxMap) throws Exception {
        Long toUserId = BaseUtil.objToLong(visitorRecord.get("userId"), (long) 0);
        String viType = BaseUtil.objToStr(visitorRecord.get("vitype"), "");
        String startDate = BaseUtil.objToStr(visitorRecord.get("startDate"), "");
        String deviceToken = BaseUtil.objToStr(userUser.get("deviceToken"), "");
//		String isOnlineApp = BaseUtil.objToStr(userUser.get("isOnlineApp"), "");
        String phone = BaseUtil.objToStr(userUser.get("phone"), "");
        String visitorBy = BaseUtil.objToStr(visitorUser.get("realName"), "");
        //发送访问者websocket聊天框

        if ("A".equals(viType)) {
            //用户在线，调用发送接口
            if (Constant.SESSIONS.containsKey(toUserId)) {
                for (Map.Entry<String, Object> entry : visitorRecord.entrySet()) {
                    if (entry.getValue() == null) {
                        visitorRecord.put(entry.getKey(), "无");
                    }
                    msg.put(entry.getKey(), entry.getValue());
                }
                Constant.SESSIONS.get(toUserId).sendMessage(new TextMessage(msg.toJSONString()));
                updateMap.put("isReceive", "T");
                update(TableList.VISITOR_RECORD, updateMap);
            } else {
                boolean single = false;
                //不在线发送推送给用户
                String notification_title = "您的访问申请已审核";
                String msg_content = "访问申请已审核，请进入app查看!";
                String isOnlineApp = BaseUtil.objToStr(userUser.get("isOnlineApp"), "F");
//个推
                single = GTNotification.Single(deviceToken, phone, notification_title, msg_content, msg_content);
                //个推不在线，短信推送
                if (!single || "F".equals(isOnlineApp)) {
                    codeService.sendMsg(phone, 3, visitorResult, visitorBy, startDate, null);
                }
            }
            //推送微信
        } else if ("C".equals(viType)) {

            if (wxMap.get("wxOpenId") == null || "".equals(wxMap.get("wxOpenId"))) {
                //短信推送
                codeService.sendMsg(phone, 3, visitorResult, visitorBy, startDate, null);
            } else {
                //审核结果发送给访问者微信公众号
                logger.info("wxUrl=" + ParamDef.findDirByName("wxUrl"));
                String s = HttpClientUtil.sendPost(ParamDef.findDirByName("wxUrl"), wxMap, "application/x-www-form-urlencoded");
                //如果微信推送失败，则切换短信推送
            }
            //其他情况，短信推送
        } else {

            codeService.sendMsg(phone, 3, visitorResult, visitorBy, startDate, null);
        }
    }




    //邀约我的人
    @Override
    public Result inviteMine(Map<String, Object> paramMap, Integer pageNum, Integer pageSize, Integer recordType) {
        //todo 将我的邀约与邀约我的人功能拆分出来
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if (userId == null) {
            return Result.unDataResult("fail", "缺少参数");
        }
        String coloumSql = "SELECT vr.id,IF(u.realName IS NULL or u.realName=\"\",remarkName,u.realName) realName,u.phone,u.headImgUrl,\n" +
                "\tvr.visitDate,vr.visitTime,vr.userId,vr.visitorId,vr.reason,vr.cstatus,vr.dateType," +
                "vr.startDate,vr.endDate,vr.answerContent,vr.orgCode,vr.companyId,vr.recordType," +
                "vr.replyDate,vr.replyTime,vr.vitype,vr.replyUserId,vr.isReceive,o.org_name,c.companyName,o.accessType ";
        String fromSql = "FROM " + TableList.VISITOR_RECORD + " vr\n" +
                "left join " + TableList.USER + " u on u.id=vr.userId\n" +
                "left join " + TableList.COMPANY + " c on vr.companyId=c.id\n" +
                "left join  " + TableList.ORG + " o on vr.orgCode=o.org_code " +
                "where vr.userId=" + userId + " and recordType=" + recordType + " " +
                "ORDER BY startDate>NOW() desc,  IF(startDate > NOW(), FIELD(cstatus,'Cancle','applyFail',  'applySuccess','applyConfirm'), startDate ) desc,startDate desc,endDate";
        logger.info("邀约我的人\n" + coloumSql + fromSql);
        PageModel pageModel = findPage(coloumSql, fromSql, pageNum, pageSize);
        return ResultData.dataResult("success", "获取成功", pageModel);
    }



    //我的访问
    @Override
    public Result myVisit(Map<String, Object> paramMap, Integer pageNum, Integer pageSize, Integer recordType) {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if (userId == null) {
            return Result.unDataResult("fail", "缺少参数");
        }

        String coloumSql = "SELECT vr.id,IF(u.realName IS NULL or u.realName=\"\",remarkName,u.realName) realName,u.phone,u.headImgUrl,\n" +
                "\tvr.visitDate,vr.visitTime,vr.userId,vr.visitorId,vr.reason,vr.cstatus,vr.dateType\n" +
                ",vr.startDate,vr.endDate,vr.answerContent,vr.orgCode,vr.companyId,vr.recordType,\n" +
                "vr.replyDate,vr.replyTime,vr.vitype,vr.replyUserId,vr.isReceive,o.org_name,c.companyName,o.accessType";
        String fromSql = " from " + TableList.VISITOR_RECORD + " vr\n" +
                "left join " + TableList.USER + " u on u.id=vr.visitorId\n" +
                "left join " + TableList.COMPANY + " c on vr.companyId=c.id\n" +
                "left join  " + TableList.ORG + " o on vr.orgCode=o.org_code " +
                "where userId=" + userId + " and recordType=" + recordType +
                "  ORDER BY startDate>NOW() desc,  IF(startDate > NOW(), FIELD(cstatus,'Cancle','applyFail',  'applySuccess','applyConfirm'), startDate ) desc,startDate desc,endDate ";
        logger.info("我的邀约：" + coloumSql + fromSql);
        PageModel pageModel = findPage(coloumSql, fromSql, pageNum, pageSize);
        return ResultData.dataResult("success", "获取成功", pageModel);
    }

    /**
     * 访问邀约
     *
     * @return
     */
    @Override
    public Result visitorList(Map<String, Object> paramMap) {
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 1);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 10);
        String condition = BaseUtil.objToStr(paramMap.get("condition"), "");
        String userId = BaseUtil.objToStr(paramMap.get("userId"), "");
        Integer recordType = BaseUtil.objToInteger(paramMap.get("recordType"), 0);
        if ("".equals(condition) || "".equals(userId) || recordType == 0) {
            return Result.unDataResult("fail", "查询失败，参数缺失");
        } else if (!"userId".equals(condition) && !"visitorId".equals(condition)) {
            return Result.unDataResult("fail", "查询失败，条件错误");
        } else if (recordType != 1 && recordType != 2) {
            return Result.unDataResult("fail", "查询失败，邀约访问条件错误");
        }
        String otherMan = "userId".equals(condition) ? "visitorId" : "userId";
        String coloumSql = "SELECT vr.id,IF(u.realName IS NULL or u.realName=\"\",remarkName,u.realName) realName,u.phone,u.headImgUrl,\n" +
                "\tvr.visitDate,vr.visitTime,vr.userId,vr.visitorId,vr.reason,vr.cstatus,vr.dateType\n" +
                ",vr.startDate,vr.endDate,vr.answerContent,vr.orgCode,vr.companyId,vr.recordType,\n" +
                "vr.replyDate,vr.replyTime,vr.vitype,vr.replyUserId,vr.isReceive,o.org_name,c.companyName,o.accessType";
        String from = " from " + TableList.VISITOR_RECORD + " vr\n" +
                "left join " + TableList.USER + " u on u.id=vr." + otherMan + "\n" +
                "left join " + TableList.COMPANY + " c on vr.companyId=c.id\n" +
                "left join  " + TableList.ORG + " o on vr.orgCode=o.org_code " +
                "where " + condition + "=" + userId + " and recordType=" + recordType +
                " ORDER BY startDate>NOW() desc,  IF(startDate > NOW(), FIELD(cstatus,'Cancle','applyFail',  'applySuccess','applyConfirm'), startDate ) desc,startDate desc,endDate";
        PageModel pageModel = this.findPage(coloumSql, from, pageNum, pageSize);
        logger.info(coloumSql + from);
        //查看未过期记录的条数
        Map<String, Object> countMap = findFirstBySql("select count(*) num from " + TableList.VISITOR_RECORD + "  where visitorId = " + userId + " and cstatus='applyConfirm' and endDate>SYSDATE() and recordType=" + recordType + "  ");
        String count = BaseUtil.objToStr(countMap.get("num"), "0");
        return pageModel != null ? ResultData.dataResultCount("success", "获取成功", pageModel, count)
                : ResultData.dataResult("success", "暂无数据", new PageModel(pageNum, pageSize));
    }

    @Override
    public Result visitorSucList(Map<String, Object> paramMap) {
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 1);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 10);
        String userId = BaseUtil.objToStr(paramMap.get("userId"), "");
        if ("".equals(userId)) {
            return Result.unDataResult("fail", "查询失败，参数缺失");
        }
        String coloumSql = "SELECT vr.id,IF(u.realName IS NULL,IF(remarkName is null,'',remarkName),u.realName) realName,u.phone,u.headImgUrl,\n" +
                "\tvr.visitDate,vr.visitTime,vr.userId,vr.visitorId,vr.reason,vr.cstatus,vr.dateType\n" +
                ",vr.startDate,vr.endDate,vr.answerContent,vr.orgCode,vr.companyId,vr.recordType,\n" +
                "vr.replyDate,vr.replyTime,vr.vitype,vr.replyUserId,vr.isReceive,o.org_name,c.companyName,c.addr,o.accessType";
        String from = " from " + TableList.VISITOR_RECORD + " vr\n" +
                "left join " + TableList.USER + " u on u.id=vr.visitorId" +
                "\n" +
                "left join " + TableList.COMPANY + " c on vr.companyId=c.id\n" +
                "left join  " + TableList.ORG + " o on vr.orgCode=o.org_code " +
                "where vr.userId=" + userId + " and cstatus='applySuccess' and u.id is not null  " +
                " ORDER BY startDate>NOW() desc,  IF(startDate > NOW(), FIELD(cstatus,'Cancle','applyFail',  'applySuccess','applyConfirm'), startDate ) desc,startDate desc,endDate";
        logger.info(coloumSql + from);
        PageModel pageModel = this.findPage(coloumSql, from, pageNum, pageSize);
        return pageModel != null ? ResultData.dataResult("success", "获取成功", pageModel)
                : ResultData.dataResult("success", "暂无数据", new PageModel(pageNum, pageSize));
    }

    /**
     * 访问邀约记录
     *
     * @param paramMap
     * @return
     */
    @Override
    public Result findRecordUser(Map<String, Object> paramMap) {
        Long userId = BaseUtil.objToLong(paramMap.get("userId"), 0L);
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 0);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 10);
        if (userId.equals(0L)) {
            return ResultData.unDataResult("fail", "缺少参数");
        }

        String coloumSql = "select *";
        String fromSql = " from (SELECT vr.id,vr.visitorId, Max( startDate ) startDate,Max(endDate) endDate,u.realName,idHandleImgUrl,headImgUrl,\n" +
                "IF( companyName IS NULL, '', companyName ) companyName,\n" +
                "IF( c.addr IS NULL, '', c.addr ) addr" + " FROM (\n" +
                "select id,IF(userId=" + userId + ",visitorId,userId) visitorId,startDate,endDate,companyId,orgCode\n" +
                "from tbl_visitor_record\n" +
                "where userId=" + userId + " or visitorId=" + userId + ")vr\n" +
                "LEFT JOIN " + TableList.USER + " u ON vr.visitorId = u.id\n" +
                "LEFT JOIN " + TableList.COMPANY + " c ON c.id = vr.companyId\n" +
                " where u.realName is not null\n" +
                "GROUP BY visitorId order by startDate desc)x";
        logger.info(coloumSql + fromSql);
        PageModel page = findPage(coloumSql, fromSql, pageNum, pageSize);
        return ResultData.dataResult("success", "查看成功", page);
    }

    @Override
    public Result findRecordUserDetail(Map<String, Object> paramMap) {
        Long userId = BaseUtil.objToLong(paramMap.get("userId"), 0L);
        Long visitorId = BaseUtil.objToLong(paramMap.get("visitorId"), 0L);
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 0);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 10);
        Integer recordType = BaseUtil.objToInteger(paramMap.get("recordType"), 0);

        if (userId.equals(0L) || visitorId.equals(0L)) {
            return ResultData.unDataResult("fail", "缺少参数");
        }
        //查询全部
        String and = " recordType =" + recordType + " and";
        if (recordType == 0) {
            and = "";
        }
        String coloumSql = "select *";
        String fromSql = "from (select vr.id,vr.userId,vr.visitorId,vr.visitDate,vr.visitTime,vr.recordType,vr.startDate,vr.endDate,vr.cstatus,\n" +
                "IF(vr.replyDate IS NULL,'',vr.replyDate) replyDate,IF(vr.replyTime IS NULL,'',vr.replyTime) replyTime," +
                "IF( companyName IS NULL, '', companyName ) companyName,\n" +
                "IF( c.addr IS NULL, '', c.addr ) addr," +
                "(CASE WHEN vr.userId=" + userId + " AND vr.recordType=1 THEN u.realName \n" +
                "WHEN vr.userId=" + userId + " AND vr.recordType=2 THEN vu.realName \n" +
                "WHEN  vr.visitorId=" + userId + " AND vr.recordType=1 THEN u.realName  \n" +
                "WHEN  vr.visitorId=" + userId + " AND vr.recordType=2 THEN vu.realName \n" +
                "else '无' end) originator,\n" +
                "(CASE WHEN vr.userId=" + userId + " AND vr.recordType=1 THEN vu.realName \n" +
                "WHEN vr.userId=" + userId + " AND vr.recordType=2 THEN u.realName \n" +
                "WHEN  vr.visitorId=" + userId + " AND vr.recordType=1 THEN vu.realName  \n" +
                "WHEN  vr.visitorId=" + userId + " AND vr.recordType=2 THEN u.realName \n" +
                "else '无' end) receiver,\n" +
                "u.realName visitor,\n" +
                "vu.realName visited from tbl_visitor_record vr\n" +
                "        left join tbl_company c on c.id=vr.companyId\n" +
                "        left join tbl_user u on u.id=vr.userId" +
                "        left join tbl_user vu on vu.id = vr.visitorId" +
                "        where vu.realName is not null and u.realName is not null and " + and + "((userId=" + userId + " and visitorId=" + visitorId + ") or(userId=" + visitorId + " and visitorId=" + userId + "))\n" +
                "        ORDER BY startDate>NOW() desc, IF(startDate > NOW(), FIELD(cstatus,'Cancle','applyFail',\n" +
                "        'applySuccess','applyConfirm'), startDate ) desc,startDate desc,endDate asc)x";
        Map<String, Object> visitor = findFirstBySql("select realName from " + TableList.USER + " where id=" + visitorId);
        String visitorName = "无";
        if (visitor != null) {
            visitorName = BaseUtil.objToStr(visitor.get("realName"), "无");
        }

        logger.info(coloumSql + fromSql);
        PageModel page = findPage(coloumSql, fromSql, pageNum, pageSize);
        return ResultData.dataResult("success", visitorName, page);
    }

}

