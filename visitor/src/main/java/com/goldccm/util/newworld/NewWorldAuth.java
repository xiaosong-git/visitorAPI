package com.goldccm.util.newworld;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SmUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.util.Base64;
import com.goldccm.util.DESUtil;
import com.goldccm.util.FilesUtils;
import com.goldccm.util.ThreadTestMethod;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author zengxq
 * @create 2020-07-07 21:28
 * @Description  网证组件测试类
 */
public class NewWorldAuth {
    static Logger logger = LoggerFactory.getLogger(NewWorldAuth.class);
    // 机构id
    public static final String ORG_ID = "1593768330660001";

    // 服务端数据加密公钥
    public static final String SERVER_KEY = "5175594dea89d55a";

    //接口地址
    public static final String SERVER_URL = "http://222.76.46.138:30003/door/server/v1/auth/validate";

    public static JSONObject sendPost(String number,String name,String photoData )  {
        // 请求header
//        Map<String, String> headers = new HashMap<>();
        String time=String.valueOf(System.currentTimeMillis());
        // 请求body报文
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("requestId", IdUtil.simpleUUID());
        jsonObject.put("idType","411");
        jsonObject.put("name",name);
        jsonObject.put("number",number);
        if (photoData!=null) {
            jsonObject.put("photoData", photoData);
        }
        String body =jsonObject.toJSONString();
//        System.out.println("客户端加密前发送数据：" + body);
        String  bodyStr = SmUtil.sm4(SERVER_KEY.getBytes()).encryptBase64(body);
//        System.out.println("客户端加密后发送数据：" + bodyStr);
        String sign= SignUtil.orgCheck(ORG_ID,time,bodyStr,SERVER_KEY);
//        System.out.println("sign：" + sign);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body1 = RequestBody.create(mediaType, bodyStr);
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .method("POST", body1)
                .addHeader("orgid", ORG_ID)
                .addHeader("sign", sign)
                .addHeader("time", time)
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .build();
        Response response = null;
        JSONObject returnObject = null;
        try {
            response = client.newCall(request).execute();
            String string = response.body().string();
            logger.info("服务端响应数据：" + string);
            // 解密响应数据
            returnObject=JSONObject.parseObject(string);
            return returnObject;

        } catch (IOException e) {
            logger.error("调用新大陆接口错误",e);
            returnObject.put("msg","系统错误");
            returnObject.put("code","-1");
            return returnObject ;
        }

    }

    public static void main(String[] args) throws Exception {
        String s="E68873C1AEE0B83BA357158D37A6D96683BD55AE5D5FF1BF";
        String key = "iB4drRzSrC";
        String decode = DESUtil.decode(key, s);
//        sendPost("陈维发","350121199306180330",Configuration.GetImageStrFromPath("http://47.98.205.206/imgserver/" + "user/125/1593398235082.jpg", 40));
//        JSONObject jsonObject = sendPost( "350121199306180330","陈维发", Base64.encode((FilesUtils.getImageFromNetByUrl("http://47.98.205.206/imgserver/" + "user/125/1594131603531.jpg"))));
        byte[] imageFromNetByUrl = FilesUtils.getImageFromNetByUrl("http://47.98.205.206/imgserver/" + "user/2311/1595465561486.jpg");
        logger.info("imageFromNetByUrl:{}",imageFromNetByUrl);
        byte[] data = FilesUtils.compressUnderSize(imageFromNetByUrl, 40960L);
        logger.info("data:{}",data);
        logger.info("压缩前后对比{}", Arrays.equals(imageFromNetByUrl,data));
        JSONObject jsonObject = sendPost(decode, "刘丹灵", Base64.encode(data));
//        JSONObject jsonObject = sendPost("350121199306180330", "陈维发", Base64.encode(data));
        if ("0".equals(jsonObject.getString("code"))){
            String data1=jsonObject.getString("data");
            if (StringUtils.isNotBlank(data1)){
                data1 = SmUtil.sm4(NewWorldAuth.SERVER_KEY.getBytes()).decryptStrFromBase64(data1);
                JSONObject value = JSON.parseObject(data1);
                System.out.println(data1);
                logger.info("data信息为{}",value.toJSONString());
                  String  bid = value.getString("bid");
                logger.info("服务端响应解密后数据：" + jsonObject);
            }
        }else{
            logger.info("失败原因：{}",jsonObject.getString("msg"));
        }
//        ThreadTestMethod.auth("35010219870905364X", "刘丹灵", Base64.encode(data));

//        ThreadTestMethod.phoneResult(decode,"易超","user/2021/1594200468327.jpg");
    }

}
