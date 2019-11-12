package com.goldccm.service.param;

import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/15 21:07
 */
public interface IParamService extends IBaseService {

    /**
     * 根据参数名获取对应的值
     * @Author linyb
     * @Date 2017/4/15 21:20
     */
    String findValueByName(String paramName);
}
