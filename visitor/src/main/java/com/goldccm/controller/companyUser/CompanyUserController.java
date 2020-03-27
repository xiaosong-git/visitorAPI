package com.goldccm.controller.companyUser;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.companyUser.ICompanyUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
@RequestMapping("/companyUser")
public class CompanyUserController extends BaseController {

    @Autowired
    private ICompanyUserService companyUserService;

    /**
     * 未确认记录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
        @RequestMapping("/findApplying")
    @ResponseBody
    public Result findApplying(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return companyUserService.findApplying(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 修改状态
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/updateStatus")
    @ResponseBody
    public Result updateStatus(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return companyUserService.updateStatus(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 全部初始化数据
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/findApplySuc")
    @ResponseBody
    public Result findApplySuc(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return companyUserService.findApplySuc(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    
    /**
     * 确认记录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/findApplySucOrg")
    @ResponseBody
    public Result findApplySucByOrg(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return companyUserService.findApplySucByOrg(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 确认记录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/newFindApplySucOrg")
    @ResponseBody
    public Result newFindApplySucOrg(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return companyUserService.newFindApplySucOrg(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 确认大楼全部记录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/findApplyAllSucOrg")
    @ResponseBody
    public Result findApplyAllSucByOrg(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return companyUserService.findApplyAllSucByOrg(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 确认大楼全部记录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/newFindApplyAllSucOrg")
    @ResponseBody
    public Result newFindApplyAllSucByOrg(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return companyUserService.newFindApplyAllSucOrg(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 查询访客所拥有的公司
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/findVisitComSuc")
    @ResponseBody
    public Result findvisitApplySuc(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            Map<String,Object> newMap = new HashMap<>();
            //将访客id重命名调用公司
            newMap.put("userId",paramMap.get("visitorId"));
            return companyUserService.findApplySuc(newMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
}
