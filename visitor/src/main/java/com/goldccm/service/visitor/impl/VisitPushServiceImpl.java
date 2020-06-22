package com.goldccm.service.visitor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.MsgModel;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.code.ICodeService;
import com.goldccm.service.visitor.IVisitPushService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.GTNotification;
import com.goldccm.util.RedisUtil;
import com.goldccm.util.SSLClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.util.Map;

/**
 * @program: goldccm
 * @description: 各种推送相关代码
 * @author: cwf
 * @create: 2020-04-01 17:34
 **/
@Service("visitPushService")
public class VisitPushServiceImpl extends BaseServiceImpl implements IVisitPushService {

    @Autowired
    private ICodeService codeService;
    Logger logger = LoggerFactory.getLogger(VisitPushServiceImpl.class);

    @Override
    public void wxPush(String openid, String title, String name, String phone, String startDate, String reason, String remarkValue, String url) throws Exception {

        JSONObject jsonObject = new JSONObject();
        if (url != null) {
            jsonObject.put("url", openid);
        }
        jsonObject.put("touser", openid);   // openid
        if (Constant.IS_DEVELOP) {//测试环境
            jsonObject.put("template_id", "I-nNgggadJrZcZmkJzgxMVOptw4tf2MD9NKgYhWnCdM");
        } else {//生产环境
            jsonObject.put("template_id", "2UBJNiTiPPQTlwu2PHxtbCKhqao3Ix1I8mjGPBIWnUU");
        }
        JSONObject data = new JSONObject();
        JSONObject first = new JSONObject();
        first.put("value", title);
        first.put("color", "#173177");
        JSONObject keyword1 = new JSONObject();
        keyword1.put("value", name);
        keyword1.put("color", "#173177");
        JSONObject keyword2 = new JSONObject();
        keyword2.put("value", phone);
        keyword2.put("color", "#173177");
        JSONObject keyword3 = new JSONObject();
        keyword3.put("value", startDate);
        keyword3.put("color", "#173177");
        JSONObject keyword4 = new JSONObject();
        keyword4.put("value", reason);
        keyword4.put("color", "#173177");
        JSONObject remark = new JSONObject();
        remark.put("value", remarkValue);
        remark.put("color", "#173177");

        data.put("first", first);
        data.put("keyword1", keyword1);
        data.put("keyword2", keyword2);
        data.put("keyword3", keyword3);
        data.put("keyword4", keyword4);
        data.put("remark", remark);

        jsonObject.put("data", data);
        HttpClient httpClient = new SSLClient();
        String accessToken = RedisUtil.getStrVal("accessToken", 2);
        HttpPost postMethod = new HttpPost("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken);

        StringEntity entityStr = new StringEntity(JSON.toJSONString(jsonObject), HTTP.UTF_8);
        entityStr.setContentType("application/json");
        postMethod.setEntity(entityStr);
        HttpResponse resp = httpClient.execute(postMethod);
        int statusCode = resp.getStatusLine().getStatusCode();
        if (200 == statusCode) {
            String str = EntityUtils.toString(resp.getEntity(), HTTP.UTF_8);
            JSONObject resutlJson = JSONObject.parseObject(str);
            Map resultMap = JSON.parseObject(resutlJson.toString());
            Integer errcode = BaseUtil.objToInteger(resultMap.get("errcode"), 2333);
            String errmsg = BaseUtil.objToStr(resultMap.get("errmsg"), "");
            if (errcode == 0) {

                // 发送成功
                logger.info("发送微信模板成功，url:{}", url);
            } else {
                // 发送失败
                logger.info("发送失败：" + errmsg);
            }
        }
    }

    //访问推送
    @Override
    public void visitPush(String startDate, Map<String, Object> user, Map<String, Object> visitor, Map<String, Object> visitRecord, Integer shortType) throws Exception {
        //访问者姓名
        String userName = BaseUtil.objToStr(user.get("realName"), "");
        String userPhone = BaseUtil.objToStr(user.get("phone"), "");
        String userHandleImgUrl = BaseUtil.objToStr(user.get("idHandleImgUrl"), "");
        String userHeadImgUrl = BaseUtil.objToStr(user.get("headImgUrl"), "");
        Long userId = BaseUtil.objToLong(user.get("id"), null);
        //被访问者信息
        Long visitorId = BaseUtil.objToLong(visitor.get("id"), null);
        //被访者姓名
        String visitorName = BaseUtil.objToStr(visitor.get("realName"), null);
        String deviceToken = BaseUtil.objToStr(visitor.get("deviceToken"), "");
        String isOnlineApp = BaseUtil.objToStr(visitor.get("isOnlineApp"), "F");
        String visitorHandleImgUrl = BaseUtil.objToStr(visitor.get("idHandleImgUrl"), "");
        String visitorHeadImgUrl = BaseUtil.objToStr(visitor.get("headImgUrl"), "");
        String visitorPhone = BaseUtil.objToStr(visitor.get("phone"), "");
        String openId = BaseUtil.objToStr(visitor.get("wx_open_id"), "");

        String notification_title;
        String msg_content;
        String tital;
        String content;
        String visitorResult = BaseUtil.objToStr(visitRecord.get("cstatus"),"无");
//        String recordType = BaseUtil.objToStr(visitRecord.get("recordType"),"无");

        if (visitorResult.equals("applySuccess")){
            visitorResult="同意";
        }else{
            visitorResult="拒绝";
        }
        switch (shortType) {
            case 3: //访问审核
                notification_title = "您有一条访问已审核";
                msg_content = "被访者:" + userName + ",访问时间:" + startDate;
                tital="访问消息通知";
                content="您的访问申请信息已被对方审核";
                break;
            case 5://访问消息
                notification_title = "您有一条访客需审核";
                msg_content = "访问者:" + userName + ",访问时间:" + startDate;
                tital="访问申请";
                content="点击查看详情信息↓";
                break;
            case 8://邀约消息
                notification_title = "您有一条邀约需审核";
                msg_content = "邀约者:" + userName + ",访问时间:" + startDate;
                tital="邀约申请";
                content="点击查看详情信息↓";
                break;
            default :
                notification_title = "您有一条邀约已审核";
                msg_content = "访问者:" + userName + ",访问时间:" + startDate;
                tital="邀约消息通知";
                content="您的邀约申请信息已被对方审核";
        }


        //发送websocket给对方
        System.out.println(visitorId);
        System.out.println(Constant.SESSIONS.containsKey(visitorId));
        if (Constant.SESSIONS.containsKey(visitorId)) {
            MsgModel toVisitor = new MsgModel(2, userId, visitorId, userHandleImgUrl, userHeadImgUrl, userName, "", visitRecord);
            String s = JSONObject.toJSONString(toVisitor);
            JSONObject jsonObject = JSONObject.parseObject(s);
            for (Map.Entry<String, Object> entry : visitRecord.entrySet()) {
                    jsonObject.put(entry.getKey(),entry.getValue());
            }
            String toVisitorString = JSON.toJSONString(toVisitor);
            Constant.SESSIONS.get(visitorId).sendMessage(new TextMessage(jsonObject.toJSONString()));
            logger.info("发送websocket消息：{}", toVisitorString);
        }
//        //发送websocket给自己
//        System.out.println(userId);
//        System.out.println(Constant.SESSIONS.containsKey(userId));
//        if (Constant.SESSIONS.containsKey(userId)) {
//            MsgModel toUser = new MsgModel(2, userId, userId, visitorHandleImgUrl, visitorHeadImgUrl, visitorName, "", visitRecord);
//            String toUserString = JSON.toJSONString(toUser);
//            Constant.SESSIONS.get(userId).sendMessage(new TextMessage(toUserString));
//        }
        //个推不在线，发送短信
        if ("F".equals(isOnlineApp)) {
            codeService.sendMsg(visitorPhone, shortType, visitorResult, userName, startDate, userName);
            logger.info(visitorName + "：发送短信推送成功");
        } else {//个推在线 发送个推
            boolean single = GTNotification.Single(deviceToken, visitorPhone, notification_title, msg_content, msg_content);
            logger.info("发送个推成功{}", visitorName);
            if (!single) {//发送个推失败，则发送短信
                codeService.sendMsg(visitorPhone, shortType, visitorResult, userName, startDate, userName);
                logger.info(visitorName + "：发送短信推送成功");
            }
        }
        //发送公众号
        if (!"".equals(openId)) {
            String params = "?recordId=" + visitRecord.get("id") + "&otherId=" + userId + "&myId=" + visitorId + "&index=reply";
            String url = "";
            if (Constant.IS_DEVELOP) {
                url = Constant.DEV_WX_URL + "reply" + params;
            } else {
                url = Constant.PORD_WX_URL + "reply" + params;
            }
            wxPush(openId, tital, userName, userPhone, startDate, visitRecord.get("reason").toString(), content, url);
        }
    }

//    //回应访问推送
//    @Override
//    public void replyPush(String startDate,Map<String, Object> user,Map<String, Object> visitor,Map<String, Object> data) throws Exception {
//        //访问者姓名
//        String userName = BaseUtil.objToStr(user.get("realName"), "");
//        String userPhone = BaseUtil.objToStr(user.get("phone"), "");
//        String userHandleImgUrl = BaseUtil.objToStr(user.get("idHandleImgUrl"), "");
//        String userHeadImgUrl = BaseUtil.objToStr(user.get("headImgUrl"), "");
//        Long userId=BaseUtil.objToLong(user.get("id"),null);
//        //被访问者信息
//        Long visitorId=BaseUtil.objToLong(visitor.get("id"),null);
//        String deviceToken = BaseUtil.objToStr(visitor.get("deviceToken"), "");
//        String isOnlineApp = BaseUtil.objToStr(visitor.get("isOnlineApp"), "F");
//        String visitorHandleImgUrl = BaseUtil.objToStr(visitor.get("idHandleImgUrl"), "");
//        String visitorHeadImgUrl = BaseUtil.objToStr(visitor.get("headImgUrl"), "");
//        String visitorPhone = BaseUtil.objToStr(visitor.get("phone"), "");
//        String openId = BaseUtil.objToStr(visitor.get("wx_open_id"), "");
//        String notification_title = "您有一条访客信息已审核";
//        String msg_content = "被访问者:" + userName + ",访问时间:" + startDate;
//        //被访者姓名
//        String visitorName = BaseUtil.objToStr(visitor.get("realName"), null);
//        MsgModel toVisitor=new MsgModel(2,visitorId,userId,userHandleImgUrl,userHeadImgUrl,userName,"",data);
//        //发送websocket给对方
//        if (Constant.SESSIONS.containsKey(visitorId)) {
//            String toVisitorString = JSON.toJSONString(toVisitor);
//            Constant.SESSIONS.get(visitorId).sendMessage(new TextMessage(toVisitorString));
//            logger.info("发送websocket消息：{}", toVisitorString);
//        }
//        //发送websocket给自己
//        if (Constant.SESSIONS.containsKey(userId)){
//            MsgModel toUser=new MsgModel(2,visitorId,userId,visitorHandleImgUrl,visitorHeadImgUrl,visitorName,"",data);
//            String toUserString = JSON.toJSONString(toUser);
//            Constant.SESSIONS.get(userId).sendMessage(new TextMessage(toUserString));
//        }
//        //个推不在线，发送短信
//        if ("F".equals(isOnlineApp)) {
//            codeService.sendMsg(visitorPhone, 8, null, null, startDate, userName);
//            logger.info(visitorName + "：发送短信推送成功");
//        } else {//个推在线 发送个推
//            boolean single = GTNotification.Single(deviceToken, visitorPhone, notification_title, msg_content, msg_content);
//            logger.info("发送个推成功{}", visitorName);
//            if (!single) {//发送个推失败，则发送短信
//                codeService.sendMsg(visitorPhone, 8, null, null, startDate, userName);
//                logger.info(visitorName + "：发送短信推送成功");
//            }
//        }
//        //发送公众号
//        if (!"".equals(openId)) {
//            String params = "?recordId=" + data.get("id") + "&otherId=" + userId + "&myId=" + visitorId + "&index=reply";
//            String url = "";
//            if (Constant.IS_DEVELOP) {
//                url = Constant.DEV_WX_URL + "reply" + params;
//            } else {
//                url = Constant.PORD_WX_URL + "reply" + params;
//            }
//            wxPush(openId, "访问消息", userName, userPhone, startDate, data.get("reason").toString(), "点击查看详情信息↓", url);
//        }
//    }

}
