package com.goldccm.websocket;

import com.goldccm.service.user.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;


public class IWebSoketHandleInterceptor implements HandshakeInterceptor {
    Logger logger = LoggerFactory.getLogger(IWebSoketHandleInterceptor.class);
    @Autowired
    private IUserService userService;
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse shr1, WebSocketHandler wsh, Map<String, Object> attributes) throws Exception {
        // 此处可以做一些权限认证的事情或者其他
        try {
            if (request instanceof ServletServerHttpRequest) {
                HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

                // 获取请求连接之前的token参数.
                Enumeration enu = servletRequest.getParameterNames();
                long userId =Long.parseLong(servletRequest.getParameter("userId"));
                String  token =(servletRequest.getParameter("token"));
                Map<String,Object> user = userService.getUserByUserToken((int)userId, token);
                if (user.isEmpty()||user==null){
                    return false;
                }
               logger.info("userId:"+userId+ ", token:"+token);
                //将ID作为在线条件插入session
                attributes.put("userId", userId);
            }

        }catch (Exception e ){
           logger.info("用户连接失败");
            return false;
        }

        return true;

    }

    @Override
    public void afterHandshake(ServerHttpRequest shr, ServerHttpResponse shr1, WebSocketHandler wsh, Exception excptn) {



    }
//    /**
//     * 根据token认证授权
//     * @param token
//     */
//    private Principal parseToken(String token){
//        //TODO 解析token并获取认证用户信息
//        return null;
//    }

}