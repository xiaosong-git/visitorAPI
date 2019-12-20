package com.goldccm.controller.foreign;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.visitor.IForeignService;
import com.goldccm.service.visitor.IVisitorRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 15:50
 */
@Controller
@RequestMapping("/foreign")
public class ForeignController extends BaseController {

    @Autowired
    private IVisitorRecordService visitorRecordService;
    @Autowired
    private IForeignService foreignService;
    /**
     * 访问我的人
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/findOrgCode/{pospCode}/{orgCode}/{pageNum}/{pageSize}")
    @ResponseBody
    public Result findOrgCode(HttpServletRequest request, @PathVariable String pospCode, @PathVariable String orgCode, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            return visitorRecordService.findOrgCode(pospCode,orgCode,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 访问我的人详情
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/findBySoleCode/{pospCode}/{orgCode}/{soleCode}/{visitId}")
    @ResponseBody
    public Result findBySoleCode(HttpServletRequest request, @PathVariable String pospCode, @PathVariable String orgCode, @PathVariable String soleCode, @PathVariable String visitId){
        try {
            return visitorRecordService.findBySoleCode(pospCode,orgCode,soleCode,visitId);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 确认访问数据
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/findOrgCodeConfirm/{pospCode}/{orgCode}/{isData}")
    @ResponseBody
    public Result findOrgCodeConfirm(HttpServletRequest request, @PathVariable String pospCode, @PathVariable String orgCode, @PathVariable String isData){
        try {
            return visitorRecordService.findOrgCodeConfirm(pospCode,orgCode,isData);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }


    /*
     *  接收单条扫描记录结果
     *  @param pospCode
     *  @param orgCode
     *  @param visitId
     *  @param inOrOut
     *  @param visitDate
     *  @param visitTime
     *
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping( value = "/uploadBySingle",method = RequestMethod.POST)
    @ResponseBody
    public Result uploadScanRecode(HttpServletRequest request,String pospCode,String orgCode,String visitId,String inOrOut,String visitDate,String visitTime) throws Exception {

        return visitorRecordService.uploadAccessRecord(pospCode,orgCode,visitId,inOrOut,visitDate,visitTime);

    }
    /*
     *  批量接收扫描记录结果
     *  @param pospCode
     *  @param orgCode
     *  @param visitId
     *  @param inOrOut
     *  @param visitDate
     *  @param visitTime
     *
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping( value = "/uploadByBatch",method = RequestMethod.POST)
    @ResponseBody
    public Result uploadScanBatchRecode(HttpServletRequest request ,@RequestParam Map<String,Object> params) throws Exception {
        JSONArray jsonArray = JSON.parseArray(params.get("raws").toString());

        for (int i=0;i<jsonArray.size();i++){
            JSONObject jsonObject = JSONObject.parseObject(jsonArray.get(i).toString());
            String pospCode =jsonObject.get("pospCode").toString();
            String orgCode =jsonObject.get("orgCode").toString();
            String visitId =jsonObject.get("visitId").toString();
            String inOrOut =jsonObject.get("inOrOut").toString();
            String visitDate =jsonObject.get("visitDate").toString();
            String visitTime =jsonObject.get("visitTime").toString();
            visitorRecordService.uploadAccessRecord(pospCode,orgCode,visitId,inOrOut,visitDate,visitTime);
        }
        return Result.unDataResult("success","数据发送成功!");
    }

    /**
     * 访问我的人新接口
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/newFindOrgCode/{pospCode}/{orgCode}/{pageNum}/{pageSize}")
    @ResponseBody
    public Result newFindOrgCode(HttpServletRequest request, @PathVariable String pospCode, @PathVariable String orgCode, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            return foreignService.FindOrgCode(pospCode,orgCode,pageNum,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    /**
     * 确认访问数据新接口
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/newFindOrgCodeConfirm/{pospCode}/{orgCode}/{idStr}")
    @ResponseBody
    public Result newFindOrgCodeConfirm(HttpServletRequest request, @PathVariable String pospCode, @PathVariable String orgCode, @PathVariable String idStr){
        try {
            return foreignService.newFindOrgCodeConfirm(pospCode,orgCode,idStr);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
}
