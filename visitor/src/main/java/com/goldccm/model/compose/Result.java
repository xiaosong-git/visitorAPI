package com.goldccm.model.compose;


import com.alibaba.druid.support.json.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回结果
 * @Date  2016/7/25 16:41
 * @author linyb
 */
public class Result {

    protected String desc ="操作成功";   //返回提示信息
    protected String sign = "success"; //成功或者失败  fail
    protected Map<String,Object> verify;  //数据


    public Result(Integer result, String desc) {
        this.desc = desc;

    }
    public Result(String sign, String desc) {
        this.sign = sign;
        this.desc = desc;

    }

    public Result(){}

    public Map getVerify() {
        return verify;
    }
    public void setVerify(Map verify) {
        this.verify = verify;
    }


    /**
     * 没有返回数据
     * @Date  2016/7/25 17:18
     * @author linyb
     */
    public static Result unDataResult(String sign,String desc){
        Result result = new Result();
        Map map = new HashMap();
        map.put("sign",sign);
        map.put("desc",desc);
        result.verify = map;
        return result;
    }

    /**
     * 操作成功
     * @Date  2016/7/25 17:18
     * @author linyb
     */
    public static Result success(){
        return unDataResult("success","操作成功");
    }
    /**
     * 操作成功
     * @Date  2016/7/25 17:18
     * @author linyb
     */
    public static Result fail(){
        return unDataResult("fail","操作失败");
    }

    public static Result ResultCode(String sign,String desc,String code){
        Result result = new Result();
        Map map = new HashMap();
        map.put("sign",sign);
        map.put("desc",desc);
        map.put("code",code);
        result.verify = map;
        return result;
    }
    public static String ResultCodeType(String sign,String desc,String code,Integer type){
        Result result = ResultCode(sign, desc, code);
        result.getVerify().put("type",type);
        String s = JSONUtils.toJSONString(result.getVerify());
        return s;
    }

}