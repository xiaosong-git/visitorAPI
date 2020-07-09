package com.goldccm.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class ThreadTestMethod implements Runnable {

    static long startTime=System.currentTimeMillis();//开始时间
    final static CountDownLatch countDownLatch=new CountDownLatch(500);

    public static void main(String[] args) throws Exception {
//        ThreadTestMethod count=new ThreadTestMethod();
//        for (int i=0;i<=0;i++){
//            Thread thread=new Thread(count);
//            thread.start();
//        }
//        countDownLatch.await();
//        System.out.println("执行总时间:"+(System.currentTimeMillis()-startTime));

        phoneResult("0C66395B3D6E36922ACBAC58178D7BB4","陈小明","user/1996/1594023280773.jpg");

//        String string= String.valueOf(System.currentTimeMillis())+new Random().nextInt(10);
//        System.out.println(string.toString());
//        System.out.println("mac is :--"+createSign(string));
//
//        String str = Configuration.GetImageStrFromPath("C:\\Users\\Administrator\\Desktop\\biddata\\63D2B2E647C03C149AB7FC45D93E9024.png", 30);
//        System.out.println(str.toString());
    }
    @Override
    public void run() {
        try {
//            auth();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            countDownLatch.countDown();
        }
    }
    public static void auth(String idNO,String realName,String idHandleImgUrl) throws Exception {
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
        HttpClient httpClient = new SSLClient();
        //HttpPost postMethod = new HttpPost("http://t.pyblkj.cn:8082/wisdom/entrance/pub");
        // HttpPost postMethod = new HttpPost("http://localhost:8080/wisdom-new/entrance/pub");
        HttpPost postMethod = new HttpPost("http://t.pyblkj.cn:8082/wisdom/entrance/pub");
        StringEntity entityStr= new StringEntity(JSON.toJSONString(itemJSONObj), HTTP.UTF_8);
        entityStr.setContentType("application/json");
        postMethod.setEntity(entityStr);
        HttpResponse resp = httpClient.execute(postMethod);
        int statusCode = resp.getStatusLine().getStatusCode();
        ThirdResponseObj responseObj = new ThirdResponseObj();

//        String path = "D:\\test.txt";
//        BufferedWriter out = new BufferedWriter(
//                new OutputStreamWriter(new FileOutputStream(path,true)));

        if (200 == statusCode) {
            responseObj.setCode("success");
            String str = EntityUtils.toString(resp.getEntity(), HTTP.UTF_8);
            responseObj.setResponseEntity(str);
            System.out.println(str);
            System.out.println();
//            out.write(str +"  "+Thread.currentThread().getName()+"\r\n");
        }else{
            responseObj.setCode(statusCode+"");
//            out.write("statusCode 不等于200"+statusCode+"  "+Thread.currentThread().getName()+"\r\n");
        }
//        out.close();
//        System.out.println("currThread " + Thread.currentThread().getName());
    }
    public static String phoneResult(String idNO,String realName,String idHandleImgUrl) throws Exception{
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


        Map<String, String> map = new HashMap<String, String>();
        map.put("merchOrderId", merchOrderId);
//        logger.info(merchOrderId);
        map.put("merchantNo", merchantNo);
        map.put("productCode", productCode);
        map.put("userName", userName);//加密
        map.put("certNo", certNo);// 加密);
        map.put("dateTime", dateTime);
        map.put("photo", photo);//加密
        map.put("sign", sign);
        ThirdResponseObj obj	=	HttpUtil.http2Nvp("http://47.99.1.34/wisdom/identity/fastIdentify",map,"UTF-8");
        String makePlanJsonResult = obj.getResponseEntity();
        JSONObject jsonObject = JSONObject.parseObject(makePlanJsonResult);
        Map resultMap = JSON.parseObject(jsonObject.toString());
        if ("1".equals(resultMap.get("bankResult").toString())){
            System.out.println(resultMap.get("message").toString());
            return "success";
        }else{
            System.out.println(resultMap.get("message").toString());
            return resultMap.get("message").toString();
        }
    }
    public static String createSign(String str) throws Exception {
        StringBuilder sb=new StringBuilder();
        sb.append("1000000007000010").append(str).append("9A0723248F21943R4208534528919630");
        String newSign = MD5Util.MD5Encode(sb.toString(),"UTF-8");
        return newSign;
    }

}
