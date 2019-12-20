package com.goldccm.service.WebSocket.impl;

import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.WebSocket.IWebSocketService;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.shortMessage.impl.ShortMessageServiceImpl;
import com.goldccm.service.user.IUserService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.DateUtil;
import com.goldccm.util.GTNotification;
import com.goldccm.websocket.IWebSoketHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * websocket接口
 * @author chenwf
 * @date 2019/7/22 10:38
 */
@Service("webSocketService")
public class WebSocketServiceimpl extends BaseServiceImpl implements IWebSocketService {

    Logger log = LoggerFactory.getLogger(IWebSoketHandle.class);
    @Autowired
    private IBaseDao baseDao;
    @Autowired
    private IUserService userService;
    @Autowired
    public ShortMessageServiceImpl shortMessageService;
    public Result sendMessageToUser(WebSocketSession session, Long fromUserId, Long type, String content, Long toUserId, TextMessage message) throws Exception {
        try {
            if (session.isOpen()) {
                session.sendMessage(message);
            } else {
                saveMessage(fromUserId, toUserId, content, type);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail();
        }
        return Result.success();
    }
    @Override
    public int saveMessage(Long fromUserId, Long toUserId, String content, Long type) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 0);
        params.put("fromUserId", fromUserId);
        params.put("toUserId", toUserId);
        params.put("message", content);
        params.put("type", type);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        params.put("update_time", date);

