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
        String comOrgId=null;
        //查找公司的大楼id
         if(companyId!=null){
            Map<String, Object> company = findById(TableList.COMPANY, BaseUtil.objToInteger(companyId,0));
            if (company!=null) {
                comOrgId = BaseUtil.objToStr(company.get("orgId"), null);
            }
        }

        String columSql="select DISTINCT m.id,m.menu_code,m.menu_name,m.menu_url,m.sid,sstatus ";
        //3、获取个人的app角色权限
        String userSql="from " + TableList.APP_MENU + " am   \n" +
                "left join " + TableList.APP_USER_ROLE_MENU + " urm on   urm.menu_id=am.id \n"+
                "where exists (select urr.role_id from "+TableList.APP_USER_ROLE_R+" urr " +
                "where  urm.role_id =urr.role_id and user_id="+userId+")\n";
        //获取基础用户权限
        String fromSql="  from "+TableList.APP_MENU+" m " +
                "left join \n" +
                TableList.APP_USER_ROLE_MENU+" urm on m.id=urm.menu_id where urm.role_id=5";
        //大楼id sql
        String orgSql="";
        if (orgId!=null){
            orgSql+=orgId+",";
        }
         if (comOrgId!=null){
            orgSql+=comOrgId+",";
        }
        if (orgId!=null||comOrgId!=null){
            //拼接orgid的sql
            orgSql= orgSql.substring(0,orgSql.length()-1);
            //1、获取登入人大楼的app菜单权限 2、获取个人所在大楼的app角色权限
            fromSql = " from (select DISTINCT m.id,m.menu_code,m.menu_name,m.menu_url,m.sid,sstatus" +
                    " from " + TableList.APP_MENU + " m left join " + TableList.APP_USER_ROLE_MENU + " org " +
                    "on org.menu_id=m.id " +
                    "where org.role_id in(select role_id from\n" +
                    TableList.APP_ORG_ROLE_R+" where org_id in ("+orgSql+")) and\n" +
                    "exists(\n" +
                    "select am.id "+userSql +
                    "and m.id=am.id )" +
                    "union all " +columSql+ fromSql+")m";
        }
        System.out.println(columSql+fromSql);
        List <Map<String, Object>> list= findList(columSql, fromSql);
        return (list==null||list.isEmpty())?Result.unDataResult("success","暂无数据"):ResultData.dataResult("success","获取app权限菜单成功",list);
    }
}
