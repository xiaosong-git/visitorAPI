package com.goldccm.aop;

import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.ParamMap;
import com.goldccm.model.compose.Result;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.RedisUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

@Aspect
@Component
public class LoggingAspect extends BaseController {

private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

//    @Pointcut("execution(* com.goldccm.controller..*.*(..) ) && !execution(* com.goldccm.controller.user.UserController.verify(..) ) ")

    //定义一个返回值为void、方法体为空的方法来命名切入点
    /**
     * 计算每个接口的时间
     */
    @Pointcut("execution(* com.goldccm.controller..*.*(..) ) ")
    private void countCut(){

    }

    //重复按钮判断
    @Pointcut("execution(* com.goldccm.controller.user.UserController.verify(..))")
    private void retryCut(){

    }
    /**
      * 统计方法执行耗时Around环绕通知
      * @param joinPoint
      * @return
      */
    @Around("countCut()")
    @Order(2)
    public Object loggingAround(ProceedingJoinPoint pjp)  throws Throwable {
        //获取request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, Object> paramsMap = getParamsToMap(request);

        // 定义返回对象、得到方法需要的参数
        Signature sig = pjp.getSignature();
        MethodSignature msig = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        msig = (MethodSignature) sig;
//        Object target = pjp.getTarget();
//        Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        long startTime = System.currentTimeMillis();
            // 调用接口
//            logger.info("======>请求{}接口开始 开始时间：{}", msig,startTime);
        Object result = pjp.proceed();
        long endTime = System.currentTimeMillis();
            logger.info("======>请求{}接口完成,耗时:{}",msig ,(endTime - startTime)+"ms");
            return result;
        }

    /**
     *  重复提交控制
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("retryCut()")
    @Order(1)
    public Object Around(ProceedingJoinPoint pjp)  throws Throwable {
        //获取request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, Object> paramsMap = getParamsToMap(request);

        String userId = BaseUtil.objToStr(paramsMap.get("userId"), "");
        String token = BaseUtil.objToStr(paramsMap.get("token"), "");

        boolean lock = RedisUtil.tryGetDistributedLock(30, userId + token, token, 30);
        logger.info("是否锁定状态={},redis key为：",!lock,userId + token);
        if (!lock){
            return Result.unDataResult("fail","系统处理中。。。请勿重复点击！");
        }
//        Object target = pjp.getTarget();
//        Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        Object result = pjp.proceed();
        boolean b1 = RedisUtil.releaseDistributedLock(30, userId + token, token);
//        logger.info("boolean1={}",b1);
        return result;
    }
}

