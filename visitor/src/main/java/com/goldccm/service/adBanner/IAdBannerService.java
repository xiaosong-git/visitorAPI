package com.goldccm.service.adBanner;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * Created by Administrator on 2017/10/18.
 */
public interface IAdBannerService extends IBaseService {
    /**
     * 获取广告
     * @return
     * @throws Exception
     */
    Result list() throws Exception;

    Result bannerList(Map<String, Object> paramMap) throws Exception;
//    /**
//     * 获取广告sql
//     * @param paramMap
//     * @return com.goldccm.model.compose.Result
//     * @throws Exception
//     * @author chenwf
//     * @date 2019/8/2 10:11
//     */
//    List bannerList(Map<String,Object> paramMap) throws Exception;
}
