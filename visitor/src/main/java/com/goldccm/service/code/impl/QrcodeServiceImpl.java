package com.goldccm.service.code.impl;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.code.IQrcodeService;
import com.goldccm.util.Base64;
import com.goldccm.util.BaseUtil;
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

        String sql = "select u.solecode, u.realName, u.idNO,vr.orgCode," +
                "vu.realName as visitName,vu.phone as visitPhone ,startDate,endDate \n" +
                "from "+TableList.VISITOR_RECORD+" vr\n" +
                "left join "+TableList.USER+" u on vr.userId=u.id\n" +
                "left join "+TableList.USER+" vu on vr.visitorId=vu.id\n" +
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
//        String userId =BaseUtil.objToStr(visitRecord.get("userId"),"");

//        //人员唯一身份识别码
//        String solecode="["+BaseUtil.objToStr(visitRecord.get("solecode"),"")+"]";
        //访客姓名
        String userName="["+BaseUtil.objToStr(visitRecord.get("realName"),"")+"]";
        //访客证件号
//        String idNo="["+BaseUtil.objToStr(visitRecord.get("idNO"),"")+"]";
//        //被访者姓名
//        String visitName="["+BaseUtil.objToStr(visitRecord.get("visitName"),"")+"]";
        //被访者手机号
//        String visitPhone="["+BaseUtil.objToStr(visitRecord.get("visitPhone"),"")+"]";
        //被访者大楼编号
//        String orgCode="["+BaseUtil.objToStr(visitRecord.get("orgCode"),"")+"]";
        //访问开始时间
        String startDate="["+BaseUtil.objToStr(visitRecord.get("startDate"),"")+"]";
        //访问结束时间
        String endDate="["+BaseUtil.objToStr(visitRecord.get("endDate"),"")+"]";
//        /**
//         * 以后每个大楼一个workKey做des加密
//         * @date 2019/9/19 15:09
//         */
//        String orgKey="ASDFGHJK";
//        visitName="["+DESUtil.encode(orgKey,visitName)+"]";
//        System.out.println(visitName);
        mainText=userName+"["+recordId+"]"+startDate+endDate;
//        mainText=solecode+userName+idNo+visitName+visitPhone+orgCode+startDate+endDate;
        //返回加密后字符串
        String ret=publiTitle+"|"+Base64.encode(mainText.getBytes("UTF-8"));
//        String ret=publiTitle+"|"+mainText;
        return Result.unDataResult("success",ret);
    }
}
