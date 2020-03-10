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
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class ThreadTestMethod implements Runnable {

    static long startTime=System.currentTimeMillis();//开始时间
    final static CountDownLatch countDownLatch=new CountDownLatch(500);

    public static void main(String[] args) throws Exception {
        ThreadTestMethod count=new ThreadTestMethod();
        for (int i=0;i<=2;i++){
            Thread thread=new Thread(count);
            thread.start();
        }
        countDownLatch.await();
        System.out.println("执行总时间:"+(System.currentTimeMillis()-startTime));

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
            String string= String.valueOf(System.currentTimeMillis())+new Random().nextInt(10);
            JSONObject itemJSONObj =new JSONObject();
            itemJSONObj.put("custid", "1000000007");//账号
            itemJSONObj.put("txcode", "tx00010");//交易码
            itemJSONObj.put("productcode", "000010");//业务编码
            itemJSONObj.put("serialno", string);//流水号
            itemJSONObj.put("mac", createSign(string));//随机状态码   --验证签名  商户号+订单号+时间+产品编码+秘钥
            itemJSONObj.put("userName", "吴桂民");
            itemJSONObj.put("certNo", "350424199009031238");
            itemJSONObj.put("imgData", Configuration.GetImageStrFromPath("C:\\Users\\Administrator\\Desktop\\biddata\\63D2B2E647C03C149AB7FC45D93E9024.png",30));

            HttpClient httpClient = new SSLClient();
            //HttpPost postMethod = new HttpPost("http://t.pyblkj.cn:8082/wisdom/entrance/pub");
           // HttpPost postMethod = new HttpPost("http://localhost:8080/wisdom-new/entrance/pub");
            HttpPost postMethod = new HttpPost("http://yzb.free-http.svipss.top/wisdom-new/entrance/pub");
            StringEntity entityStr= new StringEntity(JSON.toJSONString(itemJSONObj), HTTP.UTF_8);
            entityStr.setContentType("application/json");
            postMethod.setEntity(entityStr);
            HttpResponse resp = httpClient.execute(postMethod);
            int statusCode = resp.getStatusLine().getStatusCode();
            ThirdResponseObj responseObj = new ThirdResponseObj();

            String path = "E:\\zhong_dun\\test.txt";
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(path,true)));

            if (200 == statusCode) {
                responseObj.setCode("success");
                String str = EntityUtils.toString(resp.getEntity(), HTTP.UTF_8);
                responseObj.setResponseEntity(str);
                out.write(str.toString()+"  "+Thread.currentThread().getName()+"\r\n");
            }else{
                responseObj.setCode(statusCode+"");
                out.write("statusCode 不等于200"+statusCode+"  "+Thread.currentThread().getName()+"\r\n");
            }
            out.close();
            System.out.println("currThread " + Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            countDownLatch.countDown();
        }
    }

    public static String createSign(String str) throws Exception {
        StringBuilder sb=new StringBuilder();
        sb.append("1000000007000010").append(str).append("9A0723248F21943R4208534528919630");
        String newSign = MD5Util.MD5Encode(sb.toString(),"UTF-8");
        return newSign;
    }
}
