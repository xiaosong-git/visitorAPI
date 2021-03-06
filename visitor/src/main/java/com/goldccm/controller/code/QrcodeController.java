package com.goldccm.controller.code;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.code.impl.QrcodeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 二维码
 * @Author linyb
 * @Date 2017/4/11 10:16
 */
@Controller
@RequestMapping("/qrcode")
@ApiIgnore
public class QrcodeController extends BaseController{
    @Autowired
    private QrcodeServiceImpl qrcodeService;


    @AuthCheckAnnotation(checkLogin = false, checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/qrcode")
    @ResponseBody
    public Result qrcode(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);

            return qrcodeService.getVisitQrcode(paramMap);

        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail","获取二维码失败！");
        }
    }
    //新东网二维码
    @AuthCheckAnnotation(checkLogin = false, checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/otherQrcode")
    @ResponseBody
    public Result otherQrcode(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            System.out.println("进入otherQrcode");
            return qrcodeService.dealQrcode(paramMap);

        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail","获取二维码失败！");
        }
    }

}
