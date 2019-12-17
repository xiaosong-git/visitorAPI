package com.goldccm.inteceptor;

import com.alibaba.fastjson.JSONObject;
import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.user.IUserService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.DateUtil;
import com.goldccm.util.RedisUtil;
import com.goldccm.util.TokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 22:10
 */
@Configurable
public class AuthCheckInteceptor extends HandlerInterceptorAdapter {
    @Autowired
    private IUserService userService;

    @Autowired
    private IParamService paramService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 检查服务是否关闭
         */
        //redis修改，原dbNum=8 现在dbNum=32
        String ServerIsClose = RedisUtil.getStrVal("ServerIsClose",32);
        if(ServerIsClose == null){
            ServerIsClose = "F";
        }
        if("T".equals(ServerIsClose)){
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.append(JSONObject.toJSONString(Result.unDataResult("userFail","抱歉！服务器升级中，暂时无法使用！")));
            writer.flush();
            writer.close();
            return false;
        }
        HandlerMethod methodHandler=(HandlerMethod) handler;
        AuthCheckAnnotation auth = methodHandler.getMethodAnnotation(AuthCheckAnnotation.class);

        if(auth.checkLogin()){  //需要登录验证
            /**
             * 登录验证
             */
            String token = request.getParameter("token");
            if(StringUtils.isBlank(token)){
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=utf-8");
                PrintWriter writer = response.getWriter();
                writer.append(JSONObject.toJSONString(Result.unDataResult("tokenFail","请重新登录。")));
                writer.flush();
                writer.close();
                return false;
            }
            String userIdStr = request.getParameter("userId");
            String requestVer = request.getParameter("requestVer");//发送请求的APP版本
            if(StringUtils.isNotBlank(token) && StringUtils.isNotBlank(userIdStr)){
                //新redis
//                Integer apiAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiAuthCheckRedisDbIndex"));//app存储在缓存中的位置11
                Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//新存储在缓存中的位置35
                String userToken = null;
                String tokenKey = userIdStr+"_token";
                //redis修改
                userToken = RedisUtil.getStrVal(tokenKey, apiNewAuthCheckRedisDbIndex);
                Map<String,Object> user = null;
                if(StringUtils.isBlank(userToken)){
                    //缓存中不存在Token，就从数据库中查询
                    user = userService.findById(TableList.USER,Integer.parseInt(userIdStr));
                    userToken = user.get("token")+"";
                }

                System.out.println("是否token正确："+token.equals(userToken));
                System.out.println("userId："+userIdStr);
                System.out.println("token："+token+", userToken"+userToken);

                if(token.equals(userToken)){
                    /**
                     * 实名验证
                     */
                    if(auth.checkVerify()){
                        String verifyKey = userIdStr+"_isAuth";
                        String isAuth = RedisUtil.getStrVal(verifyKey, apiNewAuthCheckRedisDbIndex);
                        if(StringUtils.isBlank(isAuth)){
                            //缓存中不存在，就从数据库中查询
                            //redis修改
                            isAuth = BaseUtil.objToStr(user.get("isAuth"), "F");
                        }
                        if( !"T".equalsIgnoreCase(isAuth)){
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json; charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.append(JSONObject.toJSONString(Result.unDataResult("verifyFail","您还未进行实名验证")));
                            writer.flush();
                            writer.close();
                            return false;
                        }
                    }
                    /**
                     * 验证请求合法性
                     */
                    if(auth.checkRequestLegal()){
                        String threshold = request.getParameter("threshold");//客户端计算的Key
                        String factor = request.getParameter("factor");//客户端上传的时间，例如20170831143600
                        if(StringUtils.isBlank(threshold) || StringUtils.isBlank(factor)){
                            System.out.println("不是最新版本， 用户Id"+userIdStr+" 时间："+ DateUtil.getCurrentDateTime("yyyy-MM-dd HH:mm:ss"));
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json; charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.append(JSONObject.toJSONString(Result.unDataResult("tokenFail","当前版本过低，请先到应用市场下载最新的版本")));
                            writer.flush();
                            writer.close();
                            return false;
                        }
                        if(TokenUtil.checkRequestLegal(userIdStr, factor, token, threshold)){
                            return true;
                        }else{
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json; charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.append(JSONObject.toJSONString(Result.unDataResult("userFail","请求非法!")));
                            writer.flush();
                            writer.close();
                            return false;
                        }

                    }
                    return true;
                }else{
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json; charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.append(JSONObject.toJSONString(Result.unDataResult("tokenFail","您的帐号在另一台设备登录，请重新登录。")));
                    writer.flush();
                    writer.close();
                    return false;
                }
            }
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.append(JSONObject.toJSONString(Result.unDataResult("userFail","用户信息验证失败，请重新登录")));
            writer.flush();
            writer.close();
            return false;
        }

        if (auth.checkOtherLegal()){
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.append(JSONObject.toJSONString(Result.unDataResult("Fail","非法请求")));
            writer.flush();
            writer.close();
            return true;
        }

        return super.preHandle(request, response, handler);
    }
}
