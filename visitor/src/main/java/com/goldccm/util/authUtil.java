package com.goldccm.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class authUtil {

    public static void main(String[] args) throws Exception {
        phoneResult("350121199306180330","陈维发","user/125/1596016858139.jpg");
    }

    public static JSONObject auth(String idNO,String realName,String idHandleImgUrl) throws Exception {
        String string= String.valueOf(System.currentTimeMillis())+new Random().nextInt(10);
        JSONObject itemJSONObj =new JSONObject();
        itemJSONObj.put("custid", "1000000007");//账号
        itemJSONObj.put("txcode", "tx00010");//交易码
        itemJSONObj.put("productcode", "000010");//业务编码
        itemJSONObj.put("serialno", string);//流水号
        itemJSONObj.put("mac", createSign(string));//随机状态码   --验证签名  商户号+订单号+时间+产品编码+秘钥
        String key="2B207D1341706A7R4160724854065152";
        String userName =DESUtil.encode(key,realName);
        String certNo = DESUtil.encode(key,idNO);
        itemJSONObj.put("userName", userName);
        itemJSONObj.put("certNo", certNo);
        itemJSONObj.put("imgData", idHandleImgUrl );

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body1 = RequestBody.create(mediaType, JSON.toJSONString(itemJSONObj));
        Request request = new Request.Builder()
                .url("http://t.pyblkj.cn:8082/wisdom/entrance/pub")
                .method("POST", body1)
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .build();
        Response response = null;
        JSONObject returnObject = null;
        try {
            response = client.newCall(request).execute();
             string = response.body().string();
            // 解密响应数据
            returnObject=JSONObject.parseObject(string);
          return returnObject;
        } catch (IOException e) {
            returnObject.put("msg","系统错误");
            returnObject.put("code","-1");
            return returnObject;
        }
    }
    public static JSONObject phoneResult(String idNO,String realName,String idHandleImgUrl) throws Exception{
        String merchOrderId = OrderNoUtil.genOrderNo("V", 16);//商户请求订单号
        String merchantNo="100000000000006";//商户号
        String productCode="0003";//请求的产品编码
        String key="2B207D1341706A7R4160724854065152";//秘钥
        String dateTime=DateUtil.getSystemTimeFourteen();//时间戳
        String certNo = DESUtil.encode(key,idNO);
//        logger.info("名称加密前为：{}",realName);
        String userName =DESUtil.encode(key,realName);
//        logger.info("名称加密后为：{}",userName);
//        String imageServerUrl = paramService.findValueByName("imageServerUrl");
        String photo=Base64.encode(FilesUtils.getImageFromNetByUrl("http://47.98.205.206/imgserver/"+idHandleImgUrl));
        String signSource = merchantNo + merchOrderId + dateTime + productCode + key;//原始签名值
        String sign = MD5Util.MD5Encode(signSource);//签名值


        Map<String, String> map = new HashMap<>();
        map.put("merchOrderId", merchOrderId);
//        logger.info(merchOrderId);
        map.put("merchantNo", merchantNo);
        map.put("productCode", productCode);
        map.put("userName", userName);//加密
        map.put("certNo", certNo);// 加密);
        map.put("dateTime", dateTime);
        map.put("photo", URLEncoder.encode(photo,"utf-8").replace("\\",""));
        map.put("sign", sign);
        String content = authUtil.packgeSign(map);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body1 = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
                .url("http://47.99.1.34/wisdom/identity/fastIdentify")
                .method("POST", body1)
                .addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .build();
        Response response = null;
        JSONObject returnObject = null;
        try {
            response = client.newCall(request).execute();
            String string = response.body().string();

            // 解密响应数据
            returnObject=JSONObject.parseObject(string);
            return returnObject;
        } catch (IOException e) {
            returnObject.put("msg","系统错误");
            returnObject.put("code","-1");
            return returnObject;
        }
    }
    /**打包成网页参数格式 */
    public static String packgeSign(Map<String,String> map){
        StringBuilder sb = new StringBuilder();
        for(String key:map.keySet()){
            String value = map.get(key);
            if ( value == null || "".equals(value.trim())){
                continue;
            }
            sb.append(key).append("=").append(value).append("&");
        }
        if(sb.length()>0){
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }
    public static String createSign(String str) throws Exception {
        StringBuilder sb=new StringBuilder();
        sb.append("1000000007000010").append(str).append("9A0723248F21943R4208534528919630");
        String newSign = MD5Util.MD5Encode(sb.toString(),"UTF-8");
        return newSign;
    }

}
