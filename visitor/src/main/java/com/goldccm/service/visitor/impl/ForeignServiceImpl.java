package com.goldccm.service.visitor.impl;

import com.goldccm.model.compose.PageModel;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.visitor.IForeignService;
import com.goldccm.util.Base64;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.DateUtil;
import com.goldccm.util.FilesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 上位机拉取实现
 * @author: cwf
 * @create: 2019-12-10 10:22
 **/
@Service("foreignService")
public class ForeignServiceImpl  extends BaseServiceImpl implements IForeignService {
    Logger logger = LoggerFactory.getLogger(ForeignServiceImpl.class);
    @Autowired
    private IParamService paramService;
    @Override
    public Result FindOrgCode(String pospCode, String orgCode, Integer pageNum, Integer pageSize) {
        if (pageNum != 1) {
            return Result.unDataResult("fail", "页数不对!");
        }
        // 判断上位机是否正常
        String orgSql = " select * from " + TableList.ORG + " where org_code = '" + orgCode + "'";
        System.out.println(orgSql);
        Map<String, Object> org = findFirstBySql(orgSql);
        if (org == null) {
            return Result.unDataResult("fail", "数据异常!");
        }
        String orgId = org.get("id").toString();
        String pospSql = " select * from " + TableList.POSP + " where orgId = '" + orgId + "' and pospCode ='"
                + pospCode + "' and cstatus='normal'";
        System.out.println(pospSql);
        Map<String, Object> posp = findFirstBySql(pospSql);
        if (posp == null) {
            return Result.unDataResult("fail", "无此上位机编码" + pospCode + "或者无此大楼编码" + orgCode);
        }
        String today= DateUtil.getSystemTime();
        String columnSql = "select vr.id visitId,vr.userId,vr.visitDate,vr.visitTime,vr.orgCode,vr.dateType,vr.startDate,vr.endDate,u.realName userRealName,u.idType userIdType,u.idNO userIdNO,u.soleCode soleCode,u.idHandleImgUrl idHandleImgUrl,c.companyFloor companyFloor,v.realName vistorRealName,v.idType vistorIdType,v.idNO visitorIdNO,o.province province,o.city city";
        String fromSql = " from " + TableList.VISITOR_RECORD + " vr " + " left join " + TableList.USER
                + " v on vr.visitorId=v.id" + " left join " + TableList.USER + " u on vr.userId=u.id"+ " left join " +TableList.COMPANY +" c on vr.companyId=c.id"
                + " left join " + TableList.ORG + " o on v.orgId=o.id"
                + " where vr.cstatus='applySuccess' and vr.orgCode = '" + orgCode + "'"
                + " and vr.startDate<= '" + today
                + "' and vr.endDate>='" + today + "' and isFlag='F' order by vr.id";
        logger.info(columnSql+fromSql);
        PageModel pageModel = this.findPage(columnSql, fromSql, pageNum, pageSize);
        //有数据 获取图片并插入
        if (pageModel.getRows() != null && !pageModel.getRows().isEmpty()) {
         List<Map<String, Object>> maps = pageModel.getRows();
            for(int i=0;i<maps.size();i++) {
                Map<String,Object> map=maps.get(i);
                String idHandleImgUrl=(String) map.get("idHandleImgUrl");
                if(idHandleImgUrl!=null&&idHandleImgUrl.length()!=0) {
                    String imageServerUrl = paramService.findValueByName("imageServerUrl");
                    String photo= Base64.encode(FilesUtils.getImageFromNetByUrl(imageServerUrl+idHandleImgUrl));
                    maps.get(i).put("photo", photo);
                }
            }
        }
        return pageModel.getRows() != null && !pageModel.getRows().isEmpty()
                ? ResultData.dataResult("success", "获取授权访问信息成功", pageModel)
                : ResultData.dataResult("success", "暂无数据", new PageModel(pageNum, pageSize));
    }

    @Override
    public Result newFindOrgCodeConfirm(String pospCode, String orgCode, String idStr) {
        if ("".equals(pospCode)){
            return Result.unDataResult("fail", "上位机编号缺失!");
        }
        // 判断上位机是否正常
        String orgSql = " select * from " + TableList.ORG + " where org_code = '" + orgCode + "'";
        System.out.println(orgSql);
        Map<String, Object> org = findFirstBySql(orgSql);
        if (org == null) {
            return Result.unDataResult("fail", "数据异常!");
        }
        String orgId = org.get("id").toString();
        String pospSql = " select * from " + TableList.POSP + " where orgId = '" + orgId + "' and pospCode ='"
                + pospCode + "' and cstatus='normal'";
        System.out.println(pospSql);
        Map<String, Object> posp = findFirstBySql(pospSql);
        if (posp == null) {
            return Result.unDataResult("fail", "无此上位机编码" + pospCode + "或者无此大楼编码" + orgCode);
        }
        int update = deleteOrUpdate("update " + TableList.VISITOR_RECORD + " set isFlag='T' where id in (" + idStr + ")");
        if (update>0){
            return Result.success();
        }
        return Result.fail();
    }
}
