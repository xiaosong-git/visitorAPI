package com.goldccm.controller.shortMessage;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.shortMessage.impl.ShortMessageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Controller
@RequestMapping("/ShortMessage")
public class ShortMessageController extends BaseController {
    @Autowired
    public ShortMessageServiceImpl shortMessageService;

    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/msg")
    @ResponseBody
    public Result request(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
             return shortMessageService.sendShortMessage( paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.fail();
        }
    }
}

