package com.goldccm.service.visitor;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

/**
 * @program: goldccm
 * @description:
 * @author: cwf
 * @create: 2019-12-10 10:17
 **/
public interface IForeignService extends IBaseService {
    //上位机大楼拉取访问我的人
    Result FindOrgCode(String pospCode, String orgCode, Integer pageNum, Integer pageSize);

    Result newFindOrgCodeConfirm(String pospCode, String orgCode, String idStr);
}
