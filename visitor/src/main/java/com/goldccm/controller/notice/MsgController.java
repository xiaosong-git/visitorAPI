package com.goldccm.controller.notice;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.notice.IMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/5/8 15:36
 */
@Controller
@RequestMapping("/msg")
public class MsgController extends BaseController {

    @Autowired
    private IMsgService msgService;

    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("getMsg")
    public @ResponseBody  Result getMsg(HttpServletRequest request){
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return msgService.getMsg(paramMap);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail","系统异常");
        }
    }

}
