package com.goldccm.websocket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket

public class IWebSoketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 允许连接的域,只能以http或https开头
//        System.out.println("开启服务测试");
//        String[] allowsOrigins = {"http://127.0.0.1:8098/goldccm_war", "http://localhost:8098/goldccm_war"};
        //.withSockJS()如果WebSocket术不可用的话，就会选择另外的通信方式。SockJS会优先选用WebSocket --出自SpringInAction第四版
        registry.addHandler(webSoketHandle(),"/chat").addInterceptors(handshakeInterceptor()).setAllowedOrigins("*");
        registry.addHandler(webSoketHandle(),"/sockjs/chat").addInterceptors(handshakeInterceptor()).setAllowedOrigins("*").withSockJS();
    }

    @Bean
    public IWebSoketHandle webSoketHandle() {
        return new IWebSoketHandle();
    }

    @Bean
    public IWebSoketHandleInterceptor handshakeInterceptor() {
        return new IWebSoketHandleInterceptor();
    }
}