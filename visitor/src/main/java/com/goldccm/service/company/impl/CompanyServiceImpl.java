package com.goldccm.service.company.impl;

import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.company.ICompanyService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.ConsantCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
@Service("companyService")
public class CompanyServiceImpl extends BaseServiceImpl implements ICompanyService {

    @Override
    public Result requestCompany(Map<String, Object> paramMap) throws Exception {
        Integer orgId = BaseUtil.objToInteger(paramMap.get("orgId"), null);
        if(orgId==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少机构参数!");
        }
        String columnSql = "select *";
        String fromSql = " from " + TableList.COMPANY +
                " where orgId = '"+orgId+"'";
        List<Map<String, Object>> list  = findList(columnSql,fromSql);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取公司成功",list)
                : Result.unDataResult("success","暂无数据");
    }
}
