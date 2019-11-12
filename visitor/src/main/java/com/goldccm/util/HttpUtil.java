package com.goldccm.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {

	public static ThirdResponseObj http2Nvp(String url,List<BasicNameValuePair> nvps) throws Exception{
		HttpClient httpClient = new SSLClient();
		HttpPost postMethod = new HttpPost(url);
		
//		  //链接超时
//        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
//        //读取超时
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 300000);

		postMethod.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		//HttpResponse resp = null;
		HttpResponse resp = httpClient.execute(postMethod);
        
        int statusCode = resp.getStatusLine().getStatusCode();
        
        ThirdResponseObj responseObj = new ThirdResponseObj();
        if (200 == statusCode) {
        	responseObj.setCode("success");
        	String str = EntityUtils.toString(resp.getEntity(), "UTF-8");
        	responseObj.setResponseEntity(str);
        }else{
        	responseObj.setCode(statusCode+"");
        }
        
        return responseObj;
	}
	public static ThirdResponseObj http2Se(String url,StringEntity entity) throws Exception{
		HttpClient httpClient = new SSLClient();
		HttpPost postMethod = new HttpPost(url);
		
		postMethod.setEntity(entity);
		//HttpResponse resp = null;
		HttpResponse resp = httpClient.execute(postMethod);
        
        int statusCode = resp.getStatusLine().getStatusCode();
        
        ThirdResponseObj responseObj = new ThirdResponseObj();
        if (200 == statusCode) {
        	responseObj.setCode("success");
        	String str = EntityUtils.toString(resp.getEntity(), "UTF-8");
        	responseObj.setResponseEntity(str);
        }else{
        	responseObj.setCode(statusCode+"");
        	String str = EntityUtils.toString(resp.getEntity(), "UTF-8");
        	responseObj.setResponseEntity(str);

        }
        
        return responseObj;
	}

	public static ThirdResponseObj http2Nvp(String url,Map<String,String>map,String encodeType) throws Exception{

		List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

		for (Map.Entry<String, String> entry : map.entrySet()) {
			nvps.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
		}

		HttpClient httpClient = new SSLClient();
		HttpPost postMethod = new HttpPost(url);

//		  //链接超时
//        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
//        //读取超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 300000);

		postMethod.setEntity(new UrlEncodedFormEntity(nvps, encodeType));
		//HttpResponse resp = null;
		HttpResponse resp = httpClient.execute(postMethod);

		int statusCode = resp.getStatusLine().getStatusCode();

		ThirdResponseObj responseObj = new ThirdResponseObj();
		if (200 == statusCode) {
			responseObj.setCode("success");
			String str = EntityUtils.toString(resp.getEntity(), encodeType);
			responseObj.setResponseEntity(str);
		}else{
			responseObj.setCode(statusCode+"");
		}

		return responseObj;
	}

	public static void main(String[] args) throws Exception {
		//生成计划
		/*Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId","10001");
		map.put("batchId","batchId10003");
		map.put("cardNo","cardNo10001");
		map.put("amount","10000");
		map.put("minMargin","1500");
		map.put("charge","50");
		map.put("repayBDate","2018-01-06");
		map.put("repayEDate","2018-01-10");
		String url = "http://192.168.1.182:9090/RepaySchedule/addRepayUserInfo";*/


		//查询计划列表
		/*Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId","10001");
		map.put("cardNo","cardNo10001");
		map.put("pageIndex","1");
		String url = "http://192.168.1.182:9090/RepaySchedule/qryCardHistory";*/

		//查询计划明细
		Map<String, String> map = new HashMap<String, String>();
		map.put("partner_code", "jrhk_mohe");
		map.put("partner_key", "787985c220ea41168bb26f5efaa8f79b");
		map.put("task_id", "TASKEB000103201804231542200601140029");
		String url = "https://api.shujumohe.com/octopus/sdk.service.task.query/v3";

		//交易汇总
		/*Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId","10001");
		map.put("year","2018");
		map.put("month","1");
		map.put("pageIndex","1");
		String url = "http://192.168.1.182:9090/RepaySchedule/qryUserHistory";*/

		HttpUtil.test(map, url);


	}

	public static void test(Map<String, String> map, String url) throws Exception{
		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
		for(Map.Entry<String, String> m : map.entrySet()){
			list.add(new BasicNameValuePair(m.getKey(), m.getValue()));
		}
		ThirdResponseObj obj = http2Nvp(url, list);
		System.out.println(obj.getCode());
		System.out.println(obj.getResponseEntity());
	}
}
