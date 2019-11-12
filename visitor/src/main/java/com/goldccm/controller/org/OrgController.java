package com.goldccm.controller.org;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.org.IOrgService;
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
@RequestMapping("/org")
public class OrgController extends BaseController {

    @Autowired
    private IOrgService orgService;
    /**
     * 地址请求大厦
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/requestMansion")
    @ResponseBody
    public Result requestMansion(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return orgService.requestMansion(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
}
