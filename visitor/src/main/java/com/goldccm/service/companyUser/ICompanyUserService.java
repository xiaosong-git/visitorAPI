package com.goldccm.service.companyUser;


import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
public interface ICompanyUserService extends IBaseService {

    /**
     * 未确认记录
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result findApplying(Map<String, Object> paramMap) throws Exception;

    /**
     * 修改状态
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result updateStatus(Map<String, Object> paramMap) throws Exception;

    /**
     * 确认记录
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result findApplySuc(Map<String, Object> paramMap) throws Exception;
    
    
    /**
     * 大楼员工确认记录
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result findApplySucByOrg(Map<String, Object> paramMap) throws Exception;
    /**
     * 大楼员工确认记录
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result findApplyAllSucByOrg(Map<String, Object> paramMap) throws Exception;

    Map<String, Object>  findByUserCompany(Integer userId, Integer compnayId);
}
