package com.goldccm.service.visitor;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * @program: goldccm
 * @description:
 * @author: cwf
 * @create: 2019-12-10 10:17
 **/
public interface IForeignService extends IBaseService {
    //上位机大楼拉取访问我的人

    Result FindOrgCode(String pospCode, String orgCode, Object companyId, Integer pageNum, Integer pageSize);

    //上位机rsa校验
    Result rsaPosp(Result result, String pospCode, String orgCode, String mac);

    //判断是否存在上位机与大楼编码
    Result existOgrPosp(String pospCode, String orgCode);

    Result newFindOrgCodeConfirm(String pospCode, String orgCode, Object companyId, String idStr);
    //上位机大楼拉取访问我的人
    Result newFindOrgCode(Map<String, Object> paramMap);
    //确认拉到数据接口
    Result checkOrgCodeConfirm(Map<String, Object> paramMap);
}
