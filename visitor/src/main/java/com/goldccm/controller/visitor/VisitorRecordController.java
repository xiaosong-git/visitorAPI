package com.goldccm.controller.visitor;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.Result;
import com.goldccm.service.visitor.IVisitorRecordService;
import com.goldccm.util.BaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 15:50
 */
@Controller
@RequestMapping("/visitorRecord")
public class VisitorRecordController extends BaseController {


    @Autowired
    private IVisitorRecordService visitorRecordService;

    /**
     * 访问我的人
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/visitMyPeople/{pageNum}/{pageSize}")
    @ResponseBody
    public Result visitMyPeople(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.visitMyPeople(paramMap,pageNum,pageSize,Constant.RECORDTYPE_VISITOR);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 帮助审核
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/adoptionAndRejection")
    @ResponseBody
    public Result updateVisitorRecord(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.adoptionAndRejection(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 我访问的人
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/peopleIInterviewed/{pageNum}/{pageSize}")
    @ResponseBody
    public Result peopleIInterviewed(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.peopleIInterviewed(paramMap,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 我访问的人记录
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/peopleIInterviewedRecord/{pageNum}/{pageSize}")
    @ResponseBody
    public Result peopleIInterviewedRecord(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.peopleIInterviewedRecord(paramMap,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
//    /**
//     * 发起访问请求
//     * @param request
//     * @return
//     */
//    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
//    @RequestMapping("/visitRequest")
//    @ResponseBody
//    public Result visitRequest(HttpServletRequest request){
//        try {
//            Map<String,Object> paramMap = getParamsToMap(request);
//            return visitorRecordService.visitRequest(paramMap);
//        }catch (Exception e){
//            e.printStackTrace();
//            return Result.unDataResult("fail", "系统异常");
//        }
//    }
    /**
     * 访问我公司的其他人
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/visitMyCompany/{pageNum}/{pageSize}")
    @ResponseBody
    public Result visitMyCompany(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.visitMyCompany(paramMap,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 非好友访问
     * @param request
     * @return
     */
    //checkLogin需要为true
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping("/visit")
    @ResponseBody
    public Result visit(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.visit(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
//    /**
//     * 同意访问
//     * @param request
//     * @return
//     * update by cwf  2019/8/28 17:29 cause 网页端同意邀约
//     */
//    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
//    @RequestMapping("/visitAgree")
//    @ResponseBody
//    public Result visitAgree(HttpServletRequest request){
//        try {
//            Map<String,Object> paramMap = getParamsToMap(request);
//
//            System.out.println(paramMap);
//            System.out.println(paramMap.isEmpty());
//            return visitorRecordService.visitAgree(paramMap);
//        }catch (Exception e){
//            e.printStackTrace();
//            return Result.unDataResult("fail", "系统异常");
//        }
//    }
    /**
     *  网页端发送邀约信息
     * @param request
     * @return
     * update by cwf  2019/8/28 17:30 cause
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/msg")
    @ResponseBody
    public Result request(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.sendShortMessage( paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.fail();
        }
    }
    /**
     * 处理二维码生成网址
     * update by cwf  2019/8/28 17:30 cause
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/dealQrcodeUrl")
    @ResponseBody
    public Result dealQrcodeUrl(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
//            response.setHeader("Access-Control-Allow-Origin", "*");
//            response.setHeader("Cache-Control","no-cache");
            return visitorRecordService.dealQrcodeUrl( paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.fail();
        }
    }
    //我的邀约
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/inviteRecord/{pageNum}/{pageSize}")
    @ResponseBody
    public Result inviteRecord(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.myVisitOrInvite(paramMap,pageNum,pageSize,Constant.RECORDTYPE_INVITE);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    //邀约我的人
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/inviteMine/{pageNum}/{pageSize}")
    @ResponseBody
    public Result inviteMine(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.inviteMine(paramMap,pageNum,pageSize, Constant.RECORDTYPE_INVITE);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    //访问我的
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/visitRecord/{pageNum}/{pageSize}")
    @ResponseBody
    public Result visitRecord(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.myVisitOrInvite(paramMap,pageNum,pageSize, Constant.RECORDTYPE_VISITOR);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    //我的访问
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/myVisit/{pageNum}/{pageSize}")
    @ResponseBody
    public Result myVisit(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.myVisit(paramMap,pageNum,pageSize, Constant.RECORDTYPE_VISITOR);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /** 
     * 根据id查看访问记录 
     * @param request	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author cwf 
     * @date 2019/10/12 14:12
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/findRecordFromId")
    @ResponseBody
    public Result findRecordFromId(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.findRecordFromId(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /** 
     * 更新记录并下发用户公司角色
     * @param request	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author cwf 
     * @date 2019/10/12 14:18
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/updateRecord")
    @ResponseBody
    public Result updateRecord(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.updateRecord(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 推送或短信给公司管理者
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/10/12 14:18
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/visitForwarding")
    @ResponseBody
    public Result visitForwarding(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.visitForwarding(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 选择公司并且做权限判断
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/10/12 14:18
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/modifyCompanyFromId")
    @ResponseBody
    public Result modifyCompanyFromId(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.modifyCompanyFromId(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     *  回应邀约
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/12/5 9:48
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/visitReply")
    @ResponseBody
    public Result visitReply(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.visitReply(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     *  非好友邀约
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/12/5 9:48
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/inviteStranger")
    @ResponseBody
    public Result inviteStranger(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.InviteStranger(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    //根据条件判断访问我的人，邀约我的人。。。。
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/visitorList")
    @ResponseBody
    public Result visitorList(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.visitorList(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    //根据条件判断访问我的人，邀约我的人。。。。
    @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
    @RequestMapping("/visitorSucList")
    @ResponseBody
    public Result visitorSucList(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return visitorRecordService.visitorSucList(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    /**
     * 企业用户访问
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/innerVisitRequest")
    @ResponseBody
    public Result innerVisitRequest(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return this.visitorRecordService.innerVisitRequest(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }

    }
    /**
     * 企业用户回应访问
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/innerVisitResponse")
    @ResponseBody
    public Result innerVisitResponse(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return this.visitorRecordService.innerVisitResponse(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/sendPhotos")
    @ResponseBody
    public String sendPhotos(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            String  o = BaseUtil.objToStr(paramMap.get("innerUrl"),"");
            return this.visitorRecordService.sendPhotos(o);
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }
    //
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/findRecordUser")
    @ResponseBody
    public  Result findRecordUser(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return this.visitorRecordService.findRecordUser(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/findRecordUserDetail")
    @ResponseBody
    public  Result findRecordUserDetail(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return this.visitorRecordService.findRecordUserDetail(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
}
