package com.goldccm.service.user;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * @program: visitor
 * @description: 用户app权限
 * @author: cwf
 * @create: 2019-09-14 10:01
 **/

public interface IUserAppRoleService extends IBaseService {

    /**
     * 获取角色菜单
     * @param paramMap
     * @return Result
     * @throws Exception
     * @author cwf
     * @date 2019/9/14 10:37
     */

    Result getRoleMenu(Map<String, Object> paramMap) throws Exception;
}
