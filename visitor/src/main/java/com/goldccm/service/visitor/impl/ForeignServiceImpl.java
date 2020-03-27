package com.goldccm.service.visitor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.PageModel;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.company.ICompanyService;
import com.goldccm.service.companyUser.ICompanyUserService;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.visitor.IForeignService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.FilesUtils;
import com.goldccm.util.RSA.RSAEncrypt;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 上位机拉取实现
 * @author: cwf
 * @create: 2019-12-10 10:22
 **/
@Service("foreignService")
public class ForeignServiceImpl extends BaseServiceImpl implements IForeignService {
    Logger logger = LoggerFactory.getLogger(ForeignServiceImpl.class);
    @Autowired
    private IParamService paramService;
    @Autowired
    private ICompanyUserService companyUserService;

    @Override
    public Result FindOrgCode(String pospCode, String orgCode, Integer pageNum, Integer pageSize) {

        Result result = existOgrPosp(pospCode, orgCode);
        if (result.getVerify().get("sign").equals("fail")) {
            return result;
        }
       return findOrgCodeSql(pospCode, orgCode, pageNum, pageSize);
    }
    public Result findOrgCodeSql(String pospCode, String orgCode, Integer pageNum, Integer pageSize){
        String columnSql = "select vr.id visitId,vr.userId,vr.visitDate,vr.visitTime,vr.orgCode,vr.dateType,vr.startDate,vr.endDate,u.realName userRealName,u.idType userIdType,u.idNO userIdNO,u.soleCode soleCode,u.idHandleImgUrl idHandleImgUrl,c.companyFloor companyFloor,v.realName vistorRealName,v.idType vistorIdType,v.idNO visitorIdNO,o.province province,o.city city";
        String fromSql = " from " + TableList.VISITOR_RECORD + " vr " + " left join " + TableList.USER
                + " v on vr.visitorId=v.id" + " left join " + TableList.USER + " u on vr.userId=u.id" + " left join " + TableList.COMPANY + " c on vr.companyId=c.id"
                + " left join " + TableList.ORG + " o on v.orgId=o.id"
                + " where vr.cstatus='applySuccess' and vr.orgCode = '" + orgCode + "'"
                + " and vr.startDate<=date_add(now(),interval +30 minute) and vr.endDate>= date_add(now(),interval -30 minute) and isFlag='F' order by vr.id";
        logger.info(columnSql+fromSql);
        PageModel page = this.findPage(columnSql, fromSql, pageNum, pageSize);
        //有数据 获取图片并插入
        return companyUserService.insertUserPhoto(page,"page");

    }
    public Result newFindOrgCode(Map<String, Object> paramMap) {
        String orgCode = BaseUtil.objToStr(paramMap.get("orgCode"), null);
        String pospCode = BaseUtil.objToStr(paramMap.get("pospCode"), null);
        String mac = BaseUtil.objToStr(paramMap.get("mac"), null);
        if (orgCode == null || pospCode == null||mac==null) {
            return Result.unDataResult("fail", "大楼编号与上位机编号与mac不能为空");
        }
        //数据库是否存在大楼与上位机
        Result result = existOgrPosp(pospCode, orgCode);
        if (result.getVerify().get("sign").equals("fail")) {
            return result;
        }
        //rsa校验
        Result rsaPosp = rsaPosp(result, pospCode, orgCode, mac);
        if (rsaPosp.getVerify().get("sign").equals("fail")){
            return rsaPosp;
        }
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 0);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 10);
        return FindOrgCode(pospCode, orgCode, pageNum, pageSize);
    }

    //上位机rsa校验
    @Override
    public Result rsaPosp(Result result, String pospCode, String orgCode, String mac){
        ResultData resultData=(ResultData) result;
        Object data = resultData.getData();
        Map<String,Map<String,Object>> map = JSONObject.parseObject(JSONObject.toJSONString(data), Map.class);
        Map<String, Object> org = map.get("org");
        Map<String, Object> posp = map.get("posp");
        String rsapublicKey = BaseUtil.objToStr(posp.get("rsapublicKey"),"");
        try {
            byte[] macs = RSAEncrypt.decrypt(RSAEncrypt.loadPublicKeyByStr(rsapublicKey), Base64.decodeBase64(mac));
            String str = new String(macs);
            if (!posp.get("mac").toString().equals(str)){
                return Result.unDataResult("fail","mac校验错误");
            }
        } catch (Exception e) {
            logger.error("orgCode：{}与pospCode：{}的mac校验错误！mac：{}",orgCode,pospCode,mac,e);
            return Result.unDataResult("fail","密文错误");
        }
        return Result.unDataResult("success","密文正确");
    }
    //判断是否存在上位机与大楼编码
    @Override
    public Result existOgrPosp(String pospCode, String orgCode) {
        // 判断上位机是否正常
        String orgSql = " select * from " + TableList.ORG + " where org_code = '" + orgCode + "'";
//        System.out.println(orgSql);
        Map<String, Object> org = findFirstBySql(orgSql);
        if (org == null) {
            return Result.unDataResult("fail", "数据异常,无此大楼!" + orgCode);
        }
        String orgId = org.get("id").toString();
        String pospSql = " select * from " + TableList.POSP + " where orgId = '" + orgId + "' and pospCode ='"
                + pospCode + "' and cstatus='normal'";
//        System.out.println(pospSql);
        Map<String, Object> posp = findFirstBySql(pospSql);
        if (posp == null) {
            return Result.unDataResult("fail", "无此上位机编码" + pospCode + "或者无此大楼编码" + orgCode);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("org", org);
        resultMap.put("posp", posp);
        return ResultData.dataResult("success", "有上位机与大楼编码", resultMap);
    }

    @Override
    public Result newFindOrgCodeConfirm(String pospCode, String orgCode, String idStr) {
        Result result = existOgrPosp(pospCode, orgCode);
        if (result.getVerify().get("sign").equals("fail")) {
            return result;
        }
        return confirmSql(pospCode, orgCode, idStr);
    }
    public Result confirmSql(String pospCode, String orgCode, String idStr) {

        int update = deleteOrUpdate("update " + TableList.VISITOR_RECORD + " set isFlag='T' where id in (" + idStr + ")");
        if (update > 0) {
            return Result.success();
        }
        return Result.fail();
    }
    @Override
    public Result checkOrgCodeConfirm(Map<String, Object> paramMap) {
        String pospCode = BaseUtil.objToStr(paramMap.get("pospCode"),"");
        String orgCode = BaseUtil.objToStr(paramMap.get("orgCode"),"");
        String idStr = BaseUtil.objToStr(paramMap.get("idStr"),"");
        String mac = BaseUtil.objToStr(paramMap.get("mac"),"");
        if (orgCode == null || pospCode == null||mac==null) {
            return Result.unDataResult("fail", "大楼编号与上位机编号与mac不能为空");
        }
        Result result = existOgrPosp(pospCode, orgCode);
        if (result.getVerify().get("sign").equals("fail")) {
            return result;
        }
        //rsa校验
        Result rsaPosp = rsaPosp(result, pospCode, orgCode, mac);
        if (rsaPosp.getVerify().get("sign").equals("fail")){
            return rsaPosp;
        }
        //更新数据
        return confirmSql(pospCode, orgCode, idStr);
    }
}
