package com.goldccm.controller.code;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.code.ICodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author linyb
 * @Date 2017/4/10 19:31
 */
@Controller
@RequestMapping("/code")
public class CodeController extends BaseController {

    @Autowired
    private ICodeService codeService;

    /**
     * 发送验证码
     * @Author linyb
     * @Date 2017/4/3 16:05
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/sendCode/{phone}/{type}")
    @ResponseBody
    public Result sendCode(@PathVariable String phone, @PathVariable Integer type) {
        return codeService.sendMsg(phone,type,null,null,null,null);
    }

}
