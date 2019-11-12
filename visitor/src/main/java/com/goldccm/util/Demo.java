package com.goldccm.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/8/5.
 */
public class Demo {
    public static void main(String[] args) throws Exception {


/*String idNO = "DD90DC8A5212371A72E3DCC21C030FD71B18EF4E9137156E";
String realName ="测试实名";
String idHandleImgUrl = "/1.png";
        String merchOrderId = OrderNoUtil.genOrderNo("V", 16);//商户请求订单号
        String merchantNo="100000000000004";//商户号
        String productCode="0003";//请求的产品编码
        String key="763C48125280498R4112863438309376";//秘钥
        String dateTime=DateUtil.getSystemTimeFourteen();//时间戳
        String certNo = DESUtil.encode(key,idNO);
        String userName =DESUtil.encode(key,realName);
        String photo=Base64.encode(FilesUtils.getPhoto("C:\\Users\\Administrator\\Desktop\\"+idHandleImgUrl));
        String signSource = merchantNo + merchOrderId + dateTime + productCode + key;//原始签名值
        String sign = MD5Util.MD5Encode(signSource);//签名值

        Map<String, String> map = new HashMap<String, String>();
        map.put("merchOrderId", merchOrderId);
        map.put("merchantNo", merchantNo);
        map.put("productCode", productCode);
        map.put("userName", userName);//加密
        map.put("certNo", certNo);// 加密);
        map.put("dateTime", dateTime);
        map.put("photo", photo);//加密
        map.put("sign", sign);
        String userIdentityUrl = "http://192.168.1.216:8085/wisdom/identity/fastIdentify?";
        ThirdResponseObj obj	=	HttpUtil.http2Nvp(userIdentityUrl,map,"UTF-8");
        String makePlanJsonResult = obj.getResponseEntity();
        JSONObject jsonObject = JSONObject.parseObject(makePlanJsonResult);
        Map resultMap = JSON.parseObject(jsonObject.toString());
        if ("1".equals(resultMap.get("bankResult").toString())){
            System.out.println("success");
        }else{
            System.out.println(resultMap.get("message").toString());
        }*/

        Calendar curr = Calendar.getInstance();
        curr.add(Calendar.DATE,-1);

        /*Calendar start = Calendar.getInstance();
        start.setTime(new SimpleDateFormat("yyyy-MM-dd").parse("2018-11-27"));
        System.out.println(curr.after(start));
        System.out.println("curr"+curr.getTime());
        System.out.println("start"+start.getTime());*/

        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        System.out.println(list.size());
    }
}


