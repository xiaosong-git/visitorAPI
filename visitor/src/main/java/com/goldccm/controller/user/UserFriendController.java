package com.goldccm.controller.user;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.user.IUserFriendService;
import com.goldccm.service.user.IUserService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.ConsantCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 15:50
 */
@Controller
@RequestMapping("/userFriend")
public class UserFriendController extends BaseController {

    @Autowired
    private IUserFriendService userFriendService;
    @Autowired
    private IUserService userService;
    /**
     * 好友数据
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/findUserFriend")
    @ResponseBody
    public Result findUserFriend(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userFriendService.findUserFriend(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 通过手机号查找用户
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/findPhone")
    @ResponseBody
    public Result findPhone(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userFriendService.findPhone(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 通过真实姓名查找用户
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/findRealName")
    @ResponseBody
    public Result findRealName(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userFriendService.findRealName(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 添加好友
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/addUserFriend")
    @ResponseBody
    public Result addUserFriend(HttpServletRequest request){
        //添加通讯录功能需要改变
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userFriendService.applyUserFriend(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 删除通讯录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/deleteUserFriend")
    @ResponseBody
    public Result deleteUserFriend(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userFriendService.deleteUserFriend(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 根据电话与姓名添加好友
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/addFriendByPhoneAndUser")
    @ResponseBody
    public Result addFriendByPhoneAndUser(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userFriendService.addFriendByPhoneAndRealName(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 对方申请我的好友列表
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/beAgreeingFriendList")
    @ResponseBody
    public Result beAgreeingFriendList(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
            if (userId==null){
                return  Result.unDataResult(ConsantCode.FAIL,"缺少参数!");
            }
            return userFriendService.findBeAgreeFriend(userId);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 通过验证好友信息
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/agreeFriend")
    @ResponseBody
    public Result agreeFriend(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return userFriendService.agreeFriend(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 查询手机号码是否为平台用户
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/findIsUserByPhone")
    @ResponseBody
    public Result findIsUserByPhone(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userService.findIsUserByPhone(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 查询我的好友与申请我的好友列表
     * @param request
     * @return
     *  update by cwf  2019/8/28 16:50 cause
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/findFriendApplyMe")
    @ResponseBody
    public Result findFriendApplyMe(HttpServletRequest request){
        try {
            System.out.println("---------------findFriendApplyMe--------------");
            Map<String,Object> paramMap = getParamsToMap(request);
            return userFriendService.findFriendApplyMe(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

}
