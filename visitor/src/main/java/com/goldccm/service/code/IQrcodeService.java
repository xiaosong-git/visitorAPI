package com.goldccm.service.code;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/11 10:17
 */
public interface IQrcodeService extends IBaseService{

    /**
     * 根据cdkey获取二维码信息
     * @Author linyb
     * @Date 2017/4/11 10:20
     */
    public Map<String,Object> getByCdKey(String cdkey,String csStatus);

    Result getVisitQrcode(Map<String, Object> paramMap) throws Exception;

    /**
     * 处理第三方二维码
     * @param paramMap 参数
     * @return
     */
    Result dealQrcode(Map<String, Object> paramMap) throws Exception;
}
