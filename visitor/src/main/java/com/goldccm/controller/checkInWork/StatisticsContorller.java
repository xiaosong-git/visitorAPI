package com.goldccm.controller.checkInWork;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.checkInWork.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 打卡统计控制器
 * @author: cwf
 * @create: 2019-11-22 17:44
 **/
@Controller
@RequestMapping("/statistics")
@ApiIgnore

public class StatisticsContorller extends BaseController {
    @Autowired
    private StatisticsService statisticsService;

    /**
     * 管理员查看，普通员工无法查看
     * 上下班统计
     * @param request
     * @return  normalCount--正常人数，exceptionCount--异常人数，lateCount--迟到人数，
     *          earlyCount--早退人数 absentCount--缺卡人数
     * @throws Exception
     * @author cwf
     * @date 2019/11/25 10:08
     */
    @AuthCheckAnnotation(checkLogin = false,checkVerify = false)
    @RequestMapping("/offDutyStatistics")
    @ResponseBody
    public Result offDutyStatistics(HttpServletRequest request){

        try {
            Map<String,Object> paramMap = getParamsToMap(request);
            return statisticsService.offDutyStatistics(paramMap);

        }catch (Exception e){
            e.printStackTrace();

            return Result.unDataResult("fail","未知错误，请联系管理员！");
        }

    }
}
