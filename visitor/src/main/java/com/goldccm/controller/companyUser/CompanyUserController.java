package com.goldccm.controller.companyUser;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.companyUser.ICompanyUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
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
@Api(tags="公司员工",value = "公司员工")
public class CompanyUserController extends BaseController {

    @Autowired
    private ICompanyUserService companyUserService;

    /**
     * 查询公司员工待审核
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @PostMapping("/findApplying")
    @ResponseBody
    @ApiOperation(value = "查询公司员工",notes = "根据用户id查找待审核信息")
    @ApiImplicitParam(name = "userId",value="用户Id",dataType="int",paramType="query",defaultValue = "1",required = true)
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
    @PostMapping("/updateStatus")
    @ResponseBody
    @ApiOperation(value = "修改员工状态",notes = "根据公司id修改状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value="公司id",dataType="int",paramType="query",defaultValue = "18",required = true),
            @ApiImplicitParam(name = "status",value="状态：确认:applySuc/未确认:applying/确认不通过:applyFail",dataType="String",paramType="query",defaultValue = "applySuc",required = true)
    })
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
     * 单个用户公司确认数据
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @PostMapping("/findApplySuc")
    @ResponseBody
    @ApiOperation(value = "单个用户公司确认数据",notes = "查询用户公司信息")
    @ApiImplicitParam(name = "userId",value="用户id",dataType="int",paramType="query",defaultValue = "18",required = true)
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
     * 今天公司全部确认记录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @PostMapping("/findApplySucOrg")
    @ResponseBody
    @ApiOperation(value = "今天公司全部确认记录",notes = "查询公司全部员工确认记录")
    @ApiImplicitParam(name = "org_code",value="大楼编码",dataType="String",paramType="query",defaultValue = "hlxz",required = true)
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
     * 新按获取大楼员工当天员工确认数据
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @PostMapping("/newFindApplySucOrg")
    @ResponseBody
    @ApiOperation(value = "新按获取大楼员工当天员工确认数据",notes = "比旧接口多了rsq校验以及分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "org_code",value="大楼编码",dataType="String",paramType="query",defaultValue = "hlxz",required = true),
            @ApiImplicitParam(name = "posp_code",value="上位机编号",dataType="String",paramType="query",defaultValue = "18",required = true),
            @ApiImplicitParam(name = "mac",value="mac码，用于解密相当于sign",dataType="String",paramType="query",defaultValue = "applySuc",required = true),
            @ApiImplicitParam(name = "pageNum",value="页码",dataType="int",paramType="query",defaultValue = "1",required = true),
            @ApiImplicitParam(name = "pageSize",value="页数",dataType="int",paramType="query",defaultValue = "10",required = true)
    })
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
     * 旧确认大楼全部记录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @PostMapping("/findApplyAllSucOrg")
    @ResponseBody
    @ApiOperation(value = "旧确认大楼全部记录",notes = "旧确认大楼全部记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "org_code",value="大楼编码",dataType="String",paramType="query",defaultValue = "hlxz",required = true),
            @ApiImplicitParam(name = "pageNum",value="页码",dataType="int",paramType="query",defaultValue = "1",required = true),
            @ApiImplicitParam(name = "pageSize",value="页数",dataType="int",paramType="query",defaultValue = "50",required = true)
    })
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
     * 新确认大楼全部记录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @PostMapping("/newFindApplyAllSucOrg")
    @ResponseBody
    @ApiOperation(value = "新确认大楼全部记录",notes = "新确认大楼全部记录，比旧接口多了rsq校验以及分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orgCode",value="大楼编码",dataType="String",paramType="query",defaultValue = "hlxz",required = true),
            @ApiImplicitParam(name = "pospCode",value="上位机编号",dataType="String",paramType="query",defaultValue = "18",required = true),
            @ApiImplicitParam(name = "mac",value="mac码，用于解密相当于sign",dataType="String",paramType="query",defaultValue = "applySuc",required = true),
            @ApiImplicitParam(name = "pageNum",value="页码",dataType="int",paramType="query",defaultValue = "1",required = true),
            @ApiImplicitParam(name = "pageSize",value="页数",dataType="int",paramType="query",defaultValue = "10",required = true)
    })
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
    @PostMapping("/findVisitComSuc")
    @ResponseBody
    @ApiOperation(value = "查询访客所存在的公司",notes = "查询访客存在的公司")
    @ApiImplicitParam(name = "visitorId",value="访客id",dataType="int",paramType="query",defaultValue = "1",required = true)
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

    /**
     * 查询访客所拥有的公司
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @PostMapping("/applySucConfirm")
    @ResponseBody
    @ApiOperation(value = "确认下发成功",notes = "确认下发成功")
    @ApiImplicitParam(name = "idStr",value="以逗号隔开的id字符串",dataType="int",paramType="query",defaultValue = "1",required = true)
    public Result applySucConfirm(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            //将访客id重命名调用公司
            return companyUserService.applySucConfirm(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
}
