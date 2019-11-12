package com.goldccm.service.appversion.impl;

import com.alibaba.fastjson.JSON;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.service.appversion.IAppVersionService;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.param.IParamService;
import com.goldccm.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/21.
 */
@Service("appVersionService")
public class AppVersionService extends BaseServiceImpl implements IAppVersionService {
    @Autowired
    private IParamService paramService;

    /**
     * 根据app类型（android、ios）、channel、app版本号获取最新版本信息
     * @Author Bzk
     * @Date 2017/4/11 14:12
     */
    private Map<String,Object> getVersion(String appType, String channel){
        Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置35
        Map<String,Object> appVersion = null;
        String key = "appVersion_android_"+appType+"_"+channel;
        //redis修改
        String json = RedisUtil.getStrVal(key, apiNewAuthCheckRedisDbIndex);
        if(StringUtils.isNotBlank(json)){
            System.out.println("---从缓存获取版本号----："+json);
            appVersion = JSON.parseObject(json, Map.class);
        }else{

            String sql = " select * from tbl_app_version where appType = '" + appType + "' and channel='" + channel+"'";
            appVersion = this.findFirstBySql(sql);
            System.out.println("---从数据库获取版本号----："+appVersion);
            //redis修改
            RedisUtil.setStr(key, JSON.toJSONString(appVersion), apiNewAuthCheckRedisDbIndex, null);
        }
        return appVersion;
    }

    /**
     * 获取IOS最新版本信息
     * @Author Bzk
     * @Date 2017/4/11 14:12
     */
    private Map<String,Object> getIOSVersion(String appType,String channel){
        Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
        Map<String,Object> appVersion = null;
        String key = "appVersion_ios_" + appType+"_"+channel;
        //redis修改
        String json = RedisUtil.getStrVal(key, apiNewAuthCheckRedisDbIndex);
        if(StringUtils.isNotBlank(json)){
            appVersion = JSON.parseObject(json, Map.class);
        }else{
            String sql = " select * from tbl_app_version where appType = '" + appType + "' and channel='"+channel+"'" ;
            appVersion = this.findFirstBySql(sql);
            //redis修改
            RedisUtil.setStr(key, JSON.toJSONString(appVersion), apiNewAuthCheckRedisDbIndex, null);
        }
        return appVersion;
    }


    /**
     * 根据app类型（android、ios）、channel、app版本号更新
     * @Author Bzk
     * @Date 2017/4/11 14:12
     */
    public Result updateAndroid(String appType, String channel,Integer versionNum){
        //获取最新版本信息
        Map<String,Object> appVersion = getVersion(appType,channel);

        Result result = null;
        if (appVersion == null){
            System.out.println("-----数据库查不到版本号------");

            result = ResultData.unDataResult("fail","已经是最新版本了！");
        }else{
            //获取最新版本号
            String dbVersionNum = (String)appVersion.get("versionNum");
            System.out.println("-----数据库版本号------："+dbVersionNum);
            System.out.println("-----传入版本号------："+versionNum);
            if (Long.valueOf(dbVersionNum) > versionNum){
                //用户的软件不是最新版
                Map<String,Object> appVersionInfo = new HashMap<String, Object>();

                //存放最新版本信息
                appVersionInfo.put("versionName",appVersion.get("versionName"));//版本名
                appVersionInfo.put("versionNum",appVersion.get("versionNum"));//最新版本
                appVersionInfo.put("isImmediatelyUpdate",appVersion.get("isImmediatelyUpdate"));//强制更新
                appVersionInfo.put("updateUrl",appVersion.get("updateUrl"));//更新地址
                appVersionInfo.put("memo",appVersion.get("memo"));//版本说明

                return ResultData.dataResult("success", "", appVersionInfo);
            }else{
                //用户的软件已经是最新版
                System.out.println("-----用户的软件已经是最新版------");
                result = ResultData.unDataResult("fail","已经是最新版本了！");
            }
        }

        return result;
    }

    /**
     * 更新IOS
     * @param appType ios
     * @return
     * @throws Exception
     */
    public Result updateIos(String appType,Map<String,Object> paramMap) throws Exception{
        Object channel = paramMap.get("channel");
        //获取最新版本信息
        String isoChannel = "AppStore";
        if (channel == null){
            isoChannel = "AppStore";
        }else{
            isoChannel = channel.toString();
        }
        System.out.println("isoChannel:"+isoChannel);
        Map<String,Object> appVersion = getIOSVersion(appType,isoChannel);
        if(appVersion != null){
            Map<String,Object> appVersionInfo = new HashMap<String, Object>();
            //存放最新版本信息
            appVersionInfo.put("versionNum",appVersion.get("versionNum"));//版本号
            appVersionInfo.put("isImmediatelyUpdate",appVersion.get("isImmediatelyUpdate"));//立即更新？
            appVersionInfo.put("updateUrl",appVersion.get("updateUrl"));//更新地址
            appVersionInfo.put("memo",appVersion.get("memo"));//版本说明

            return ResultData.dataResult("success", "", appVersionInfo);
        }
        return ResultData.unDataResult("fail","系统异常");
    }
}
