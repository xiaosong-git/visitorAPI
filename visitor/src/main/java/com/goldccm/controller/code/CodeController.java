package com.goldccm.controller.code;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.code.ICodeService;
import com.goldccm.util.BaseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/10 19:31
 */
@Controller
@RequestMapping("/code")
@Api(tags="验证码",value = "验证码")
public class CodeController extends BaseController {

    @Autowired
    private ICodeService codeService;

    /**
     * 发送验证码
     * @Author linyb
     * @Date 2017/4/3 16:05
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @GetMapping("/sendCode/{phone}/{type}")
    @ResponseBody
    @ApiOperation(value = "发送验证码")
    public Result sendCode(@PathVariable String phone, @PathVariable Integer type) {
        return codeService.sendMsg(phone,type,null,null,null,null);
    }

    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @PostMapping("/verifyCode")
    @ResponseBody
    @ApiOperation(value = "验证验证码")
    public Result verifyCode(HttpServletRequest request) {
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            String phone = BaseUtil.objToStr(paramMap.get("phone"), null);
            String code = BaseUtil.objToStr(paramMap.get("code"), null);
            Integer type = BaseUtil.objToInteger(paramMap.get("type"), null);//2
            if (codeService.verifyCode(phone,code,type)){
                return Result.unDataResult("success","验证成功");
            }else {
                return Result.unDataResult("fail","验证失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
}
