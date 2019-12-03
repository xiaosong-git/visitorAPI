package com.goldccm.websocket.stomp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import java.util.List;

/**
 * EnableWebSocketMessageBroker 注解表明： 这个配置类不仅配置了 WebSocket，还配置了基于代理的 STOMP 消息；
 * registerStompEndpoints() 方法：添加一个服务端点，来接收客户端的连接。将 “/chat” 路径注册为 STOMP 端点。这个路径与之前发送和接收消息的目的路径有所不同， 这是一个端点，客户端在订阅或发布消息到目的地址前，要连接该端点，即用户发送请求 ：url=’/127.0.0.1:8080/chat’ 与 STOMP server 进行连接，之后再转发到订阅url；
 * configureMessageBroker() 方法：配置了一个 简单的消息代理，通俗一点讲就是设置消息连接请求的各种规范信息。
 *
 * @author linyun
 * @date 2018/9/13 下午5:15
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebStompConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketHandleInterceptor interceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry)   {
        //添加一个/chat端点，客户端就可以通过这个端点来进行连接；withSockJS作用是添加SockJS支持
//        registry.addEndpoint("/chatTest").setAllowedOrigins("*");
        System.out.println(1);
        registry.addEndpoint("/chatTest1").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration webSocketTransportRegistration) {

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //定义了两个客户端订阅地址的前缀信息，也就是客户端接收服务端发送消息的前缀信息
//        registry.enableSimpleBroker("/message", "/notice");
        //定义了服务端接收地址的前缀，也即客户端给服务端发消息的地址前缀
        registry.setApplicationDestinationPrefixes("/app");
        /**
         * 设置单独发送到某个user需要添加的前缀，用户订阅地址/user/topic/td1地址后会去掉/user，并加上用户名（需要springsecurity支持）等唯一标识组成新的目的地发送回去，
         * 对于这个url来说 加上后缀之后走代理。发送时需要制定用户名:convertAndSendToUser或者sendtouser注解.
         */
        registry.setUserDestinationPrefix("/user");
        registry.enableStompBrokerRelay("/message","/notice")
                .setRelayHost("47.106.82.190")
                .setRelayPort(61613)
                .setSystemLogin("admin")
                .setSystemPasscode("admin")
                .setClientLogin("admin")
                .setClientPasscode("admin");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        //注册了一个接受客户端消息通道拦截器
        registration.setInterceptors(interceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration channelRegistration) {

    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> list) {

    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> list) {

    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> list) {
        return true;
    }
}
