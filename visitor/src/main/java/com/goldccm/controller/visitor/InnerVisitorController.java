package com.goldccm.controller.visitor;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.visitor.IInnerVisitorService;
import com.goldccm.util.BaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 提供企业版接口
 * @author: cwf
 * @create: 2020-03-20 16:50
 **/
@Controller
@RequestMapping("/innerVisitor")
public class InnerVisitorController extends BaseController {
    @Autowired
    private IInnerVisitorService innerVisitorService;

    /**
     * 企业用户访问
     *
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/innerVisitRequest")
    @ResponseBody
    public Result innerVisitRequest(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = getParamsToMap(request);
            return this.innerVisitorService.innerVisitRequest(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }

    }

    /**
     * 企业用户回应访问
     *
     * @param request
     * @return
     */
    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/innerVisitResponse")
    @ResponseBody
    public Result innerVisitResponse(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = getParamsToMap(request);
            return this.innerVisitorService.innerVisitResponse(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

    @AuthCheckAnnotation(checkLogin = false)
    @RequestMapping("/sendPhotos")
    @ResponseBody
    public String sendPhotos(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = getParamsToMap(request);
            String o = BaseUtil.objToStr(paramMap.get("innerUrl"), "");
            return this.innerVisitorService.sendPhotos(o);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}

