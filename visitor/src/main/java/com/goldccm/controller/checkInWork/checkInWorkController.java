package com.goldccm.controller.checkInWork;

import com.alibaba.fastjson.JSONObject;
import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.checkInWork.CheckInWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping("/work")
public class checkInWorkController extends BaseController {

    @Autowired
    private CheckInWorkService checkInWorkService;
    /**
     * 保存规则
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping(value = "addGroup", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Result addGroup(@RequestBody JSONObject jsonObject){

        try {

            return checkInWorkService.addGroup( jsonObject);

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
    @RequestMapping("/gainWork")
    @ResponseBody
    public Result gainWorkOne(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.gainWork(paramMap);

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
    @RequestMapping("/saveWork")
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
     * 打卡
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/outWork")
    @ResponseBody
    public Result outWork(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.outWork(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","系统错误！");
        }

    }
    /**
     * 获取规则目录
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/gainGroupIndex")
    @ResponseBody
    public Result gainGroupIndex(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.gainGroupIndex(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }
    /**
     * 获取规则详情
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/gainGroupDetail")
    @ResponseBody
    public Result gainGroupDetail(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.gainGroupDetail(paramMap);

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
     * 查询用户
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/companyUser")
    @ResponseBody
    public Result companyUser(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.companyUser(paramMap);

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
    /**
     * 统计
     * @Author cwf
     * @Date 2019/11/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/gainDay")
    @ResponseBody
    public Result gainDay(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return checkInWorkService.gainDay(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }
}
