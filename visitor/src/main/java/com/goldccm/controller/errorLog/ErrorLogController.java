package com.goldccm.controller.errorLog;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.errorLog.IErrorLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Controller
@RequestMapping(value = "/errorLog")
public class ErrorLogController extends BaseController {

    @Resource(name = "errorLogService")
    private IErrorLogService errorLogService;

    @AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false,checkOtherLegal = true)
    @RequestMapping(value = "/uploadErrorLog")
    @ResponseBody
    public Result uploadErrorLog(HttpServletRequest request) throws Exception {
        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return errorLogService.saveErrorLog(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

}
