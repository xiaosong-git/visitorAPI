package com.goldccm.service.companyUser.impl;

import com.goldccm.model.compose.PageModel;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.companyUser.ICompanyUserService;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.visitor.IForeignService;
import com.goldccm.util.*;

import com.goldccm.util.Base64;
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
    @Autowired
    private IForeignService foreignService;

    @Override
    public Result findApplying(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if (userId == null) {
            return Result.unDataResult(ConsantCode.FAIL, "缺少用户参数!");
        }
        String columnSql = "select cu.*,c.companyName,cs.sectionName";
        String fromSql = " from " + TableList.COMPANY_USER + " cu " +
                " left join " + TableList.COMPANY + " c on cu.companyId=c.id" +
                " left join " + TableList.COMPANY_SECTION + " cs on cu.sectionId=cs.id" +
                " left join " + TableList.DICT_ITEM + " d on d.dict_code='companyUserRoleType' and d.item_code=cu.roleType " +
                " left join " + TableList.DICT_ITEM + " i on i.dict_code='companyUserStatus' and i.item_code=cu.status " +
                " where cu.userId = '" + userId + "' and cu.status = 'applying'";
        List<Map<String, Object>> list = findList(columnSql, fromSql);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success", "获取公司成功", list)
                : Result.unDataResult("success", "暂无数据");
    }

    @Override
    public Result updateStatus(Map<String, Object> paramMap) throws Exception {
        Integer id = BaseUtil.objToInteger(paramMap.get("id"), null);
        if (id == null) {
            return Result.unDataResult(ConsantCode.FAIL, "缺少参数!");
        }
        String status = BaseUtil.objToStr(paramMap.get("status"), null);
        if (StringUtils.isBlank(status)) {
            return Result.unDataResult(ConsantCode.FAIL, "缺少状态参数!");
        }
        //修改
        Map<String, Object> update = new HashMap<String, Object>();
        update.put("id", id);
        update.put("status", status);
        Integer updateResult = update(TableList.COMPANY_USER, update);
        return updateResult > 0
                ? Result.unDataResult("success", "修改成功")
                : Result.unDataResult("fail", "修改失败");
    }

    @Override
    public Result findApplySuc(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        if (userId == null) {
            return Result.unDataResult(ConsantCode.FAIL, "缺少用户参数!");
        }
        String columnSql = "select cu.*,c.companyName,c.addr,cs.sectionName";
        String fromSql = " from " + TableList.COMPANY_USER + " cu " +
                " left join " + TableList.COMPANY + " c on cu.companyId=c.id" +
                " left join " + TableList.COMPANY_SECTION + " cs on cu.sectionId=cs.id" +
                // " left join"  + TableList.ORG +" og on c.orgid=og.id"+
                " left join " + TableList.DICT_ITEM + " d on d.dict_code='companyUserRoleType' and d.item_code=cu.roleType " +
                " left join " + TableList.DICT_ITEM + " i on i.dict_code='companyUserStatus' and i.item_code=cu.status " +
                " where cu.userId = '" + userId + "' and cu.status = 'applySuc' and cu.currentStatus='normal'";
        System.out.println(columnSql + fromSql);
        List<Map<String, Object>> list = findList(columnSql, fromSql);

        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success", "获取公司成功", list)
                : Result.unDataResult("success", "暂无数据");
    }
    /**
     * 旧按获取大楼员工当天员工
     * @return Result
     */
    @Override
    public Result findApplySucByOrg(Map<String, Object> paramMap) throws Exception {
        String orgCode = BaseUtil.objToStr(paramMap.get("org_code"), null);
        String createDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), null);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), null);
        if (orgCode == null) {
            return Result.unDataResult(ConsantCode.FAIL, "缺少大楼参数!");
        }
        return applySucByOrg(orgCode,createDate,pageNum,pageSize,"old");
    }
    /**
     * 新按获取大楼员工当天员工确认数据
     * @return Result
     */
    @Override
    public Result newFindApplySucOrg(Map<String, Object> paramMap){
        String orgCode = BaseUtil.objToStr(paramMap.get("org_code"), null);
        String pospCode = BaseUtil.objToStr(paramMap.get("posp_code"), null);
        String mac = BaseUtil.objToStr(paramMap.get("mac"), null);
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 1);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 10);
        String createDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (orgCode == null || pospCode == null||mac==null) {
            return Result.unDataResult("fail", "大楼编号与上位机编号与mac不能为空");
        }
        Result result = foreignService.existOgrPosp(pospCode, orgCode);
        if (result.getVerify().get("sign").equals("fail")) {
            return result;
        }
        //rsa校验
        Result rsaPosp = foreignService.rsaPosp(result, pospCode, orgCode, mac);
        if (rsaPosp.getVerify().get("sign").equals("fail")){
            return rsaPosp;
        }
        return applySucByOrg(orgCode,createDate,pageNum,pageSize,"new");
    }

    /**
     * 确认下发上位机
     * @param paramMap
     * @return
     */
    @Override
    public Result applySucConfirm(Map<String, Object> paramMap) {
        String idStr = BaseUtil.objToStr(paramMap.get("idStr"),null);
        if (idStr==null){
            return ResultData.unDataResult("fail","请传入idStr");
        }
        return confirmSql(idStr);
    }

    /**
     * 获取大楼当天员工查询
     * @param orgCode  大楼编号
     * @param create_date 日期
     * @param type 新旧接口 new old
     * @return Result
     */
    //todo 增加一个companyId
    public Result applySucByOrg(String orgCode , String create_date,Integer pageNum,Integer pageSize,String type){

        String columnSql = " from (( select * from (select cu.id,cu.companyId,cu.sectionId,cu.userId,cu.userName,if( cu.createDate<u.authDate,u.authDate,cu.createDate) " +
                "createDate,cu.createTime,cu.roleType,cu.status,max(currentStatus) currentStatus,cu.postId,u.idHandleImgUrl idHandleImgUrl,u.idType idType,u.idNO idNO," +
                "c.companyFloor companyFloor,u.phone ,u.bid,org_code";
        String fromSql = " from tbl_company_user cu  " +
                "left join tbl_user u on cu.userId=u.id " +
                "left join tbl_company c on cu.companyId=c.id " +
                "left join tbl_company_section cs on cu.sectionId=cs.id " +
                "join t_org og on c.orgid=og.id " +
                "left join   t_dict_item  d on d.dict_code='companyUserRoleType' and d.item_code=cu.roleType  " +
                "left join   t_dict_item  i on i.dict_code='companyUserStatus' and i.item_code=cu.status  " +
                "where og.org_code = '" + orgCode + "'" +
                " and cu.status = 'applySuc' and u.isAuth = 'T' and " +
                "((u.authDate = '" + create_date + "' or  cu.createDate='" + create_date + "') " +
                "and currentStatus='normal') or (cu.createDate= '" + create_date + "' " +
                "and currentStatus='deleted') GROUP BY `companyId`,userId )x " +
                "where org_code='" + orgCode + "'\n ) " +
                " UNION   " +
                "(select null id ,u.companyid  ,null sectionId, userId, userName, ovu.createDate,ovu.createtime, " +
                "roleType,status,currentStatus, postId,u.idHandleImgUrl,u.idType ,u.idno idNO,null companyFloor," +
                "u.phone,u.bid,org_code  from " + "tbl_org_vip_user" + " ovu\n" +
                "left join " + "tbl_user" + " u on ovu.userId=u.id \n" +
                "left join " + " t_org" + " org on org.id=ovu.orgId   " +
                "where org.org_code='" + orgCode + "'  and u.isAuth = 'T' and " +
                "(DATE_FORMAT(ovu.`updateTime`,  '%Y-%m-%d') = '" + create_date + "' " +
                "or DATE_FORMAT(ovu.createDate, '%Y-%m-%d') = '" + create_date + "')))x";

        logger.info(columnSql+fromSql);

        PageModel page = findPage("select * ",columnSql+fromSql, pageNum, pageSize==null?50:pageSize);
        List<Map<String, Object>> rows = page.getRows();
        //返回值类型page有带页数
         type = pageNum == null && pageSize == null ? "old" : "new";
        return  "new".equals(type)? insertUserPhoto(page,"page"):insertUserPhoto(rows,"rows");
    }

    /**
     * 旧确认大楼全部员工
     * @param paramMap
     * @return
     * @throws Exception
     */
    @Override
    public Result findApplyAllSucByOrg(Map<String, Object> paramMap) throws Exception {
        String orgCode = BaseUtil.objToStr(paramMap.get("org_code"), null);
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 1);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 50);
        if (orgCode == null) {
            return Result.unDataResult(ConsantCode.FAIL, "缺少大楼参数!");
        }
        //查询sql
        return applyAllSucOrg(orgCode, pageNum, pageSize);
    }

    /**
     * 新确认大楼全部员工记录
     * @param paramMap
     * @return
     */
    @Override
    public Result newFindApplyAllSucOrg(Map<String, Object> paramMap) {
        String orgCode = BaseUtil.objToStr(paramMap.get("orgCode"), null);
        String pospCode = BaseUtil.objToStr(paramMap.get("pospCode"), null);
        String mac = BaseUtil.objToStr(paramMap.get("mac"), null);
        if (orgCode == null || pospCode == null||mac==null) {
            return Result.unDataResult("fail", "大楼编号与上位机编号与mac不能为空");
        }
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 1);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 50);
        Result result = foreignService.existOgrPosp(pospCode, orgCode);
        if (result.getVerify().get("sign").equals("fail")) {
            return result;
        }
        //rsa校验
        Result rsaPosp = foreignService.rsaPosp(result, pospCode, orgCode, mac);
        if (rsaPosp.getVerify().get("sign").equals("fail")){
            return rsaPosp;
        }
        return applyAllSucOrg(orgCode, pageNum, pageSize);
    }
    //查询全部大楼员工
    public Result applyAllSucOrg(String orgCode, Integer pageNum, Integer pageSize) {
        String columnSql = "select * ";
        String fromSql = "from (select cu.id,cu.companyId,cu.sectionId,cu.userId,cu.userName,cu.createDate,cu.createTime," +
                "                cu.roleType,cu.status,cu.currentStatus,cu.postId,u.idHandleImgUrl idHandleImgUrl,u.idType idType,u.bid ," +
                "                u.idNO idNO,c.companyFloor companyFloor,u.phone from " + TableList.COMPANY_USER + " cu " +
                " left join " + TableList.USER + " u on cu.userId=u.id" +
                " left join " + TableList.COMPANY + " c on cu.companyId=c.id" +
                " left join " + TableList.COMPANY_SECTION + " cs on cu.sectionId=cs.id" +
                " join" + TableList.ORG + " og on c.orgid=og.id" +
                " left join " + TableList.DICT_ITEM + " d on d.dict_code='companyUserRoleType' and d.item_code=cu.roleType " +
                " left join " + TableList.DICT_ITEM + " i on i.dict_code='companyUserStatus' and i.item_code=cu.status " +
                " where og.org_code = '" + orgCode + "' and cu.status = 'applySuc' " + " and u.isAuth = 'T' and cu.currentStatus='normal'  "
                +
                " UNION all " +
                " select null id ,u.companyid  ,null sectionId, userId, userName, ovu.createDate,ovu.createtime, roleType,status," +
                "currentStatus, postId,u.idHandleImgUrl,u.idType ,u.idno idNO,null companyFloor,u.phone,u.bid " +
                " from " + TableList.ORG_VIP_USER + " ovu\n" +
                "left join " + TableList.USER + " u on ovu.userId=u.id \n" +
                "left join " + TableList.ORG + " org on org.id=ovu.orgId   " +
                "where org.org_code='" + orgCode + "'  and u.isAuth = 'T' and currentStatus='normal')x";

        PageModel page = findPage(columnSql, fromSql, pageNum, pageSize);
        return insertUserPhoto(page,"page");

    }

    /**
     * 给每一条记录插入照片
     * @param page sql查询的记录
     * @return
     */
    @Override
    public Result insertUserPhoto(Object page,String type) {
        //todo   update by cwf  2020-07-09 17:07 Reason: 获取裁剪后的图片
        List<Map<String, Object>> rows;
        try {
            logger.info("拉取员工时插入照片");
            if ("page".equals(type)) {
                PageModel result = (PageModel) page;
                if (result.getRows() == null || result.getRows().isEmpty()) {
                    //插入图片接口
                    return ResultData.dataResult("success", "暂无数据",page);
                }
                rows = result.getRows();
            } else {
                rows = (List<Map<String, Object>>) page;
            }
            //错误照片用户id
            String errorId = insertUserPhoto(rows);
            if ("".contentEquals(errorId)) {
                return !rows.isEmpty()
                        ? ResultData.dataResult("success", "获取大楼员工信息成功", page)
                        : Result.unDataResult("success", "暂无数据");
            }
            return !rows.isEmpty()
                    ? ResultData.dataResult("success", "获取大楼员工信息成功" + ",错误照片的用户id:" + errorId, page)
                    : Result.unDataResult("success", ",错误照片的用户id:" + errorId);
        }catch (Exception e){
            logger.error("插入图片错误",e);
            return  null;
        }

    }

    /**
     * 给每一条记录插入照片
     * @param rows sql查询的记录
     * @return 错误用户id
     */
    public String insertUserPhoto(List<Map<String, Object>> rows ) {
        String  imageServerUrl = paramService.findValueByName("imageServerUrl");
        String photo;
        StringBuilder errorId = new StringBuilder();
        String idHandleImgUrl;
        for (Map<String, Object> row : rows) {
            idHandleImgUrl = (String) row.get("idHandleImgUrl");
            if (idHandleImgUrl != null && idHandleImgUrl.length() != 0) {
                try {
                    photo = Base64.encode(FilesUtils.getImageFromNetByUrl(imageServerUrl + idHandleImgUrl));
                } catch (Exception e) {
                    errorId.append(row.get("userId") + ",");
                    row.put("photo", "");
                    continue;
                }
//           测试图片地址
//        	 String photo=Base64.encode(FilesUtils.getPhoto(idHandleImgUrl));
                row.put("photo", photo);
            }
        }
        return errorId.toString();
    }

    @Override
    public Map<String, Object> findByUserCompany(Integer userId, Integer compnayId) {
        Map<String, Object> companyUser = findFirstBySql("select * from " + TableList.COMPANY_USER + " where userId=" + userId + " and companyId=" + compnayId);
        return companyUser;
    }

    public Result confirmSql( String idStr) {
        logger.info("准备更新{}", idStr);
        int update = deleteOrUpdate("update " + TableList.COMPANY_USER + " set isFlag='T' where id in (" + idStr + ")");
        if (update > 0) {
            logger.info("更新{}成功", idStr);
            return Result.success();
        }
        return Result.fail();
    }

}
