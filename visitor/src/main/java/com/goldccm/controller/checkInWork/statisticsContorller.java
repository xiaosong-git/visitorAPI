package com.goldccm.controller.checkInWork;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.checkInWork.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 打卡统计控制器
 * @author: cwf
 * @create: 2019-11-22 17:44
 **/
@Controller
@RequestMapping("/statistics")
public class statisticsContorller extends BaseController {
    @Autowired
    private StatisticsService statisticsService;

//    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
//    @RequestMapping("/create")
//    @ResponseBody
//    public Result createFlow(HttpServletRequest request){
//
//        try {
//            Map<String,Object> paramMap = getParamsToMap(request);
//            return StatisticsService.createFlow(paramMap);
//
//        }catch (Exception e){
//            e.printStackTrace();
//
//            return Result.unDataResult("fail","未知错误，请联系管理员！");
//        }
//
//    }
}
