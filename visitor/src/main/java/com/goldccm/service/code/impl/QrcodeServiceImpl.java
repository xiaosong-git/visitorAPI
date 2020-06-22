package com.goldccm.service.code.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.code.IQrcodeService;
import com.goldccm.util.Base64;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.MD5Util;
import com.google.common.base.Splitter;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/11 10:18
 */
@Service("qrcodeService")
public class QrcodeServiceImpl extends BaseServiceImpl implements IQrcodeService{
    Logger logger = LoggerFactory.getLogger(QrcodeServiceImpl.class);
    @Autowired
    private IBaseDao baseDao;
    @Override
    public Map<String,Object> getByCdKey(String cdkey,String csStatus){

        String sql =" select q.*, o.orgType from "+ TableList.QRCODE
                    +" q left join " +TableList.ORG + " o on q.orgId = o.id"
                    +" where cdkey = '"+cdkey+"' ";
        if(StringUtils.isNotBlank(csStatus)){
            sql += " and cstatus = '"+csStatus+"' ";
        }
        return baseDao.findFirstBySql(sql);
    }
    /**
     * 根据访客记录生成二维码信息
     *
     * @param paramMap
     */
    @Override
    public Result   getVisitQrcode(Map<String, Object> paramMap) throws Exception{
        String id= BaseUtil.objToStr(paramMap.get("id"),"0");
        Integer recordId=BaseUtil.objToInteger(new String(Base64.decode(id),"UTF-8"),0);
        String publiTitle="";
        String mainText="";
        String sql="select  u.realName,startDate,endDate "+
                "from "+TableList.VISITOR_RECORD+" vr\n" +
                "left join "+TableList.USER+" u on vr.userId=u.id\n" +
                "where vr.id="+recordId;
        logger.info("二维码sql:{}",sql);
        Map<String, Object> visitRecord = findFirstBySql( sql);
        if (visitRecord==null){
            logger.error("访客二维码获取错误！未找到访客记录");
            return Result.fail();
        }
        //特殊标识
        String Special="abc";
        //二维码类型2 访问
        String qrcodeType="&2";
        //总帧数1
        String total="&1";
        //第几帧1
        String num="&1";
        //生成时间
        long creatTime= System.currentTimeMillis();
        publiTitle=Special+qrcodeType+total+num+"&"+creatTime;
        //访客姓名
        String userName="["+BaseUtil.objToStr(visitRecord.get("realName"),"")+"]";
        //访客证件号
        //访问开始时间
        String startDate="["+BaseUtil.objToStr(visitRecord.get("startDate"),"")+"]";
        //访问结束时间
        String endDate="["+BaseUtil.objToStr(visitRecord.get("endDate"),"")+"]";
        mainText=userName+"["+recordId+"]"+startDate+endDate;
        //返回加密后字符串
        String ret=publiTitle+"|"+Base64.encode(mainText.getBytes("UTF-8"));
        return Result.unDataResult("success",ret);
    }
    @Override
    public Result dealQrcode(Map<String, Object> paramMap) throws Exception {
        String url = BaseUtil.objToStr(paramMap.get("url"), "");
        if("".equals(url)){
            return Result.unDataResult("fail","url不能为空");
        }
        String params = url.substring(url.indexOf("?") + 1);
        Map<String, String> split = Splitter.on("&").withKeyValueSeparator("=").split(params);
        String stringA="s="+split.get("s")+"&u="+split.get("u");
        String stringSignTemp=stringA+"&key=7223f404580c466db94d027f576be3d7";
        String sign= MD5Util.MD5(stringSignTemp).toLowerCase();
        logger.info("sign:"+sign);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("s", split.get("s"))
                .addFormDataPart("u", split.get("u"))
                .addFormDataPart("sign", sign)
                .build();
        Request request = new Request.Builder()
                .url("http://hb2.doone.com.cn/api/checkUserStatus")
                .method("POST", body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        logger.info("responseBody:{}",responseBody);
        JSONObject parse = JSON.parseObject(responseBody);
        JSONObject data = JSON.parseObject(parse.getString("data"));
        String code = data.getString("code");
        String resultSign="1".equals(code)?"success":"fail";

        return ResultData.dataResult(resultSign,data.getString("message"),data.get("user"));

    }
}
