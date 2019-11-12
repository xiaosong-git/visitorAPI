package com.goldccm.controller.company;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.company.ICompanyService;
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
@RequestMapping("/company")
public class CompanyController extends BaseController {

    @Autowired
    private ICompanyService companyService;
    /**
     * 地址请求公司
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/requestCompany")
    @ResponseBody
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
