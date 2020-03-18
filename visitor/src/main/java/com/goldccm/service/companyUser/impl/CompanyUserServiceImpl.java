package com.goldccm.service.companyUser.impl;

import com.goldccm.model.compose.PageModel;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.companyUser.ICompanyUserService;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.visitor.impl.VisitorRecordServiceImpl;
import com.goldccm.util.Base64;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.ConsantCode;
import com.goldccm.util.FilesUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
@Service("companyUserService")
public class CompanyUserServiceImpl extends BaseServiceImpl implements ICompanyUserService {

	@Autowired
    private IParamService paramService;
    Logger logger = LoggerFactory.getLogger(CompanyUserServiceImpl.class);
    @Override
    public Result findApplying(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if(userId==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少用户参数!");
        }
        String columnSql = "select cu.*,c.companyName,cs.sectionName";
        String fromSql = " from " + TableList.COMPANY_USER + " cu " +
                " left join " + TableList.COMPANY + " c on cu.companyId=c.id" +
                " left join " + TableList.COMPANY_SECTION + " cs on cu.sectionId=cs.id" +
                " left join " + TableList.DICT_ITEM + " d on d.dict_code='companyUserRoleType' and d.item_code=cu.roleType " +
                " left join " + TableList.DICT_ITEM + " i on i.dict_code='companyUserStatus' and i.item_code=cu.status " +
                " where cu.userId = '"+userId+"' and cu.status = 'applying'";
        List<Map<String, Object>> list  = findList(columnSql,fromSql);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取公司成功",list)
                : Result.unDataResult("success","暂无数据");
    }

    @Override
    public Result updateStatus(Map<String, Object> paramMap) throws Exception {
        Integer id = BaseUtil.objToInteger(paramMap.get("id"), null);
        if(id==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少参数!");
        }
        String status = BaseUtil.objToStr(paramMap.get("status"), null);
        if(StringUtils.isBlank(status)){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少状态参数!");
        }
        //修改
        Map<String, Object> update = new HashMap<String, Object>();
        update.put("id", id);
        update.put("status",status);
        Integer updateResult =update(TableList.COMPANY_USER, update);
        return updateResult > 0
                ? Result.unDataResult("success","修改成功")
                : Result.unDataResult("fail","修改失败");
    }
    @Override
    public Result findApplySuc(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if(userId==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少用户参数!");
        }
        String columnSql = "select cu.*,c.companyName,c.addr,cs.sectionName";
        String fromSql = " from " + TableList.COMPANY_USER + " cu " +
                " left join " + TableList.COMPANY + " c on cu.companyId=c.id" +
                " left join " + TableList.COMPANY_SECTION + " cs on cu.sectionId=cs.id" +
               // " left join"  + TableList.ORG +" og on c.orgid=og.id"+
                " left join " + TableList.DICT_ITEM + " d on d.dict_code='companyUserRoleType' and d.item_code=cu.roleType " +
                " left join " + TableList.DICT_ITEM + " i on i.dict_code='companyUserStatus' and i.item_code=cu.status " +
                " where cu.userId = '"+userId+"' and cu.status = 'applySuc' and cu.currentStatus='normal'";
        System.out.println(columnSql+fromSql);
        List<Map<String, Object>> list  = findList(columnSql,fromSql);

        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取公司成功",list)
                : Result.unDataResult("success","暂无数据");
    }
    

