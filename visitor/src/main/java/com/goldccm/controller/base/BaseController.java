package com.goldccm.controller.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 基础controller
 *
 * @author linyb
 * @Date 2016/7/25 17:01
 */
public class BaseController {
    Logger logger = LoggerFactory.getLogger(BaseController.class);
    /**
     * 把请求参数 转换为map
     *
     * @Date 2016/7/25 17:03
     * @Author linyb
     */
    public Map<String, Object> getParamsToMap(HttpServletRequest request) {

        Map<String,Object>  res = new HashMap<String,Object>();
        Map<String,String[]>  parameter = request.getParameterMap();
        Iterator<String> it = parameter.keySet().iterator();
        StringBuffer str=new StringBuffer();
        while(it.hasNext()){
            String key = it.next();
            String[]  val = parameter.get(key);
            if(val!=null&&val.length>0){
                if(val[0]!=null&&!"".equals(val[0])){
                    res.put(key, val[0].trim());
                    str.append(key+"="+ val[0].trim()+"\n");

                }
            }
        }
        logger.info(str.toString());
        return res;
    }

}