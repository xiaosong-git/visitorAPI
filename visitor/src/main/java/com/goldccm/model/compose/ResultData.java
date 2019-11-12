package com.goldccm.model.compose;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/10 19:49
 */
public class ResultData extends Result {

    private Object data;  //数据

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 返回数据
     * @Date  2016/7/25 17:18
     * @author linyb
     */
    public static ResultData dataResult(String sign,String desc,Object data){
        ResultData result = new ResultData();
        Map map = new HashMap();
        map.put("sign",sign);
        map.put("desc",desc);
        result.setVerify(map);
        result.data = data;
        return result;
    }

    /**
     * 没有返回数据
     * @Date  2016/7/25 17:18
     * @author linyb
     */
    public static ResultData unDataResult(String sign,String desc){
        ResultData result = new ResultData();
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
    public static ResultData success(){
        return unDataResult("success","操作成功");
    }
    /**
     * 操作成功
     * @Date  2016/7/25 17:18
     * @author linyb
     */
    public static ResultData fail(){
        return unDataResult("fail","操作失败");
    }
    /**
     * 在返回数据基础上多返回两个字段
     * @Date  2016/7/25 17:18
     * @author linyb
     */
    public static ResultData dataResultCodeType(String sign,String desc,String code,Integer type,Object data){
        ResultData resultData = dataResult(sign, desc, data);
        resultData.getVerify().put("code",code);
        resultData.getVerify().put("type",type);
        return resultData;
    }
    /**
     * 在返回数据基础上多返回两个字段
     * @Date  2016/7/25 17:18
     * @author linyb
     */
    public static ResultData dataResultCount(String sign,String desc,Object data,String count){

        ResultData resultData = dataResult(sign, desc, data);
        resultData.getVerify().put("count",count);
        System.out.println(resultData);
        return resultData;
    }
}
