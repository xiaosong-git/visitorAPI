package com.goldccm.service.appversion;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * Created by Administrator on 2017/5/21.
 */
public interface IAppVersionService extends IBaseService {

    /**
     * 根据app类型（android、ios）、channel、app版本号更新
     * @Author Bzk
     * @Date 2017/4/11 14:12
     */
    Result updateAndroid(String appType, String channel,Integer versionNum);

    /**
     * 更新IOS
     * @param appType ios
     * @return
     * @throws Exception
     */
    Result updateIos(String appType,Map<String,Object> paramMap) throws Exception;

}
