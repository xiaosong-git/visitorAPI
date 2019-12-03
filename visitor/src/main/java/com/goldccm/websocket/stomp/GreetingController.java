package com.goldccm.websocket.stomp;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.sun.security.auth.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Map;

/**
 * @author linyun
 * @date 2018/9/13 下午5:42
 */

@Controller
@RequestMapping("/stomp")
public class GreetingController {
    Logger log = LoggerFactory.getLogger(GreetingController.class);
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    /**
     * 测试页面
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/chat4")
    @ResponseBody
    public String chat4() {
        System.out.println("chat4");
        return "chat4";
    }

    /**
     * 测试页面2
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/chat5")
    @ResponseBody
    public String chat5() {

        System.out.println("chat5");
        return "chat5";
    }

    /**
     * 测试订阅
     * @param message
     * @param messageHeaders
     * @param destination
     * @param headers
     * @param id
     * @param body
     */
    @MessageMapping("/hello/{id}")
    public void hello(Message message,
                      MessageHeaders messageHeaders,
                      @Header("destination") String destination,
                      @Headers Map<String, Object> headers,
                      @DestinationVariable long id,
                      @Payload String body) {
        log.info("message:{}", message);
        log.info("messageHeaders:{}", messageHeaders);
        log.info("destination:{}", destination);
        log.info("headers:{}", headers);
        log.info("id:{}", id);
        log.info("body:{}", body);
    }


    /***  群消息   ***/

    /**
     * 主动返回消息。
     * @param stompMessage
     */
    @MessageMapping("/hello")
    public void hello(@Payload StompMessage stompMessage) {
        System.out.println(stompMessage);
        StompMessage returnStompMessage = new StompMessage();
        returnStompMessage.setContent("转发，" + stompMessage.getContent());
        simpMessagingTemplate.convertAndSend("/message/public", returnStompMessage);
    }

    /**
     * 使用注解的方式返回消息
     * @param stompMessage
     * @return
     */
    @MessageMapping("/hello1")
    @SendTo("/message/public")
    public StompMessage hello1(@Payload StompMessage stompMessage) {
        System.out.println(stompMessage);
        StompMessage returnStompMessage = new StompMessage();
        returnStompMessage.setContent("转发2，" + stompMessage.getContent());
        return returnStompMessage;
    }

    /***  点对点   ***/

    /**
     * 点对点发送消息。接收消息的人是从消息中获取的。
     * @param stompMessage
     * @param principal
     */
    @MessageMapping("/hello2")
    public void hello2(@Payload StompMessage stompMessage, Principal principal) {
        System.out.println(stompMessage);
        System.out.println(principal);
        StompMessage returnStompMessage = new StompMessage();
        returnStompMessage.setContent("转发3，" + stompMessage.getContent());
        returnStompMessage.setTo(stompMessage.getTo());
        returnStompMessage.setFrom(stompMessage.getFrom());

        simpMessagingTemplate.convertAndSendToUser(stompMessage.getTo(), "/notice/msg", returnStompMessage);
    }

    public static void main(String[] args) {
        Principal principal =new UserPrincipal("jerry");

    }
}
