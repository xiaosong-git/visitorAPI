package com.goldccm.controller.router;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.router.IRouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 路由切换
 * @author: cwf
 * @create: 2019-12-30 17:43
 **/
@Controller
@RequestMapping("/router")
public class RouterController extends BaseController {
    @Autowired
    public IRouterService service;
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/{pageNum}/{pageSize}")
    @ResponseBody
    public Result router(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return service.router( paramMap,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.fail();
        }
    }
}
