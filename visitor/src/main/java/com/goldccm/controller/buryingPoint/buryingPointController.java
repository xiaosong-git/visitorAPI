package com.goldccm.controller.buryingPoint;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.buryingPoint.BuryingPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 埋点
 * @author: cwf
 * @create: 2020-02-27 14:51
 **/
@Controller
@RequestMapping("/buryingPoint")
public class buryingPointController extends BaseController {
    @Autowired
    private BuryingPointService buryingPointService;
    /**
     * 保存规则
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/save")
    @ResponseBody
    public Result save(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return buryingPointService.save(paramMap);

        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }

}