        return baseDao.save(TableList.USER_MESSAGE, params);
    }
    @Override
    public Result gainMessagefromDb(WebSocketSession session, Long toUserId) throws Exception {
        //从数据库获取离线消息
        List<Map<String, Object>> msgList = getMsgByToUserId(toUserId);
        System.out.println(msgList);
        if (msgList == null) {
            return Result.unDataResult("success", "无聊天记录需要获取");
        }
        JSONObject obj = new JSONObject();
        for (Map<String, Object> msgMap : msgList) {
            String realName=BaseUtil.objToStr(msgMap.get("realName"),"null");
            String nickName=BaseUtil.objToStr(msgMap.get("niceName"),"null");
            String headImgUrl=BaseUtil.objToStr(msgMap.get("headImgUrl"),"null");
            String idHandleImgUrl=BaseUtil.objToStr(msgMap.get("idHandleImgUrl"),"null");
            for (Map.Entry<String, Object> entry : msgMap.entrySet()) {
                String mapKey = entry.getKey();
                Object mapValue = entry.getValue();
                obj.put(mapKey, mapValue);

            }
            obj.put("realName",realName);
            obj.put("nickName",nickName);
            obj.put("headImgUrl",headImgUrl);
            obj.put("idHandleImgUrl",idHandleImgUrl);
            Integer type = BaseUtil.objToInteger(msgMap.get("type"), 0);
            if (type==4){
                String sql="select count(*) c from "+TableList.USER_FRIEND+" where friendId="+toUserId+" and applyType=0";
                Map<String, Object> count = findFirstBySql(sql);
                if (count!=null){
                    obj.put("count",count.get("c"));
                }
            }
            obj.remove("id");
            System.out.println(obj.toJSONString());
            //发送给session连接者
            session.sendMessage(new TextMessage(obj.toJSONString()));
        }

            if (deleteMessage(toUserId)>0){
                System.out.println("删除聊天记录成功");
            }

        return Result.success();
    }

    @Override
    public List<Map<String, Object>> getMsgByToUserId(Long toUserId) {
        String coloumSql = "select um.*,niceName,realName,headImgUrl ";
        String fromSql = " from " + TableList.USER_MESSAGE + " um " +
                "left join "+TableList.USER+" u on fromUserId=u.id" +
                " where toUserId = " + toUserId;
        return baseDao.findList(coloumSql, fromSql);
    }
    /**
     获取离线访问消息sql
     update by cwf  2019/9/11 11:03 Reason:改为union 来查询 提高效率 11/14 增加vitype='A' 条件
     */
    @Override
    public List<Map<String, Object>> getVisitRecordByVisitorId( Long userId) {

        String coloumSql = " select * ";

        /* 查看谁访问我 我=被访者=visitorId=20 记录状态=recordType=1 阅读状态=未阅读='F' replyDate is null */
        String fromSql = " from(\n" +
                "select *  from "+TableList.VISITOR_RECORD+" where endDate>SYSDATE() and  isReceive ='F' and cstatus='applyConfirm' and" +
                " visitorId = "+userId+" and recordType=1 and vitype='A' and replyDate is null\n";
        /* 查看谁邀请我去访问 我=访客=userId=20 记录状态=recordType=2 阅读状态=未阅读='F' replyDate is null */
        String union1="union all\n" +
                "select *  from "+TableList.VISITOR_RECORD+" where endDate>SYSDATE() and  isReceive ='F' and cstatus='applyConfirm' and " +
                "userId =  "+userId+" and recordType=2 and vitype='A' and replyDate is  null\n";
        /* 查看谁回应了我的访问申请 我=访客=userId=20 replyDate is not null回应日期不为空，状态不是申请中 cstatus<>'applyConfirm' 则说明回应 记录状态=recordType=1 */
        String union2="union all\n" +
                "select *  from tbl_visitor_record where endDate>SYSDATE() and  isReceive ='F' and cstatus<>'applyConfirm' " +
                "and userId =  "+userId+" and recordType=1 and vitype='A' and replyDate is not null\n";
        /* 查看谁回应了我的邀约申请 我=被访者=visitorId=20 replyDate is not null回应日期不为空，状态不是申请中 cstatus<>'applyConfirm' 则说明回应 记录状态=recordType=1  */
        String union3="union all \n" +
                "select *  from tbl_visitor_record where endDate>SYSDATE() and  isReceive ='F' and cstatus<>'applyConfirm' " +
                "and visitorId =  "+userId+" and vitype='A' and recordType=2\n" +
                "and replyDate is not null";
        String suffix=")x";
//        System.out.println(coloumSql+fromSql+union1+union2+union3+suffix);
        return baseDao.findList(coloumSql, fromSql+union1+union2+union3+suffix);
    }

    /**
     * 上线拉取离线消息
     * 1.首先查看登入者是否有离线的访客消息，如果有则进入下一步 2.根据list循环发送每一条邀约信息
     * 2.1 发送信息时判断是(邀约/访问)还是应答（邀约/访问）
     * 3.判断是否已经发送过消息给访客，isReceive是为T 否为F
     */
    @Override
    public Result gainVisitRcordfromDb(WebSocketSession session, Long userId) throws Exception {
        //1从数据库获取离线邀约消息
        List<Map<String, Object>> msgList = getVisitRecordByVisitorId(userId);
        if (msgList == null||msgList.isEmpty()) {
            System.out.println("无访问记录需要获取");
            return Result.unDataResult("success", "无访问记录需要获取");
        }
        System.out.println(msgList);
        JSONObject obj = new JSONObject();
        //2.根据list循环发送每一条邀约信息
        for (Map<String, Object> msgMap : msgList) {
            for (Map.Entry<String, Object> entry : msgMap.entrySet()) {
//                System.out.println(entry.getKey()+","+entry.getValue());
                obj.put(entry.getKey(), entry.getValue()==null?"null":entry.getValue());
            }
            //判断消息来源 1访问 2 邀约
            obj.remove("userId");
            //fromUserId=谁发送的 当recordType=1时 发送人为userId
            if(msgMap.get("recordType")==Constant.RECORDTYPE_VISITOR){
                obj.put("fromUserId",msgMap.get("userId"));
                //获取用户信息
                saveJson(BaseUtil.objToInteger(msgMap.get("userId"),0),obj);
               // 当recordType=1时 发送人为userId
            }else if(msgMap.get("recordType")==Constant.RECORDTYPE_INVITE){
                obj.put("fromUserId",msgMap.get("visitorId"));
                //获取用户信息
                saveJson(BaseUtil.objToInteger(msgMap.get("visitorId"),0),obj);
            }
            obj.put("toUserId",userId);
            //2.1 发送信息时判断是(邀约/访问)还是应答（邀约/访问）
            if ("applyConfirm".equals(msgMap.get("cstatus"))){
                obj.put("type", Constant.MASSEGETYPE_VISITOR);
            }else {
                //如果状态不为applyConfirm 那么返回tpye=3作为应答
                obj.put("type", Constant.MASSEGETYPE_REPLY);
            }
            //发送给session连接者
            System.out.println("发送websocket消息:"+obj.toJSONString());
            session.sendMessage(new TextMessage(obj.toJSONString()));
            //3.判断是否已经发送过消息给访客，是为T 否为F
            if (!("T".equals(msgMap.get("isReceive")))){
                updateRecord((long)msgMap.get("id"));
            }
        }
        return Result.unDataResult("success", "访问记录获取成功");
    }
    @Override
    public Result sendVisitRcord(WebSocketSession session,Map<String, Object> paramMap) throws Exception{
        JSONObject obj = new JSONObject();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String mapKey = entry.getKey();
            Object mapValue = entry.getValue();
            obj.put(mapKey, mapValue);
        }
                //type=2 作为websocket的访问标志
                obj.put("type", Constant.MASSEGETYPE_VISITOR);
                System.out.println(obj.toJSONString());
                session.sendMessage(new TextMessage(obj.toJSONString()));

        return Result.unDataResult("success", "发送记录成功");
    }

    @Override
    public Result dealChat(WebSocketSession session,JSONObject msg) throws Exception{
        //获取主要消息
        String content=msg.getString("message");
        if (content==null){
            return Result.unDataResult("fail","message不能为空");
        }
        //获取fromUserId
        long fromUserId=(long)session.getAttributes().get("userId");

        //传输对象
        long toUserId= msg.getInteger("toUserId");
        Integer type= msg.getInteger("type");
        //判断
        JSONObject obj = new JSONObject();
        try {
                System.out.println("查询在线情况+" + Constant.SESSIONS.get(toUserId));
//                for (Map.Entry<Object, WebSocketSession> entry : Constant.SESSIONS.entrySet()) {
//                    System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
//                }
                //用户在线，调用发送接口
                if (Constant.SESSIONS.containsKey(toUserId)) {

                    obj.put("message", msg.getString("message"));
                    obj.put("toUserId", toUserId);
                    obj.put("fromUserId", fromUserId);
                    obj.put("type",type);
                    obj.put("updateTime", DateUtil.getSystemTime());
                    //查看用户信息
                    saveJson((int)fromUserId,obj);
//                    //查看好友申请数量
                    if (type==4){
                        String sql="select count(*) c from "+TableList.USER_FRIEND+" where friendId="+toUserId+" and applyType=0";
                        Map<String, Object> count = findFirstBySql(sql);
                        if (count!=null){
                            obj.put("count",count.get("c"));
                        }
                    }
                    sendMessageToUser(Constant.SESSIONS.get(toUserId), fromUserId, (long)toUserId, content,(long) type, new TextMessage(obj.toJSONString()));

                    session.sendMessage(new TextMessage(Result.ResultCodeType("success","发送成功","200",type)));
                    //用户不在线，插入数据库
                } else {
                    int istrue = saveMessage(fromUserId, toUserId, content, (long)type);
                    if (istrue>0){
                        session.sendMessage(new TextMessage(Result.ResultCodeType("success","发送成功","200",type)));
                        //发送推送
                        Map<String, Object> toUserMap = findById(TableList.USER, (int) toUserId);
                        String deviceToken = BaseUtil.objToStr(toUserMap.get("deviceToken"), "");
//                        String deviceType = BaseUtil.objToStr(toUserMap.get("deviceType"), "0");
//                        String isOnlineApp = BaseUtil.objToStr(toUserMap.get("isOnlineApp"), "T");
                            String notification_title="您有一条聊天消息需处理！";
                            if (type==4){
                                notification_title="您有一条好友申请需处理！";
                            }
                          String  phone = BaseUtil.objToStr(toUserMap.get("phone"), "0");
                        //个推
                         GTNotification.Single(deviceToken, phone, notification_title, content, content);
//                         shortMessageService.YMNotification(deviceToken,deviceType,notification_title,content,isOnlineApp);
                    }else {
                        session.sendMessage(new TextMessage(Result.ResultCodeType("fail","发送失败","-1",type)));
                    }
                    log.info("插入成功,当前id" + istrue);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.success();
    }

    @Override
    public int deleteMessage(Long toUserId) throws Exception {

        String sql = "delete from " +TableList.USER_MESSAGE +
                " where  toUserId="+toUserId ;
        return baseDao.deleteOrUpdate(sql);
    }


    @Override
    public String undataResult(String code,String resutl,Integer type){
        JSONObject obj =new JSONObject();
        obj.put("code",code);
        obj.put("result ",resutl);
        obj.put("type ",type);
        return obj.toJSONString();
    }
    @Override
    public Result sendVisitToUser(WebSocketSession session,TextMessage message) throws Exception{
        try {
            if (session.isOpen()) {
                session.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail();
        }
        return Result.success();
    }

    public int updateRecord(long id) throws Exception {

        String sql = "update  " +TableList.VISITOR_RECORD +
                " set isReceive='T'"+
                " where  id = "+id+" and SYSDATE()<endDate " ;
//        System.out.println(sql);
        return baseDao.deleteOrUpdate(sql);
    }

    /**
     * 获取用户信息并存入jsonObj
     *
     */
    @Override
    public void saveJson(Integer fromUserId, JSONObject obj){
        Map<String,Object> paraMap=userService.getUserByUserId(fromUserId);
        String realName=BaseUtil.objToStr(paraMap.get("realName"),"null");
        String nickName=BaseUtil.objToStr(paraMap.get("niceName"),"null");
        String headImgUrl=BaseUtil.objToStr(paraMap.get("headImgUrl"),"null");
        String idHandleImgUrl=BaseUtil.objToStr(paraMap.get("idHandleImgUrl"),"null");
        String orgId=BaseUtil.objToStr(paraMap.get("orgId"),"null");
        obj.put("realName",realName);
        obj.put("nickName",nickName);
        obj.put("headImgUrl",headImgUrl);
        obj.put("idHandleImgUrl",idHandleImgUrl);
        obj.put("orgId",orgId);

    }
}
