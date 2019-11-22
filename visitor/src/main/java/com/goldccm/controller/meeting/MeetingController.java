package com.goldccm.controller.meeting;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.meeting.IMeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/meeting")
public class MeetingController extends BaseController {
    @Autowired
    private IMeetingService meetingService;
    /**
     * 会议室总览
     * @param request
     * @param pageNum
     * @param pageSize
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/5 17:34
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/list/{pageNum}/{pageSize}")
    @ResponseBody
    public Result list(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.getMeeting(paramMap,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 单个会议室预定情况
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/6 9:27
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/roomStatus")
    @ResponseBody
    public Result roomStatusList(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.getRoomStatus(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 预定会议室
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/6 9:27
     */
    @AuthCheckAnnotation(checkLogin = true, checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/reserve")
    @ResponseBody
    public Result reserveMeeting(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.  reserveMeeting(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 我的会议室预定情况
     * @param request
     * @param pageNum
     * @param pageSize
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/6 9:36
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/myReserveList/{pageNum}/{pageSize}")
    @ResponseBody
    public Result myReserveMeetingList(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.getMyReserveMeeting(paramMap,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /** 
     * 取消会议室预定
     * @param request	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/6 9:40
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/cancle")
    @ResponseBody
    public Result cancleMeeting(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.cancleMeeting(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /** 
     * 重新预定会议室
     * @param request	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author chenwf 
     * @date 2019/8/6 9:43
     */
    @AuthCheckAnnotation(checkLogin = true, checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/reTryReserve")
    @ResponseBody
    public Result reTryReserve(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.reTryReserve(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 个人统计预定信息
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/6 9:43
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/statistics")
    @ResponseBody
    public Result statistics(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.statistics(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 增加会议室、茶室
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019-9-14 09:53:52
     */
    @AuthCheckAnnotation(checkLogin = true, checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/addRoom")
    @ResponseBody
    public Result addRoom(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.addRoom(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 获取从大楼编码获取会议室预定信息
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/5 17:34
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/getFromOrgCode/{pageNum}/{pageSize}")
    @ResponseBody
    public Result getFromOrgCode(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.getFromOrgCode(paramMap,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 确认拉取会议室预定信息
     * @param request id字符串，上位机编号
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/5 17:34
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/getFromOrgCodeConfirm")
    @ResponseBody
    public Result getFromOrgCodeConfirm(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return meetingService.getFromOrgCodeConfirm(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
}
