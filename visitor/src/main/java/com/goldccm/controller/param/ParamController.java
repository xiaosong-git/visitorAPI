package com.goldccm.controller.param;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.service.param.IParamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author linyb
 * @Date 2017/4/30 21:23
 */
@Controller
@RequestMapping("/param")
public class ParamController extends BaseController{

    @Autowired
    private IParamService paramService;

    /**
     * 根据参数名获取对应的参数值
     * @Author linyb
     * @Date 2017/4/30 21:24
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/{paramName}")
    @ResponseBody
    public Result getParamByName(@PathVariable String paramName){

        String paramText =  paramService.findValueByName(paramName);
        return StringUtils.isNotBlank(paramText)
                ? ResultData.dataResult("success","获取成功",paramText)
                : Result.unDataResult("fail","参数名不存在");
    }

}
