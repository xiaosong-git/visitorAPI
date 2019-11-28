package com.goldccm.controller.user;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.user.IUserAppRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * app角色菜单控制设计：
 * 1、检索登入人公司权限 2、检索登入人权限 3、登入人权限必须为公司权限的子集
 * @program: visitor
 * @description: app用户权限控制
 * @author: cwf
 * @create: 2019-09-14 10:00
 **/
@Controller
@RequestMapping("/userAppRole")
public class UserAppRoleController extends BaseController {

    Logger logger = LoggerFactory.getLogger(UserAppRoleController.class);
    @Autowired
    private IUserAppRoleService userAppRoleService;

    /**
     * 获取app登入人角色菜单
     *
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/getRoleMenu")
    @ResponseBody

    public Result getRoleMenu(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userAppRoleService.getRoleMenu(paramMap);
        }catch (Exception e){
            logger.error("UserAppRoleController.getRoleMenu fail！",e);
            return Result.unDataResult("fail","系统异常");
        }
    }

}
