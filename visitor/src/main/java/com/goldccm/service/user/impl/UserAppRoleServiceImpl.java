package com.goldccm.service.user.impl;

import com.goldccm.controller.user.UserAppRoleController;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.user.IUserAppRoleService;
import com.goldccm.util.BaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @program: visitor
 * @description: 用户app权限控制实现
 * @author: cwf
 * @create: 2019-09-14 10:02
 **/
@Service("userAppRoleService")
public class UserAppRoleServiceImpl extends BaseServiceImpl implements IUserAppRoleService {
    Logger logger = LoggerFactory.getLogger(UserAppRoleServiceImpl.class);
    /**
     * 1、获取登入人公司所在大楼的app角色权限 2、获取个人所在大楼的app角色权限 3、获取个人的app角色权限
     * 4、个人角色权限需要时1、2 两个步骤合集的子集
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    @Override
    public Result getRoleMenu(Map<String, Object> paramMap) throws Exception{

        Integer userId= BaseUtil.objToInteger(paramMap.get("userId"),null);
        if (userId==null){
          return Result.unDataResult("fail","缺少参数");
        }
        Map<String, Object> user = findById(TableList.USER, userId);
        if(user==null||user.isEmpty()){
            return Result.unDataResult("fail","没有用户参数");
        }
        String companyId = BaseUtil.objToStr(user.get("companyId"),null);
        //查找自己的大楼id
        String orgId = BaseUtil.objToStr(user.get("orgId"),null);
        Integer comOrgId=null;

        //查找公司的大楼id
         if(companyId!=null){
            Map<String, Object> company = findById(TableList.COMPANY, BaseUtil.objToInteger(companyId,0));
            if (company!=null) {
                comOrgId = BaseUtil.objToInteger(company.get("orgId"), null);
            }
        }
        String columSql="select DISTINCT m.id,m.menu_code,m.menu_name,m.menu_url,m.sid,sstatus ";
//        //3、获取个人的app角色权限
//        //获取基础用户权限
        String fromSql="  from "+TableList.APP_MENU+" m " +
                "left join \n" +
                TableList.APP_USER_ROLE_MENU+" urm on m.id=urm.menu_id ";
        String suffix=" left join " +TableList.APP_USER_ROLE +" ur on ur.id=urm.role_id and urm.isOpen='T'"+
        " where ur.role_name='访客'";
        String union="";
        //查找orgRole
        if (comOrgId!=null) {
            Map<String, Object> org = findById(TableList.ORG, comOrgId);
            if (org!=null) {
                String appRole = BaseUtil.objToStr(org.get("approle"), "");
                if (!"".equals(appRole)) {
                    union = " union " + columSql + fromSql + " where urm.role_id=" + appRole+" and urm.isOpen='T' ";
                }
            }
        }
        String order=" order by id";
        logger.info("访客权限："+columSql+fromSql+suffix+union+order);
//        //大楼id sql
        List <Map<String, Object>> list= findList(columSql, fromSql+suffix+union);
        return (list==null||list.isEmpty())?Result.unDataResult("success","暂无数据"):ResultData.dataResult("success","获取app权限菜单成功",list);
    }
}
