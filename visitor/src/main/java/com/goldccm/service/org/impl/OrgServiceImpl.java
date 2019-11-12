package com.goldccm.service.org.impl;

import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.org.IOrgService;
import com.goldccm.service.visitor.impl.VisitorRecordServiceImpl;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.ConsantCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
@Service("orgService")
public class OrgServiceImpl extends BaseServiceImpl implements IOrgService {
    Logger logger = LoggerFactory.getLogger(OrgServiceImpl.class);
    @Override
    public Result requestMansion(Map<String, Object> paramMap) throws Exception {
        String city = BaseUtil.objToStr(paramMap.get("city"), null);
        if(StringUtils.isBlank(city)){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少参数!");
        }
        String columnSql = "select *";
        String fromSql = " from " + TableList.ORG +
                " where city = '"+city+"' and orgType ='floor'";
        List<Map<String, Object>> list  = findList(columnSql,fromSql);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取大厦成功",list)
                : Result.unDataResult("success","暂无数据");
    }
    /**
     *  传入用户id 根据用户当前的公司获取大楼编号
     * @param userId	用户id
     * @return java.lang.String
     * @throws Exception
     * @author cwf
     * @date 2019/9/26 10:23
     */
    @Override
    public String findOrgCodeByUserId(Integer userId) throws Exception{
        String sql = "select org_code from " + TableList.ORG + " o left join "+TableList.COMPANY+" c " +
                "on c.orgId=o.id " +
                "left join " +TableList.USER+" u on u.companyId=c.id"+
                " where u.id='"+userId+"'";
        String orgCode="";
        try {
            Map<String, Object> org = findFirstBySql(sql);
            if (org==null){
                return null;
            }
             orgCode = BaseUtil.objToStr(org.get("org_code"),null);
        }catch (Exception e){
            logger.error("获取大楼orgcode错误",e);
            return null;
        }
        return orgCode;
    }
    /**
     *  传入公司id 根据用公司获取大楼编号
     * @param companyId	用户id
     * @return java.lang.String
     * @throws Exception
     * @author cwf
     * @date 2019/10/12 10:23
     */
    @Override
    public String findOrgCodeByCompanyId(Integer companyId) throws Exception{
        String sql = "select org_code from " + TableList.ORG + " o left join "+TableList.COMPANY+" c " +
                "on c.orgId=o.id " +
                " where c.id='"+companyId+"'";
        String orgCode="";
        try {
            Map<String, Object> org = findFirstBySql(sql);
            if (org==null){
                return null;
            }
            orgCode = BaseUtil.objToStr(org.get("org_code"),null);
        }catch (Exception e){
            logger.error("获取大楼orgcode错误",e);
            return null;
        }
        return orgCode;
    }
}
