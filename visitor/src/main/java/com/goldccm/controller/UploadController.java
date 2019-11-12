package com.goldccm.controller;


import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.IBaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 13:36
 */
@Controller
public class UploadController extends BaseController {

    @Autowired
    private IBaseService baseService;
    /**
     * 上传头像
     * @Author Linyb
     * @Date 2016/8/8 21:55
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping(value = "/uploadVerify",method = RequestMethod.POST)
    @ResponseBody
    public Result uploadVerify(String realName, String idCard , Integer userId, HttpServletRequest request){


        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String url = request.getSession().getServletContext().getRealPath("upload"+ File.separator+"verify");
        File file = new File(url);
        if(!file.exists()){
            file.mkdirs();
        }
        Map<String,Object> userParams = new HashMap<String, Object>();
        userParams.put("id",userId);
        if(StringUtils.isBlank(realName)){
            return new Result(500,"真实姓名不能为空");
        }
        try {
            userParams.put("realName",new String(realName.getBytes("iso8859-1"),"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(StringUtils.isBlank(idCard)){
            return new Result(500,"身份证号不能为空");
        }
        userParams.put("idCard",idCard);

        Iterator<String> iter = multipartRequest.getFileNames();
        while (iter.hasNext()) {
            /**页面控件的文件流**/
            /**涉及文件**/
            String key = iter.next();
            MultipartFile multipartFile = multipartRequest.getFile(key);
            String suffix = multipartFile.getOriginalFilename().substring
                    (multipartFile.getOriginalFilename().lastIndexOf("."));  //后缀
            String name = System.currentTimeMillis()+suffix;
            File last = new File(url+File.separator+name);
            try {
                multipartFile.transferTo(last);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if("cardFront".equals(key)){
                userParams.put("cardFront","/upload/verify/"+name);
            }else if("cardBack".equals(key)){
                userParams.put("cardBack","/upload/verify/"+name);
            }else if("cardHand".equals(key)){
                userParams.put("cardHand","/upload/verify/"+name);
            }
        }
        userParams.put("verify",1);
        int i = baseService.update(TableList.USER,userParams);
        return  i > 0 ? Result.success() : Result.fail() ;
    }

    /**
     * 上传头像
     * @Author Linyb
     * @Date 2016/8/8 21:55
     */
    @AuthCheckAnnotation(checkLogin = true,checkVerify = false, checkRequestLegal = true)
    @RequestMapping(value = "/uploadVerify/{userId}",method = RequestMethod.POST)
    @ResponseBody
    public Result uploadVerify(@PathVariable Integer userId, HttpServletRequest request){

        /**涉及文件**/
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String url = request.getSession().getServletContext().getRealPath("upload"+ File.separator+"verify");
        File file = new File(url);
        if(!file.exists()){
            file.mkdirs();
        }
        Map<String,Object> userParams = new HashMap<String, Object>();
        userParams.put("id",userId);


        Iterator<String> iter = multipartRequest.getFileNames();
        String path = "";
        while (iter.hasNext()) {
            /**页面控件的文件流**/
            String key = iter.next();
            MultipartFile multipartFile = multipartRequest.getFile(key);
            String suffix = multipartFile.getOriginalFilename().substring
                    (multipartFile.getOriginalFilename().lastIndexOf("."));  //后缀
            String name = System.currentTimeMillis()+suffix;
            File last = new File(url+File.separator+name);
            try {
                multipartFile.transferTo(last);
            } catch (IOException e) {
                e.printStackTrace();
            }
            path = "/upload/verify/"+name;
            if("cardFront".equals(key)){
                userParams.put("cardFront",path);
            }else if("cardBack".equals(key)){
                userParams.put("cardBack",path);
            }else if("cardHand".equals(key)){
                userParams.put("cardHand",path);
            }
            userParams.put(key,path);
        }
        int i = baseService.update(TableList.USER,userParams);
        return  i > 0 ? new Result(200,path) : Result.fail() ;
    }
}