    @Override
    public Result findApplySucByOrg(Map<String, Object> paramMap) throws Exception {
        String org_code = BaseUtil.objToStr(paramMap.get("org_code"), null);
        String create_date =new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if(org_code==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少大楼参数!");
        }
        String columnSql = "select cu.id,cu.companyId,cu.sectionId,cu.userId,cu.userName,cu.createDate,cu.createTime," +
                "cu.roleType,cu.status,cu.currentStatus,cu.postId,u.idHandleImgUrl idHandleImgUrl,u.idType idType," +
                "u.idNO idNO,c.companyFloor companyFloor,u.phone ";
        String fromSql = " from " + TableList.COMPANY_USER + " cu " +
                " left join " + TableList.USER + " u on cu.userId=u.id" +
                " left join " + TableList.COMPANY + " c on cu.companyId=c.id" +
                " left join " + TableList.COMPANY_SECTION + " cs on cu.sectionId=cs.id" +
                " join"  + TableList.ORG +" og on c.orgid=og.id"+
                " left join " + TableList.DICT_ITEM + " d on d.dict_code='companyUserRoleType' and d.item_code=cu.roleType " +
                " left join " + TableList.DICT_ITEM + " i on i.dict_code='companyUserStatus' and i.item_code=cu.status " +
                " where og.org_code = '"+org_code+"' and cu.status = 'applySuc' and (u.authDate = '"+create_date+"' or "+
                " cu.createDate='"+create_date+"')"+" and u.isAuth = 'T' "
                +
                " UNION  " +
                " select null id ,u.companyid  ,null sectionId, userId, userName, ovu.createDate,ovu.createtime, roleType,status," +
                "currentStatus, postId,u.idHandleImgUrl,u.idType ,u.idno idNO,null companyFloor,u.phone " +
                " from "+TableList.ORG_VIP_USER+" ovu\n" +
                "left join "+TableList.USER+" u on ovu.userId=u.id \n" +
                "left join "+TableList.ORG+" org on org.id=ovu.orgId   " +
                "where org.org_code='"+org_code+"'  and u.isAuth = 'T' and DATE_FORMAT(ovu.createDate, '%Y-%m-%d') = '"+create_date+"'";
//               System.out.println(columnSql+fromSql);
        
        List<Map<String, Object>> list  = findList(columnSql,fromSql);
        for(int i=0;i<list.size();i++) {
        	Map<String,Object> map=list.get(i);
        	String idHandleImgUrl=(String) map.get("idHandleImgUrl");
        	if(idHandleImgUrl!=null&&idHandleImgUrl.length()!=0) {
        	    //生产图片地址
                String imageServerUrl = paramService.findValueByName("imageServerUrl");
        	 String photo=Base64.encode(FilesUtils.getImageFromNetByUrl(imageServerUrl+idHandleImgUrl));
//           测试图片地址
//        	 String photo=Base64.encode(FilesUtils.getPhoto(idHandleImgUrl));
        	 list.get(i).put("photo", photo);
        	}
        }
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取大楼员工信息成功",list)
                : Result.unDataResult("success","暂无数据");
    }
    @Override
    public Result findApplyAllSucByOrg(Map<String, Object> paramMap) throws Exception {
        String org_code = BaseUtil.objToStr(paramMap.get("org_code"), null);
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 1);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 50);
        if(org_code==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少大楼参数!");
        }
        String columnSql = "select * ";
        String fromSql = "from (select cu.id,cu.companyId,cu.sectionId,cu.userId,cu.userName,cu.createDate,cu.createTime," +
                "                cu.roleType,cu.status,cu.currentStatus,cu.postId,u.idHandleImgUrl idHandleImgUrl,u.idType idType, " +
                "                u.idNO idNO,c.companyFloor companyFloor,u.phone from " + TableList.COMPANY_USER + " cu " +
                " left join " + TableList.USER + " u on cu.userId=u.id" +
                " left join " + TableList.COMPANY + " c on cu.companyId=c.id" +
                " left join " + TableList.COMPANY_SECTION + " cs on cu.sectionId=cs.id" +
                " join"  + TableList.ORG +" og on c.orgid=og.id"+
                " left join " + TableList.DICT_ITEM + " d on d.dict_code='companyUserRoleType' and d.item_code=cu.roleType " +
                " left join " + TableList.DICT_ITEM + " i on i.dict_code='companyUserStatus' and i.item_code=cu.status " +
                " where og.org_code = '"+org_code+"' and cu.status = 'applySuc' "+" and u.isAuth = 'T' and cu.currentStatus='normal' "
                +
                " UNION all " +
                " select null id ,u.companyid  ,null sectionId, userId, userName, ovu.createDate,ovu.createtime, roleType,status," +
                "currentStatus, postId,u.idHandleImgUrl,u.idType ,u.idno idNO,null companyFloor,u.phone " +
                " from "+TableList.ORG_VIP_USER+" ovu\n" +
                "left join "+TableList.USER+" u on ovu.userId=u.id \n" +
                "left join "+TableList.ORG+" org on org.id=ovu.orgId   " +
                "where org.org_code='"+org_code+"'  and u.isAuth = 'T' and currentStatus='normal')x";

        PageModel page = findPage(columnSql, fromSql, pageNum, pageSize);
        logger.info(columnSql+fromSql);
        List<Map<String,Object>> rows = page.getRows();
        List<Map<String,Object>> removeList =new LinkedList<>();

        String imageServerUrl;
        String photo = null;
        StringBuilder errorId = new StringBuilder("");
        Map<String,Object> map;
        for(int i=0;i<rows.size();i++) {
            map=rows.get(i);
        	String idHandleImgUrl=(String) map.get("idHandleImgUrl");
        	if(idHandleImgUrl!=null&&idHandleImgUrl.length()!=0) {
//             //生产图片地址
                imageServerUrl = paramService.findValueByName("imageServerUrl");
                if (imageServerUrl==null){
                    errorId.append(map.get("userId") +",");
//                    removeList.add(map);
                    continue;
                }
                try {
                    photo = Base64.encode(FilesUtils.getImageFromNetByUrl(imageServerUrl + idHandleImgUrl));
                }catch (Exception e){
                    errorId.append(map.get("userId")+",");
                    removeList.add(map);
//                    page.getRows().remove(i);
                }
//           测试图片地址
//        	 String photo=Base64.encode(FilesUtils.getPhoto(idHandleImgUrl));

                rows.get(i).put("photo", photo);
        	}
        }
        rows.removeAll(removeList);
        if (errorId!=null){
            logger.error("错误照片的用户id:{}",errorId);
        }
        return rows != null && !rows.isEmpty()
                ? ResultData.dataResult("success","获取大楼员工信息成功",page)
                : Result.unDataResult("success","暂无数据");
    }
    @Override
    public Map<String, Object>  findByUserCompany(Integer userId,Integer compnayId){
        Map<String, Object> companyUser = findFirstBySql("select * from " + TableList.COMPANY_USER + " where userId=" + userId + " and companyId=" + compnayId);
        return companyUser;
    }
}
