package com.goldccm.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


/**
 * 友盟消息推送
 * 
 * @author Administrator
 *
 */
public class YMNotification {

	public final static String appKey = "5cd398f7570df39d0200125f";
	public final static String appMasterSecret = "nxn2qz2p4ly5psy5jykxhoth9ljdm8eo";
//	public final static String type = "customizedcast";// customizedcast，通过alias进行推送，
	public final static String type = "unicast";// customizedcast，通过deviceToken进行推送，
//	public final static String device_tokens
	public final static String alias_type = "PHONE_ALIAS";// alias序列
	public final static String UM_URL = "http://msg.umeng.com/api/send";
	public final static String appKey_IOS ="5cef4a6c0cafb28c35000d98";
	public final static String appMasterSecret_IOS="eqd2pgn7xwhkbbsorb5svh0ez1aldkri";
	
	

	public static JSONObject buildAndroidReq(String alias, String msgTitle, String msgContent) {
		JSONObject jsonreq = new JSONObject();
		JSONObject jsonpayload = new JSONObject();
		JSONObject jsonbody = new JSONObject();
		JSONObject jsonextra = new JSONObject();
		JSONObject jsonpolicy = new JSONObject();
		jsonreq.put("appkey", appKey);// 应用唯一标识
		jsonreq.put("timestamp", System.currentTimeMillis());// 时间戳
		jsonreq.put("type", type);// 必填，消息发送类型
//		jsonreq.put("alias_type", alias_type);// alias的类型, alias_type可由开发者自定义,
												// 开发者在SDK中调用setAlias(alias, alias_type)时所设置的alias_type
//		jsonreq.put("alias", alias);

		//这里的“alias”为device_tokens
		jsonreq.put("device_tokens", alias);

		jsonpayload.put("display_type", "notification");// 消息类型:
														// notification(通知)、message(消息)

		jsonbody.put("ticker", msgTitle); // 必填，通知栏提示文字
		jsonbody.put("title", msgTitle);// 必填，通知标题
		jsonbody.put("text", msgContent);// 必填，通知文字描述
		jsonbody.put("after_open", "go_custom");
		jsonbody.put("custom", "go_visitor");

		jsonextra.put("push_from", "UM");
		jsonpayload.put("body", jsonbody);
		jsonpayload.put("extra", jsonextra);

		jsonreq.put("payload", jsonpayload);
		jsonreq.put("production_mode", true);// 可选，正式/测试模式。默认为true
		jsonreq.put("mipush", true); // 可选，默认为false。当为true时，表示MIUI、EMUI、Flyme系统设备离线转为系统下发
//		jsonreq.put("mi_activity", "com.goldccm.visitor.ui.activity.MiPushActivity");// 可选，mipush值为true时生效，表示走系统通道时打开指定页面acitivity的完整包路径。
		jsonreq.put("mi_activity", "com.github.flutterumpush.UmengOtherPushActivity");// 可选，mipush值为true时生效，表示走系统通道时打开指定页面acitivity的完整包路径。

		return jsonreq;
	}

	public static JSONObject buildIOSReq(String alias, String msgTitle, String msgContent) {
		JSONObject jsonreq = new JSONObject();
		JSONObject jsonpayload = new JSONObject();
		JSONObject jsonaps = new JSONObject();
		JSONObject jsonalert = new JSONObject();
		JSONObject jsonpolicy = new JSONObject();

		jsonreq.put("appkey", appKey_IOS);// 应用唯一标识
		jsonreq.put("timestamp", System.currentTimeMillis());// 时间戳
		jsonreq.put("type", type);// 必填，消息发送类型
//		jsonreq.put("alias_type", alias_type);// alias的类型, alias_type可由开发者自定义,
//												// 开发者在SDK中
//		jsonreq.put("alias", alias);
		jsonreq.put("device_tokens", alias);
		jsonalert.put("title", msgTitle);
		jsonalert.put("subtitle", msgTitle);
		jsonalert.put("body", msgContent);

		jsonaps.put("alert", jsonalert);
		jsonaps.put("content-available", "1");

		jsonpayload.put("aps", jsonaps);
		jsonpayload.put("badge", 1);

		jsonreq.put("payload", jsonpayload);
		jsonreq.put("production_mode", true);// 可选，正式/测试模式。默认为true
		jsonreq.put("description", "访客消息"); // 可选，默认为false。当为true时，表示MIUI、EMUI、Flyme系统设备离线转为系统下发

		return jsonreq;

	}

