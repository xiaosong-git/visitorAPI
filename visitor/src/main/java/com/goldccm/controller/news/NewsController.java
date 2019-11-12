package com.goldccm.controller.news;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.Status;
import com.goldccm.service.news.INewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 新闻
 * @Author LZ
 * @Date 2018-1-22
 */
@Controller
@RequestMapping("/news")
public class NewsController extends BaseController {

    @Autowired
    private INewsService newsService;

    /**
     * 获取新闻信息
     * @Author LZ
     * @Date 2017/4/16 10:10
     */
    @AuthCheckAnnotation(checkLogin = false, checkVerify = false, checkRequestLegal = false)
    @RequestMapping("/list/{pageNum}/{pageSize}")
    @ResponseBody
    public Result list(HttpServletRequest request, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            return newsService.findByStatus(Status.APPLY_STATUS_NORMAL, pageNum, pageSize);
        }catch (Exception e){
            e.printStackTrace();
            return Result.unDataResult("fail", "系统异常");
        }
    }
}
