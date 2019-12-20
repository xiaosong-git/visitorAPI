package com.goldccm.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.Result;
import com.goldccm.service.WebSocket.IWebSocketService;
import com.goldccm.service.user.IUserFriendService;
import com.goldccm.service.visitor.IVisitorRecordService;
import com.goldccm.util.BaseUtil;
import org.hamcrest.core.Is;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;


public class IWebSoketHandle extends AbstractWebSocketHandler {
    Logger logger = LoggerFactory.getLogger(IWebSoketHandle.class);

    @Autowired
    public IWebSocketService webSocketService;
    @Autowired
    public IVisitorRecordService visitorRecordService;
    @Autowired
    public IUserFriendService userFriendService;
    /**
     * 处理字符串类的信息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        session.sendMessage(new TextMessage(message.asBytes()));

       logger.info("客户发送信息："+message.asBytes());
    }
    /**
     *  连接成功后获取用户消息
     * @param session
     * @return void
     * @throws Exception
     * @author chenwf
     * @date 2019/7/24 11:12
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("链接成功......");

        //当前登入人ID
       try {
           //关闭相同连接
               long userId= BaseUtil.objToLong(session.getAttributes().get("userId"),null);
           if( Constant.SESSIONS.containsKey(userId)){

               Constant.SESSIONS.get(userId).close();
           }
           //存入在线情况

           Constant.SESSIONS.put(userId,session);
           for (Map.Entry<Object, WebSocketSession> entry: Constant.SESSIONS.entrySet()){
              logger.info("当前在线：user: "+entry.getKey()+" value: "+entry.getValue());
           }
           //获取当前登入人：userId的离线消息
           webSocketService.gainMessagefromDb(session,userId);
           //获取当前登入人：userId的邀约消息
          logger.info("准备进入拉取离线邀约消息");
           webSocketService.gainVisitRcordfromDb(session,userId);

       }catch (Exception e){
           e.printStackTrace();
           logger.info("初始化数据报错.....");
           return;
       }

    }
    /** 
     * 消息处理
     * @param session	
     * @param message
     * @return void
     * @throws Exception    
     * @author chenwf 
     * @date 2019/7/23 9:57
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String msgStr = message.getPayload().toString();
        int type=0;
        //解析消息
        try {
            //增加心跳检测
           if( "ping".equals(msgStr)){
               session.sendMessage(new TextMessage("pong"));
              return  ;
           }
            logger.info("处理要发送的消息：{}",msgStr);
            JSONObject msg = JSON.parseObject(msgStr);
             type= msg.getInteger("type");
            //判断是否为好友，非好友则返回信息
            boolean isFriend = isFriend(session, msg);
            //是好友
            if (isFriend){
                //type==4 好友申请数量
                if (Constant.MASSEGETYPE_NOMAL==type||4==type){
                    webSocketService.dealChat(session,msg);
                }
                //申请访问或申请邀约
                else if(Constant.MASSEGETYPE_VISITOR==type){
                    visitorRecordService.receiveVisit(session,msg);
                    //回应访问或回应邀约
                }else if(Constant.MASSEGETYPE_REPLY==type){
                    visitorRecordService.visitReply(session,msg);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.info("发送数据报错.....{}",msgStr);
            session.sendMessage(new TextMessage(Result.ResultCodeType("fail","发送失败","-1",type)));
            return;
        }

    }
    /**
     * 处理二进制类的信息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override

    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        session.sendMessage(new BinaryMessage(message.getPayload()));
    }

    /**
     * ping-pong
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {

    }

    /**
     * 传出错误的处理
     *
     * @param session
     * @param exception
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    /**
     * 连接关闭的处理
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

       logger.info(session.getAttributes().get("userId")+"客户连接关闭");
        Constant.SESSIONS.remove(session.getAttributes().get("userId"));
    }
    /**
     *  判断用户是否为好友，非好友则返回信息
     * @param session webSocket相关信息
     * @param msg 消息
     * @return  是否为好友
     * @throws IOException
     * @author cwf
     * @date 2019/12/19 14:45
     */
    public boolean isFriend(WebSocketSession session,JSONObject msg) throws IOException {

        int type= msg.getInteger("type");
        Object userId = session.getAttributes().get("userId");
        Object friendId= msg.get("toUserId");
        Map<String,Object> friendUser=userFriendService . findFriend(friendId,userId);
        JSONObject obj = new JSONObject();
        obj.put("fromUserId", friendId);
        obj.put("toUserId",userId);
        obj.put("message", "对方开启了好友验证，你还不是他好友，请先发送好友验证请求！");
        obj.put("type", type);
        webSocketService.saveJson(BaseUtil.objToInteger(friendId,0),obj);
        if (friendUser==null){
            logger.info("发送的消息：{}",obj.toString());
            session.sendMessage(new TextMessage(obj.toString()));
            //非好友
            return false;
            //todo 判断好友状态是否为2
        } else  {
            int applyType = BaseUtil.objToInteger(friendUser.get("applyType"), 2);
            if (applyType!=1) {
                logger.info("发送的消息：{}",obj.toString());
                session.sendMessage(new TextMessage(obj.toString()));
                //非好友
                return false;
            }
        }
        Map<String,Object> userFriend=userFriendService . findFriend(userId,friendId);
        if (userFriend==null){
            obj.put("message", "您还不是对方好友，请添加好友！");
            logger.info("发送的消息：{}",obj.toString());
            session.sendMessage(new TextMessage(obj.toString()));
            //非好友
            return false;
        }else  {
            int applyType = BaseUtil.objToInteger(userFriend.get("applyType"), 2);
            if (applyType!=1) {
                obj.put("message", "您还不是对方好友，请添加好友！");
                if(applyType==2){
                    obj.put("message", "您已删除对方，请重新添加好友！");
                }
                logger.info("发送的消息：{}",obj.toString());
                session.sendMessage(new TextMessage(obj.toString()));
               //非好友
                return false;
            }
        }
        //是好友
        return true;
    }


}