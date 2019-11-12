package com.goldccm.model.compose;


import java.util.HashMap;

/**
 * 构造Dao层的请求参数
 * @Author linyb
 * @Date 2016/12/7 15:58
 */
public class ParamMap extends HashMap<String,Object> {


    public ParamMap(){}
    /**
     * 构造Dao参数 填充
     * @Author linyb
     * @Date 2016/12/7 16:12
     * @param paramsObj 格式为：{{"key1",value1},{"key2",value2}......}
     */
    public ParamMap(Object[][] paramsObj){
        if(paramsObj!=null && paramsObj.length >0 ){
            for (Object[] temp: paramsObj) {
                if(temp != null && temp.length > 0){
                    Object keyObj = temp[0];
                    if(keyObj != null ){
                        String key = temp[0].toString();
                        this.put(key,temp[1]);
                    }else{
                        throw new RuntimeException("[ParamMap]构造Dao层请求参数的key出现null!");
                    }
                }
            }
        }
    }


}
