package com.goldccm.controller.checkInWork;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.checkInWork.CheckInWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
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
    /**
     * 获取单人打卡数据
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/gain/one")
    @ResponseBody
    public Result gainWorkOne(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.gainWorkOne(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }
    /**
     * 打卡
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/save/work")
    @ResponseBody
    public Result saveWork(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.saveWork(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }

    /**
     * 统计
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/gain/month/statistics")
    @ResponseBody
    public Result gainMonthStatistics(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.gainMonthStatistics(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }

    /**
     * 统计
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/effective")
    @ResponseBody
    public Result effective(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.effective(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }
}
