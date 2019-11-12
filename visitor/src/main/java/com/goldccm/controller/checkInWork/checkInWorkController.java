package com.goldccm.controller.checkInWork;

import com.alibaba.fastjson.JSONObject;
import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.checkInWork.CheckInWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @program: visitor
 * @description: 打卡控制器
 * @author: cwf
 * @create: 2019-11-04 09:43
 **/
@Controller
@RequestMapping("/checkInWork")
public class checkInWorkController extends BaseController {

    @Autowired
    private CheckInWorkService checkInWorkService;
    /**
     * 保存规则
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/save/group")
    @ResponseBody
    public Result saveGroup(HttpServletRequest request){

        try {

            return checkInWorkService.saveGroup(request);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }

}
