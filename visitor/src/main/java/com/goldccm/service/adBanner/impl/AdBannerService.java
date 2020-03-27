package com.goldccm.service.adBanner.impl;

import com.alibaba.fastjson.JSON;
import com.goldccm.model.compose.*;
import com.goldccm.service.adBanner.IAdBannerService;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.user.impl.UserFriendServiceImpl;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    Logger logger = LoggerFactory.getLogger(UserFriendServiceImpl.class);
    /**
     * 获取广告
     * @return
     * @throws Exception
     * @param paramMap
     */
    @Override
    public Result list(Map<String, Object> paramMap) throws Exception {
        List<Map<String,Object>> banners = null;
        String key = "api_adBanner";
        Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
        //redis修改
        String json = RedisUtil.getStrVal(key, apiNewAuthCheckRedisDbIndex);
        if(!"[]".equals(json)){
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
    @Override
    public Result bannerList(Map<String, Object> paramMap) throws Exception{
        Long userId = BaseUtil.objToLong(paramMap.get("userId"), 0L);
        if (userId.equals(0L)){
            //查询默认新闻
            List list = this.findList(" select * ", "from " + TableList.AD_BANNER + " where (orgId is null or orgId=1) and status = 1 order by orders desc limit 7 ");
            return ResultData.dataResult("success","获取成功",list);
        }
        //根据用户id查找relationNo
        Map<String, Object> relation = findFirstBySql("select o.relation_no from t_org  o  left join tbl_company c on c.orgId=o.id left join tbl_user u on u.companyId =c.id \n" +
                "where u.id=" + userId);
        String relationNo="0";
        if (relation!=null){
            relationNo=BaseUtil.objToStr(relation.get("relation_no"), "0");
        }
        String sql = "  from ((select * from "+ TableList.AD_BANNER + " where  status = 1 AND relationNo like concat(('"+relationNo+"'),'%')" +
                " order by orders desc limit 7) " +
                "union" +
                " ( select * from "+ TableList.AD_BANNER + " where  (orgId is null or orgId=1) and status = 1  order by orders desc limit 7 ))x limit 7";
        List list = findList("select * ", sql);
        logger.info("select * "+ sql);
        return ResultData.dataResult("success","获取成功",list);
    }
}
