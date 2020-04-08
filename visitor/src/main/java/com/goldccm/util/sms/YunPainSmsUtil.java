package com.goldccm.util.sms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class YunPainSmsUtil {

	//查账户信息的http地址
    private static String URI_GET_USER_INFO = "https://sms.yunpian.com/v2/user/get.json";

    //智能匹配模板发送接口的http地址
    private static String URI_SEND_SMS = "https://sms.yunpian.com/v2/sms/single_send.json";

    //模板发送接口的http地址
    private static String URI_TPL_SEND_SMS = "https://sms.yunpian.com/v2/sms/tpl_single_send.json";

    //发送语音验证码接口的http地址
    private static String URI_SEND_VOICE = "https://voice.yunpian.com/v2/voice/send.json";

    //绑定主叫、被叫关系的接口http地址
    private static String URI_SEND_BIND = "https://call.yunpian.com/v2/call/bind.json";

    //解绑主叫、被叫关系的接口http地址
    private static String URI_SEND_UNBIND = "https://call.yunpian.com/v2/call/unbind.json";

    //编码格式。发送编码格式统一用UTF-8
    private static String ENCODING = "UTF-8";
    
    //public static String CHECK_CODE_TEMPLATE = "【妥妥管家】欢迎使用妥妥信用卡管家，您的手机验证码是code。本条信息无需回复";
    
    public static String CHECK_CODE_TEMPLATE = "【金银花】欢迎使用今日还款，您的手机验证码是code。本条信息无需回复";

    public static String CHECK_CODE_REGISTER = "【金银花】验证码：code，本次为注册帐号验证。请勿向任何人提供您收到的短信验证码。";

    public static String CHECK_CODE_FIND_SYSPWD = "【金银花】验证码：code，本次为找回登录密码验证。请勿向任何人提供您收到的短信验证码。";

    public static String CHECK_CODE_FIND_PAYPWD = "【金银花】验证码：code，本次为找回支付密码验证。请勿向任何人提供您收到的短信验证码。";

    public static String CHECK_CODE_BIND_CREDITCARD = "【金银花】验证码：code，本次为您进行信用卡绑定验证。请勿向任何人提供您收到的短信验证码。";

    public static String CHECK_CODE_BIND_DEBIT_CARD = "【金银花】验证码：code，本次为您进行储蓄卡绑定验证。请勿向任何人提供您收到的短信验证码。";

    public static String CHECK_CODE_VERIFY= "【金银花】验证码：code，本次为用户实名验证。请勿向任何人提供您收到的短信验证码。";

    public static Integer MSG_TYPE_TEMPLATE = 0;

    public static Integer MSG_TYPE_REGISTER = 1;//注册

    public static Integer MSG_TYPE_FIND_SYSPWD = 2;//找回登录密码

    public static Integer MSG_TYPE_FIND_PAYPWD = 3;//找回密码

    public static Integer MSG_TYPE_BIND_CREDITCARD = 4;//绑定信用卡卡

    public static Integer MSG_TYPE_DEBIT_CARD = 5;//绑定借记卡

    public static Integer MSG_TYPE_VERIFY = 6;//实人认证

    private final static String APIKEY = "e76ad24176a738b2cc561825b508624a";

    /**
     * 取账户信息
     *
     * @return json格式字符串
     * @throws IOException
     */

     public static String getUserInfo(String apikey) throws IOException, URISyntaxException {
         Map<String, String> params = new HashMap<String, String>();
         params.put("apikey", apikey);
         return post(URI_GET_USER_INFO, params);
     }


    /**
     * 发送短信验证码(注册、修改密码、找回密码)
     * @param checkCode   　验证码
     * @param mobile 　接受的手机号
     * @return json格式字符串
     * @throws IOException
     */

    public static String sendSmsCode(String checkCode, String mobile, Integer type) {
        String msg = "";
        if(MSG_TYPE_TEMPLATE == type){
            msg = CHECK_CODE_TEMPLATE;
        }else if(MSG_TYPE_REGISTER == type){
            //注册
            msg = CHECK_CODE_REGISTER;
        }else if(MSG_TYPE_FIND_SYSPWD == type){
            //找回登录密码
            msg = CHECK_CODE_FIND_SYSPWD;
        }else if(MSG_TYPE_FIND_PAYPWD == type){
            //找回支付密码
            msg = CHECK_CODE_FIND_PAYPWD;
        }else if(MSG_TYPE_BIND_CREDITCARD == type){
            //绑定信用卡
            msg = CHECK_CODE_BIND_CREDITCARD;
        }else if(MSG_TYPE_DEBIT_CARD == type){
            //绑定借记卡
            msg = CHECK_CODE_BIND_DEBIT_CARD;
        }else if(MSG_TYPE_VERIFY == type){
            //实名
            msg = CHECK_CODE_VERIFY;
        }else{
            msg = CHECK_CODE_TEMPLATE;
        }
        String content = msg.replace("code", checkCode);
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", APIKEY);
        params.put("text", content);
        params.put("mobile", mobile);
        String  backJson =  post(URI_SEND_SMS, params);
        JSONObject jsonObject = JSON.parseObject(backJson);

        if (jsonObject.getString("code").equals("0")){
            return  "0000";
        }
        return jsonObject.getString("msg");
    }


    /**
     * 智能匹配模板接口发短信
     *
     * @param text   　短信内容
     * @param mobile 　接受的手机号
     * @return json格式字符串
     * @throws IOException
     */

     public static String sendSms(String text, String mobile) throws IOException {
         Map<String, String> params = new HashMap<String, String>();
         params.put("apikey", APIKEY);
         params.put("text", text);
         params.put("mobile", mobile);
         return post(URI_SEND_SMS, params);
     }

     /**
     * 通过模板发送短信(不推荐)
     *
     * @param apikey    apikey
     * @param tpl_id    　模板id
     * @param tpl_value 　模板变量值
     * @param mobile    　接受的手机号
     * @return json格式字符串
     * @throws IOException
     */

     public static String tplSendSms(String apikey, long tpl_id, String tpl_value, String mobile) throws IOException {
         Map<String, String> params = new HashMap<String, String>();
         params.put("apikey", apikey);
         params.put("tpl_id", String.valueOf(tpl_id));
         params.put("tpl_value", tpl_value);
         params.put("mobile", mobile);
         return post(URI_TPL_SEND_SMS, params);
     }

     /**
     * 通过接口发送语音验证码
     * @param apikey apikey
     * @param mobile 接收的手机号
     * @param code   验证码
     * @return
     */

     public static String sendVoice(String apikey, String mobile, String code) {
         Map<String, String> params = new HashMap<String, String>();
         params.put("apikey", apikey);
         params.put("mobile", mobile);
         params.put("code", code);
         return post(URI_SEND_VOICE, params);
     }

     /**
     * 通过接口绑定主被叫号码
     * @param apikey apikey
     * @param from 主叫
     * @param to   被叫
     * @param duration 有效时长，单位：秒
     * @return
     */

     public static String bindCall(String apikey, String from, String to , Integer duration ) {
         Map<String, String> params = new HashMap<String, String>();
         params.put("apikey", apikey);
         params.put("from", from);
         params.put("to", to);
         params.put("duration", String.valueOf(duration));
         return post(URI_SEND_BIND, params);
     }

     /**
     * 通过接口解绑绑定主被叫号码
     * @param apikey apikey
     * @param from 主叫
     * @param to   被叫
     * @return
     */
     public static String unbindCall(String apikey, String from, String to) {
         Map<String, String> params = new HashMap<String, String>();
         params.put("apikey", apikey);
         params.put("from", from);
         params.put("to", to);
         return post(URI_SEND_UNBIND, params);
     }

     /**
     * 基于HttpClient 4.3的通用POST方法
     *
     * @param url       提交的URL
     * @param paramsMap 提交<参数，值>Map
     * @return 提交响应
     */

     public static String post(String url, Map<String, String> paramsMap) {
         CloseableHttpClient client = HttpClients.createDefault();
         String responseText = "";
         CloseableHttpResponse response = null;
             try {
                 HttpPost method = new HttpPost(url);
                 if (paramsMap != null) {
                     List<NameValuePair> paramList = new ArrayList<NameValuePair>();
                     for (Map.Entry<String, String> param : paramsMap.entrySet()) {
                         NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
                         paramList.add(pair);
                     }
                     method.setEntity(new UrlEncodedFormEntity(paramList, ENCODING));
                 }
                 response = client.execute(method);
                 HttpEntity entity = response.getEntity();
                 if (entity != null) {
                     responseText = EntityUtils.toString(entity, ENCODING);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 try {
                     response.close();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             return responseText;
      }
     
     public static void main(String[] args) throws Exception{

     }
}
