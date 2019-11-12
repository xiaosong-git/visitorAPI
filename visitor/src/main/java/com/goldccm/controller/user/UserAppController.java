package com.goldccm.controller.user;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @program: visitor
 * @description: app相关
 * @author: cwf
 * @create: 2019-09-24 09:04
 **/
@Controller
@RequestMapping("/app")
public class UserAppController extends BaseController {
    @Autowired
    private IUserService userService;

    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/quit")
    @ResponseBody
    public Result appQuit(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userService.appQuit(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail","系统异常");
        }
    }

}
