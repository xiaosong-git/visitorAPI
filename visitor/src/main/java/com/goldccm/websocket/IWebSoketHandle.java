package com.goldccm.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.Result;
import com.goldccm.service.WebSocket.IWebSocketService;
import com.goldccm.service.visitor.IVisitorRecordService;
import com.goldccm.util.BaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;


public class IWebSoketHandle extends AbstractWebSocketHandler {
    Logger logger = LoggerFactory.getLogger(IWebSoketHandle.class);

    @Autowired
    public IWebSocketService webSocketService;
    @Autowired
    public IVisitorRecordService visitorRecordService;
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




}