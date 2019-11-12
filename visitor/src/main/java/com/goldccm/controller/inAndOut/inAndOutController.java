package com.goldccm.controller.inAndOut;
import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.inAndOutService.IInAndOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author chenwf
 * @date 2019/8/12 14:36
 */
@Controller
@RequestMapping("/inAndOut")
public class inAndOutController extends BaseController {
    @Autowired
    private IInAndOutService inAndOutService;

    /**
     * 下发人脸进出文件
     * @param request
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/8/25 16:11
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/getInOutTxt")
    @ResponseBody
    public Result getInOutTxt(HttpServletRequest request,HttpServletResponse response){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return inAndOutService.getInOutTxt(paramMap, response);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }

}
