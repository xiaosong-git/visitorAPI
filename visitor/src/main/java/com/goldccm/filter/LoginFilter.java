package com.goldccm.filter;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @Author Linyb
 * @Date 2016/12/16.
 */
public class LoginFilter implements Filter{

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        filterChain.doFilter(request,response);


        /*String path = request.getServletPath();
        if(path.contains("/user/sendCode")
                || path.contains("/user/register")|| path.contains("/user/login")
                || path.contains("/user/forget") || path.contains("/user/isVerify")){
            filterChain.doFilter(request,response);
            return;
        }else{
            String token = request.getParameter("token");

            if(StringUtils.isNotBlank(token)){
                Map<String,Object> user = userService.getUserByToken(token);
                if(user != null){
                    filterChain.doFilter(request,response);
                    return;
                }
            }
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result","你还未登录");
            jsonObject.put("code","500");
            PrintWriter writer = response.getWriter();
            writer.append(jsonObject.toJSONString());
            writer.flush();
            writer.close();
        }*/

    }
    public void destroy() {

    }
}
