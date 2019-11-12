package com.goldccm.controller.notice;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.notice.INoticeService;
import com.goldccm.util.BaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/16 10:06
 */
@Controller
@RequestMapping("/notice")
public class NoticeController extends BaseController {

    @Autowired
    private INoticeService noticeService;

    /**
     * 获取公告信息
     * @Author linyb
     * @Date 2017/4/16 10:10
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/list/{pageNum}/{pageSize}")
    @ResponseBody
    public Result list(HttpServletRequest request, @PathVariable Integer pageNum,@PathVariable Integer pageSize){
        Map<String,Object> paramMap = getParamsToMap(request);
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),0);
        return noticeService.findNoticeByUser(userId,pageNum,pageSize);
    }
    /**
     * 获取所有上级组织的公告
     * @param request
     * @param pageNum
     * @param pageSize
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author chenwf
     * @date 2019/8/5 9:53
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/allList/{pageNum}/{pageSize}")
    @ResponseBody
    public Result allList(HttpServletRequest request, @PathVariable Integer pageNum,@PathVariable Integer pageSize){

        Map<String,Object> paramMap = getParamsToMap(request);
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),0);
        try {
            return noticeService.findBySidCompany(userId,pageNum,pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail();
        }
    }
}
