package com.goldccm.controller.notice;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.adBanner.IAdBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author linyb
 * @Date 2017/5/8 15:36
 */
@Controller
@RequestMapping("/banner")
public class BannerController extends BaseController {

    @Autowired
    private IAdBannerService adBannerService;

    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("")
    public @ResponseBody  Result list(){
        try {
            return adBannerService.list();
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail","系统异常");
        }
    }
}
