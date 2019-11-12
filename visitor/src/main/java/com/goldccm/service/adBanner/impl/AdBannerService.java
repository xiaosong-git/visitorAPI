package com.goldccm.service.adBanner.impl;

import com.alibaba.fastjson.JSON;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.adBanner.IAdBannerService;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.param.IParamService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/18.
 */
@Service("adBannerService")
public class AdBannerService extends BaseServiceImpl implements IAdBannerService {
    @Autowired
    private IParamService paramService;

    /**
     * 获取广告
     * @return
     * @throws Exception
     */
    @Override
    public Result list() throws Exception {
        List<Map<String,Object>> banners = null;
        String key = "api_adBanner";
        Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
        //redis修改
        String json = RedisUtil.getStrVal(key, apiNewAuthCheckRedisDbIndex);
        if(!StringUtils.isBlank(json)){
            //先从缓存中获取

            banners = JSON.parseObject(json, List.class);
        }else{
            //从数据库中取
            banners = this.findList(" select * ","from "+ TableList.AD_BANNER +" where status = 1 order by orders desc ");
            json = JSON.toJSONString(banners);
            //redis修改
            RedisUtil.setStr(key, json, apiNewAuthCheckRedisDbIndex, null);
        }
        for(int i=0; i<banners.size(); i++) {
            String androidParams = BaseUtil.objToStr(banners.get(i).get("androidParams"), null);
            String iosParams = BaseUtil.objToStr(banners.get(i).get("iosParams"), null);
            if (!StringUtils.isBlank(androidParams)) {
                System.out.println("androidParams:"+androidParams+"--");
                Map<String, Object> androidParamsMap = JSON.parseObject(androidParams, Map.class);
                banners.get(i).put("androidParams", androidParamsMap);
            }else{
                banners.get(i).remove("androidParams");
            }
            if (!StringUtils.isBlank(iosParams)) {
                System.out.println("iosParams:"+iosParams+"--");
                Map<String, Object> iosParamsMap = JSON.parseObject(iosParams, Map.class);
                banners.get(i).put("iosParams", iosParamsMap);
            }else{
                banners.get(i).remove("iosParams");
            }
        }
        return ResultData.dataResult("success","获取成功",banners);
    }
//    @Override
//    public List bannerList(Map<String,Object> paramMap) throws Exception{
//        String coloumSql="select * ";
//        String andSql="";
//        Integer companyId=BaseUtil.objToInteger(paramMap.get("companyId"),null);
//        if (companyId!=null){
//            andSql="companyId ="+companyId;
//        }
//        String fromSql="from "+ TableList.AD_BANNER +" where status = 1 "+andSql+" order by orders desc ";
//        return findList(coloumSql,fromSql);
//    }
}
