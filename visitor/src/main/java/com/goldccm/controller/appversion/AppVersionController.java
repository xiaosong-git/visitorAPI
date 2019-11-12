package com.goldccm.controller.appversion;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.appversion.IAppVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/21.
 */
@Controller
@RequestMapping("/appVersion")
public class AppVersionController extends BaseController {
    @Autowired
    private IAppVersionService appVersionService;



    /**
     * 安卓更新接口
     * @Author linyb
     * @Date 2017/5/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/updateAndroid/{channel}/{versionNum}")
    public @ResponseBody
    Result updateAndroid(@PathVariable String channel,@PathVariable String versionNum){
        return appVersionService.updateAndroid("android",channel,new Integer(versionNum));
    }

    /**
     * IOS更新接口
     * @Author linyb
     * @Date 2017/5/4 21:33
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/updateIOS")
    public @ResponseBody
    Result updateIOS(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return appVersionService.updateIos("ios",paramMap);
        }catch (Exception e){
            e.printStackTrace();
        }
       return null;
    }

}
