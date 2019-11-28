package com.goldccm.controller.user;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.code.ICodeService;
import com.goldccm.service.user.IUserAccountService;
import com.goldccm.service.user.IUserService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.ConsantCode;
import com.sun.tools.javadoc.Start;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 15:50
 */
@Controller
@RequestMapping("/user")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class UserController extends BaseController {

    @Autowired
    private ICodeService codeService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserAccountService userAccountService;
    /**
     * 注册
     * @Author linyb
     * @Date 2017/4/3 16:44
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/register")
    @ResponseBody
    public Result register(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userService.registerOrigin(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail","系统异常");
        }
    }

    /**
     * 获取用户信息（通过UserId,Token）
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/getUser")
    @ResponseBody
    public Result getUser(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),0);
            Map<String,Object> user = userService.getUserByUserToken(userId, BaseUtil.objToStr(paramMap.get("token"),null));
            return user == null ? Result.unDataResult("fail","找不到用户的信息") : ResultData.dataResult("success","获取成功",user);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult(ConsantCode.FAIL, "系统异常");
        }
    }

    /**
     * 账号，密码登录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/login")
    @ResponseBody
    public Result login(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            paramMap.remove("token");
            if(paramMap.get("code") != null){
                return userService.loginByVerifyCode(paramMap);
            }
            return userService.login(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult(ConsantCode.FAIL, "系统异常");
        }
    }
    /**
     * 忘记密码
     * @Author linyb
     * @Date 2017/4/3 16:44
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/forget")
    @ResponseBody
    public Result forget(HttpServletRequest request){
        Map<String,Object> paramMap = getParamsToMap(request);
        paramMap.remove("token");
        String code = paramMap.get("code")+"";//短信验证码
        String phone = paramMap.get("phone")+"";//手机号
        if(userService.verifyPhone(phone)){
            return new Result(500,"手机号未注册");
        }

        boolean flag = codeService.verifyCode(phone,code,1);
        if(!flag){
            return new Result(500,"验证码错误");
        }
        paramMap.remove("code");
        /**
         * 修改密码
         */
        Map<String,Object> user = userService.getUserByPhone(phone);
        paramMap.put("password", paramMap.get("password")+"");
        paramMap.put("id", Integer.parseInt(user.get("id")+""));
        return userService.update(TableList.USER,paramMap) > 0 ? Result.success() : Result.fail();
    }

    /**
     * 修改账号
     * @Author linyb
     * @Date 2017/4/3 16:44
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/updatePhone")
    @ResponseBody
    public Result updatePhone(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userService.updatePhone(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 修改密码
     * @Author linyb
     * @Date 2017/4/3 16:44
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/updatePassword")
    @ResponseBody
    public Result updatePassword(HttpServletRequest request){
        Map<String,Object> paramMap = getParamsToMap(request);
        paramMap.remove("token");
        Integer userId = Integer.parseInt(paramMap.get("userId")+"");

        Map<String,Object> user = userService.findById(TableList.USER,userId);
        String newPassword = paramMap.get("newPassword")+"";
        String oldPassword = paramMap.get("oldPassword")+"";
        String userPassword = user.get("password")+"";
        if(oldPassword.equals(userPassword)){
            Map<String,Object> userUpdate = new HashMap<String, Object>();
            userUpdate.put("id",userId);
            userUpdate.put("password",newPassword);
            userService.update(TableList.USER,userUpdate);
            return Result.success();
        }else{
            return  new Result(500,"旧密码错误");
        }
    }

    /**
     * 修改手势密码
     * @Author LZ
     * @Date 2017-7-26
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/updateGesturePwd")
    @ResponseBody
    public Result updateGesturePwd(HttpServletRequest request){
        Map<String,Object> paramMap = getParamsToMap(request);
        return userService.updateGesturePwd(paramMap);
    }

    /**
     * 判断用户是否已经实名认证
     * @Author linyb
     * @Date 2017/4/3 21:15
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/isVerify")
    @ResponseBody
    public Result isVerify(HttpServletRequest request){
        Map paramMap = getParamsToMap(request);
        boolean flag = this.userService.isVerify(Integer.valueOf(Integer.parseInt(paramMap.get("userId") + "")));
        return flag ? Result.unDataResult("success", "已经实名验证") : Result.unDataResult("fail", "还未实名验证");
    }
    /**
     * 实名认证
     * @Author linyb
     * @Date 2017/4/3 21:15
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/verify")
    @ResponseBody
    public Result verify(HttpServletRequest request){
        Map paramMap = getParamsToMap(request);
        return this.userService.verify(paramMap);
    }

    /**
     * 修改系统密码
     * @Author linyb
     * @Date 2017/4/3 21:15
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/update/sysPwd")
    @ResponseBody
    public Result updateSysPwd(HttpServletRequest request){
        Map<String,Object> paramMap = getParamsToMap(request);
        return userService.updateSysPwd(paramMap);
    }

    /**
     * 修改系统密码
     * @Author linyb
     * @Date 2017/4/3 21:15
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/forget/sysPwd")
    @ResponseBody
    public Result forgetSysPwd(HttpServletRequest request){
        Map<String,Object> paramMap = getParamsToMap(request);
        return userService.forgetSysPwd(paramMap);
    }

    /**
     * 设置手势密码
     * @Author linyb
     * @Date 2017/4/3 21:15
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/setGesturePwd")
    @ResponseBody
    public Result setGesturePwd(HttpServletRequest request){
        Map<String,Object> paramMap = getParamsToMap(request);
        return userService.setGesturePwd(paramMap);
    }

    /**
     * 修改昵称头像
     * @Author linyb
     * @Date 2017/4/3 21:15
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/nick")
    @ResponseBody
    public Result nick(HttpServletRequest request){
        Map<String,Object> paramMap = getParamsToMap(request);
        return userService.updateNick(paramMap);
    }

    /**
     * 查询登录密码是否正确
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/checkSysPwd")
    @ResponseBody
    public Result checkSysPwd(HttpServletRequest request){
        try {
            Long userId = Long.valueOf(request.getParameter("userId"));
            String pwd = request.getParameter("pwd");
            Map<String, Object> resultMap = new HashMap<String, Object>();
            if(StringUtils.isBlank(pwd)){
                return ResultData.unDataResult(ConsantCode.FAIL, "缺少待验证登录密码");
            }
            if(userAccountService.checkSysPwd(userId, pwd)){
                resultMap.put("isCorrectPwd", "T");
            }else{
                resultMap.put("isCorrectPwd", "F");
            }
            return ResultData.dataResult(ConsantCode.SUCCESS, "查询成功", resultMap);
        }catch (Exception e){
            e.printStackTrace();
            return ResultData.unDataResult(ConsantCode.FAIL,"系统异常");
        }
    }

    /**
     * 查询同一公司的员工
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/findCompanyId/{pageNum}/{pageSize}")
    @ResponseBody
    public Result findCompanyId(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userService.findCompanyId(paramMap,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 添加员工
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/addUser")
    @ResponseBody
    public Result addUser(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userService.addUser(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 删除员工
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/deleteUser")
    @ResponseBody
    public Result deleteUser(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userService.deleteUser(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 查询公司人员
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/findVisitorId")
    @ResponseBody
    public Result findVisitorId(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userService.findVisitorId(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 修改用户公司id，角色
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/updateCompanyIdAndRole")
    @ResponseBody
    public Result updateCompanyIdAndRole(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return userService.updateCompanyIdAndRole(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 测试人员修改实人认证专用接口
     * 只能修改小松人员
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/test/modify")
    @ResponseBody
    public Result modify(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return this.userService.modify(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }

    }
}
