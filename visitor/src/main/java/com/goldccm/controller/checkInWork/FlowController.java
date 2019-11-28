package com.goldccm.controller.checkInWork;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.checkInWork.FlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 流程控制
 * @author: cwf
 * @create: 2019-11-20 16:32
 **/
@Controller
@RequestMapping("/flow")
public class FlowController extends BaseController {

    @Autowired
    private FlowService flowService;
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/create")
    @ResponseBody
    public Result createFlow(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return flowService.createFlow(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/Check/{pageNum}/{pageSize}")
    @ResponseBody
    public Result CheckFlow(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return flowService.checkFlow(paramMap,pageNum,pageSize);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }


    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/approve")
    @ResponseBody
    public Result approveFlow(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return flowService.approveFlow(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }

    /**
     * 我审批的
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/myApprove/{pageNum}/{pageSize}")
    @ResponseBody
    public Result myApprove(HttpServletRequest request,@PathVariable Integer pageNum, @PathVariable Integer pageSize){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return flowService.myApprove(paramMap,  pageNum,  pageSize);

        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }
    /**
     * 根据单个编号查看审批情况
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/approveDetail")
    @ResponseBody
    public Result approveDetail(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return flowService.approveDetail(paramMap);

        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }

}
