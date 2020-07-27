package com.goldccm.controller.company;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.company.ICompanyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 15:50
 */
@Controller
@RequestMapping("/company")
@Api(tags="公司管理",value = "公司管理")
public class CompanyController extends BaseController {

    @Autowired
    private ICompanyService companyService;
    /**
     * 地址请求公司
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @PostMapping("/requestCompany")
    @ResponseBody
    @ApiOperation(value = "根据orgId请求公司",notes = "不知道干嘛用的接口，可能已经废除了")
    @ApiImplicitParam(name = "orgId",value="大楼id",dataType="int",paramType="query",defaultValue = "18",required = true)
    public Result requestCompany(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return companyService.requestCompany(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
}
