package com.goldccm.service.WebSocket;

import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

/**
 * websocket接口
 * @throws Exception
 * @author chenwf
 * @date 2019/7/22 18:50
 */
public interface IWebSocketService extends IBaseService {
     /**
      *  发送消息给个人
      * @param fromUserId
      * @param type
      * @param content
      * @param session
      * @param message
      * @return com.goldccm.model.compose.Result
      * @throws Exception
      * @author chenwf
      * @date 2019/7/22 18:53
      */
     Result sendMessageToUser(WebSocketSession session, Long fromUserId, Long userName, String content, Long type, TextMessage message) throws Exception;
     /**
      * 离线消息存到数据库
      * @param fromUserId
      * @param toUserId
      * @param content
      * @param type
      * @return com.goldccm.model.compose.Result
      * @throws Exception
      * @author chenwf
      * @date 2019/7/23 15:23
      */
     int saveMessage(Long fromUserId, Long toUserId, String content, Long type) throws Exception;
     /**
      * 获取离线消息
      * @param
      * @param session
      * @param toUserId
      * @return com.goldccm.model.compose.Result
      * @throws Exception
      * @author chenwf
      * @date 2019/7/22 18:55
      */
     Result gainMessagefromDb(WebSocketSession session, Long toUserId) throws Exception;
     /**
      * 获取离线消息sql语句
      * @throws Exception
      * @author chenwf
      * @date 2019/7/22 18:50
      */
     List<Map<String, Object>> getMsgByToUserId(Long toUserId);
     /**
      * 获取访问记录
      * @param userId
      * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
      * @throws Exception
      * @author chenwf
      * @date 2019/7/29 14:44
      */
     List<Map<String, Object>> getVisitRecordByVisitorId( Long userId);
     /**
      * 离线记录发送给websession连接者
      * @param session
      * @param userId
      * @return com.goldccm.model.compose.Result
      * @throws Exception
      * @author chenwf
      * @date 2019/7/29 14:26
      */
     Result gainVisitRcordfromDb(WebSocketSession session, Long userId) throws Exception;
     /**
      * 在线发送给websession连接者
      * @param session
      * @param paramMap
      * @return com.goldccm.model.compose.Result
      * @throws Exception
      * @author chenwf
      * @date 2019/7/29 14:26
      */
     /**
      * msg的type为1时处理聊天信息
      * @param session
      * @param msg
      * @return com.goldccm.model.compose.Result
      * @throws Exception
      * @author chenwf
      * @date 2019/7/29 18:09
      */
     Result dealChat(WebSocketSession session, JSONObject msg) throws Exception;
     /**
      * 删除已获取的聊天记录
      * @param toUserId
      * @return com.goldccm.model.compose.Result
      * @throws Exception
      * @author chenwf
      * @date 2019/7/29 18:09
      */
     int deleteMessage(Long toUserId) throws Exception;


     /**
      * 发送消息
      * @param session
      * @param message
      * @return com.goldccm.model.compose.Result
      * @throws Exception
      * @author chenwf
      * @date 2019/8/1 10:32
      */
     Result sendVisitToUser(WebSocketSession session,TextMessage message) throws Exception;

     String undataResult(String code,String result,Integer type);


     void saveJson(Integer fromUserId, JSONObject obj);
}
