package com.goldccm.service.visitor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Constant;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.visitor.IPushService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.RedisUtil;
import com.goldccm.util.SSLClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: goldccm
 * @description: 各种推送相关代码
 * @author: cwf
 * @create: 2020-04-01 17:34
 **/
@Service("pushService")
public class PushServiceImpl extends BaseServiceImpl implements IPushService {
    Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);
@Override
    public void wx_push( String openid, String title, String name, String phone, String startDate,String reason, String remarkValue,String url) throws Exception {

          JSONObject  jsonObject = new JSONObject();
          if (url!=null){
              jsonObject.put("url", openid);
          }
        jsonObject.put("touser", openid);   // openid
        if (Constant.IS_DEVELOP) {//测试环境
            jsonObject.put("template_id", "I-nNgggadJrZcZmkJzgxMVOptw4tf2MD9NKgYhWnCdM");
        } else {//生产环境
            jsonObject.put("template_id", "2UBJNiTiPPQTlwu2PHxtbCKhqao3Ix1I8mjGPBIWnUU");
        }
        JSONObject data = new JSONObject();
        JSONObject first = new JSONObject();
        first.put("value", title);
        first.put("color", "#173177");
        JSONObject keyword1 = new JSONObject();
        keyword1.put("value", name);
        keyword1.put("color", "#173177");
        JSONObject keyword2 = new JSONObject();
        keyword2.put("value", phone);
        keyword2.put("color", "#173177");
        JSONObject keyword3 = new JSONObject();
        keyword3.put("value", startDate);
        keyword3.put("color", "#173177");
        JSONObject keyword4 = new JSONObject();
        keyword4.put("value", reason);
        keyword4.put("color", "#173177");
        JSONObject remark = new JSONObject();
        remark.put("value", remarkValue);
        remark.put("color", "#173177");

        data.put("first", first);
        data.put("keyword1", keyword1);
        data.put("keyword2", keyword2);
        data.put("keyword3", keyword3);
        data.put("keyword4", keyword4);
        data.put("remark", remark);

        jsonObject.put("data", data);
        HttpClient httpClient = new SSLClient();
        String accessToken = RedisUtil.getStrVal("accessToken", 2);
        HttpPost postMethod = new HttpPost("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken);

        StringEntity entityStr = new StringEntity(JSON.toJSONString(jsonObject), HTTP.UTF_8);
        entityStr.setContentType("application/json");
        postMethod.setEntity(entityStr);
        HttpResponse resp = httpClient.execute(postMethod);
        int statusCode = resp.getStatusLine().getStatusCode();
        if (200 == statusCode) {
            String str = EntityUtils.toString(resp.getEntity(), HTTP.UTF_8);
            JSONObject resutlJson = JSONObject.parseObject(str);
            Map resultMap = JSON.parseObject(resutlJson.toString());
            Integer errcode = BaseUtil.objToInteger(resultMap.get("errcode"), 2333);
            String errmsg = BaseUtil.objToStr(resultMap.get("errmsg"), "");
            if (errcode == 0) {

                // 发送成功
                logger.info("发送微信模板成功，url:{}",url);
            } else {
                // 发送失败
                logger.info("发送失败：" + errmsg);
            }
        }
    }


}