	/**
	 * 想Android用户发送消息
	 * 
	 * @param alias
	 * @param msgTitle
	 * @param msgContent
	 * @return
	 * @throws Exception
	 */
	public static boolean httpToYMAndroid(String alias, String msgTitle, String msgContent) throws Exception {
		boolean is_success = false;
		String json = buildAndroidReq(alias, msgTitle, msgContent).toJSONString();
		StringEntity entity = new StringEntity(json, "UTF-8");
		entity.setContentType("application/json");
		String sign = DigestUtils.md5Hex(("POST" + UM_URL + json + appMasterSecret).getBytes("utf8"));
		String url = UM_URL + "?sign=" + sign;
		ThirdResponseObj obj = HttpUtil.http2Se(url, entity);
		String httpCode = obj.getCode();
		System.out.println("友盟返回码：" + httpCode);
		String resultStr = obj.getResponseEntity();
		System.out.println("友盟返回内容:" + resultStr);
		if ("success".equals(httpCode)) {

			System.out.println("友盟连接成功");
			JSONObject jsonres = JSONArray.parseObject(resultStr);
			String msg = jsonres.get("ret").toString();
			String data = jsonres.get("data").toString();
			if ("SUCCESS".equals(msg)) {
				is_success = true;
			} else {
				JSONObject jsondata = JSONArray.parseObject(data);
				String error_code = jsondata.getString("error_code");
				String error_msg = jsondata.getString("error_msg");
				System.out.println("友盟发送Android失败,错误码：" + error_code + ",错误信息:" + error_msg);
			}
		}
		return is_success;
	}

	/**
	 * IOS 推送
	 * 
	 * @param alias
	 * @param msgTitle
	 * @param msgContent
	 * @return
	 * @throws Exception
	 */
	public static boolean httpToYMIOS(String alias, String msgTitle, String msgContent) throws Exception {
		boolean is_success=false;
		String json = buildIOSReq(alias, msgTitle, msgContent).toJSONString();
		StringEntity entity = new StringEntity(json, "UTF-8");
		entity.setContentType("application/json");
		String sign = DigestUtils.md5Hex(("POST" + UM_URL + json + appMasterSecret_IOS).getBytes("utf8"));
		String url = UM_URL + "?sign=" + sign;
		ThirdResponseObj obj = HttpUtil.http2Se(url, entity);
		String httpCode = obj.getCode();
		String resultStr= obj.getResponseEntity();
		System.out.println("友盟返回码：" + httpCode);
		if ("success".equals(httpCode)) {

			System.out.println("友盟连接成功");
			JSONObject jsonres = JSONArray.parseObject(resultStr);
			String msg = jsonres.get("ret").toString();
			String data = jsonres.get("data").toString();
			if ("SUCCESS".equals(msg)) {
				is_success = true;
			} else {
				JSONObject jsondata = JSONArray.parseObject(data);
				String error_code = jsondata.getString("error_code");
				String error_msg = jsondata.getString("error_msg");
				System.out.println("友盟发送Android失败,错误码：" + error_code + ",错误信息:" + error_msg);
			}
		}
		return is_success;
	}

	public static void main(String[] args) {
		try {

			boolean android_issuccess = YMNotification.httpToYMAndroid("Avv2vbrSxTPXy36YZWR7FXUJ8PU4jsAixHPzYGbdkaxW", "hello", "凡客信息");
			System.out.println(android_issuccess);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
