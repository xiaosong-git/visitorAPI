package com.goldccm.service.visitor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.visitor.IInnerVisitorService;
import com.goldccm.service.visitor.IVisitorRecordService;
import com.goldccm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 内网接口实现类
 * @author: cwf
 * @create: 2020-03-20 16:59
 **/
@Service("innerVisitorService")
public class InnerVisitorServiceImpl extends BaseServiceImpl implements IInnerVisitorService {
    Logger logger = LoggerFactory.getLogger(InnerVisitorServiceImpl.class);
    @Autowired
    private IVisitorRecordService visitorRecordService;
    @Autowired
    private IParamService paramService;
    /**
     * 接收企业用户访问||邀约
     * A访问企业 B云端 C被访企业
     * 访问时只需提供用户信息，确认访问后去拉取图片信息
     *
     * @param paramMap
     * @return
     */
    @Override
    public Result innerVisitRequest(Map<String, Object> paramMap) throws Exception {
        String userCode = BaseUtil.objToStr(paramMap.get("userCode"), "");//企业内部员工在外部唯一码
        String userRealName = BaseUtil.objToStr(paramMap.get("userRealName"), "");//企业内部员工姓名
        String userPhone = BaseUtil.objToStr(paramMap.get("userPhone"), "");//企业内部员工手机号
        String realName = BaseUtil.objToStr(paramMap.get("realName"), "");//被访者姓名
        String phone = BaseUtil.objToStr(paramMap.get("phone"), "");//被访者手机号
        String routerId = BaseUtil.objToStr(paramMap.get("routerId"), "");//被访者手机号
        String reason = BaseUtil.objToStr(paramMap.get("reason"), "无");//访问理由
        String startDate = BaseUtil.objToStr(paramMap.get("startDate"), "");//访问开始时间
        String endDate = BaseUtil.objToStr(paramMap.get("endDate"), "");//访问结束时间
        String originId = BaseUtil.objToStr(paramMap.get("originId"), "");//内部记录Id
        String toType = "T";
        boolean toInner = true;//是否发送向企业
        if ("".equals(realName) || "".equals(phone) || "".equals(userCode) || "".equals(userRealName)
                || "".equals(userPhone) || "".equals(startDate) || "".equals(endDate) || "".equals(routerId) || "".equals(originId)
        ) {
            return Result.unDataResult("fail", "上传云端参数缺失");
        }
        //存入访问记录
        Map<String, Object> visitRecord = new HashMap<>();
        //todo 查询被访者是否为云端用户
        Map<String, Object> user = findFirstBySql("select id, phone,realName,isAuth,idHandleImgUrl from tbl_user where realName='" + realName + "' and phone='" + phone + "'");
        if (user != null) {
            visitRecord.put("visitorId", BaseUtil.objToStr(user.get("id"), ""));
            visitRecord.put("toType", "T");//表示查询时visitorId关联的为tbl_user id
            toInner = false;
        } else {
            //todo 查找被访者是否为企业用户
            user = findFirstBySql("select * from " + TableList.INNER_USER + " iu left join " + TableList.ROUTER + " tr on iu.routerId=tr.id where realName='" + realName + "' and phone ='" + phone + "'");
            if (user != null) {//存入被访者id
                visitRecord.put("visitorId", BaseUtil.objToStr(user.get("id"), ""));
                visitRecord.put("toType", "F");//表示查询时visitorId关联的为v_inner_user id
                toType = "F";
            } else {//没有用户
                return Result.unDataResult("fail", "未查到用户，无法访问");
            }
        }

        //企业用户表 访客是否存在
        Map<String, Object> visitInnerUser = findFirstBySql("select * from " + TableList.INNER_USER + " where realName='" + userRealName + "' and phone ='" + userPhone + "'");
        //todo  存储企业访客 包括路由 访客信息
        if (visitInnerUser == null) {
            //目前假设传入的为id 插入innerUser
            visitInnerUser = new HashMap<>();
            visitInnerUser.put("routerId", routerId);
            visitInnerUser.put("userCode", userCode);
            visitInnerUser.put("realName", userRealName);
            visitInnerUser.put("phone", userPhone);
            visitInnerUser.put("isAuth", "T");
//            //发送人脸图片到文件服务器 文件服务器需要开启接口接收
//
//            //获取图片服务器处理后的地址并插入 假设获取成功
//            visitInnerUser.put("idHandleImgUrl", "/inner/123/1234567.jpg");
            int visitInnerUserId = save(TableList.INNER_USER, visitInnerUser);
            //如果存储成功 则存入访问记录
            if (visitInnerUserId > 0) {
                visitRecord.put("userId", String.valueOf(visitInnerUserId));
            } else {
                return Result.unDataResult("fail", "访问失败，云端数据错误");
            }
        } else {//存储访客id
            visitRecord.put("userId", String.valueOf(visitInnerUser.get("id")));
        }
        //未审核
        visitRecord.put("cstatus", "applyConfirm");
        visitRecord.put("visitDate", DateUtil.getCurDate());
        visitRecord.put("visitTime", DateUtil.getCurTime());
        visitRecord.put("reason", reason);
        visitRecord.put("startDate", startDate);
        visitRecord.put("endDate", endDate);
        visitRecord.put("vitype", "F");
        visitRecord.put("recordType", "1");
        visitRecord.put("originType", "F");//表示查询时userId关联的为v_inner_user id
        //提示为非好友访问
        visitRecord.put("answerContent", "非好友访问");
        //来源id
        visitRecord.put("originId", originId);
        //todo 发送给C企业，需要下发访客信息，与插入访客记录
        if (toInner) {//如果被访者为企业用户
            Map<String, String> map = new HashMap<>();
            Iterator<Map.Entry<String, Object>> entries = visitRecord.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Object> entry = entries.next();
                map.put(entry.getKey(), entry.getValue().toString());
            }
            map.put("phone", userPhone);
            map.put("realName", userRealName);
            map.put("userCode", userCode);
            String ip = BaseUtil.objToStr(user.get("ip"), "");
            String port = BaseUtil.objToStr(user.get("port"), "");
            if ("".equals(ip) || "".equals(port)) {
                logger.error("{},{}查找路由错误，缺少ip或端口号", userRealName, realName);
                return Result.unDataResult("fail", "查找错误");
            }
            ThirdResponseObj obj = HttpUtil.http2Nvp("http://" + ip + ":" + port + "/visitor/visitorRecord/receiveOutVisit", map, "UTF-8");
            String makePlanJsonResult = obj.getResponseEntity();
            if (makePlanJsonResult == null) {
                logger.error("routerId={}，调用客户端接口错误，ip或端口号错误", user.get("routerId"));
                return Result.unDataResult("fail", "查找错误");
            }
            JSONObject jsonObject = JSONObject.parseObject(makePlanJsonResult);
            Map resultMap = JSON.parseObject(jsonObject.toString());
            Map<String, Object> verify = (Map<String, Object>) resultMap.get("verify");
            String sign = BaseUtil.objToStr(verify.get("sign"), "");
            String desc = BaseUtil.objToStr(verify.get("desc"), "");
            if (!"success".equals(sign)) {
                return Result.unDataResult(sign, desc);
            }
        }
        //查询重复
//        Map<String, Object> check = visitorRecordService.check(visitInnerUser.get("id"), user.get("id"), 1, startDate, endDate);
//        if (check == null) {
            int save = save(TableList.VISITOR_RECORD, visitRecord);
            if (save > 0) {
                // todo 返回A公司云端数据库id
                return ResultData.dataResult("success", "操作成功",save);
            } else {
                logger.error("非好友访问失败,{}访问{}", userRealName, realName);
                return Result.unDataResult("fail", "操作失败");
            }
//        } else {
//            logger.info(startDate + "该时间段" + endDate + "内已经有邀约信息存在");
//            return Result.unDataResult("fail", "在" + startDate + "——" + endDate + "内已经有访问信息存在");
//        }

    }
    //返回企业接口
    @Override
    public Result innerVisitResponse(Map<String, Object> paramMap) throws Exception {
        Object id = paramMap.get("id");//访客记录id
        Integer outRecordId = BaseUtil.objToInteger(paramMap.get("outRecordId"),0);
        Object cstatus = paramMap.get("cstatus");
        Object companyId = paramMap.get("companyId");
        Object orgCode = paramMap.get("orgCode");
        Object answerContent = paramMap.get("answerContent");
        if (outRecordId==null){
            return Result.unDataResult("fail","缺少参数");
        }
        Map<String, Object> record = findById(TableList.VISITOR_RECORD,outRecordId );
        Object toId = record.get("toId");
        record.put("cstatus",cstatus);
        record.put("companyId",companyId);
        record.put("orgCode",orgCode);
        record.put("answerContent",answerContent);
        Object originType = record.get("originType");
        int update = update(TableList.VISITOR_RECORD, record);
        //访客

        //拒绝访问，只保存数据库，不下发图片
        if ("applyFail".equals(cstatus)){
            return update >0?Result.unDataResult("success","拒绝成功"):
                    Result.unDataResult("fail","拒绝操作失败，服务器做出！");
        }else{//同意访问，保存数据库，下发图片
            //根据toId是否为空来判断访客的去向，空表示云端访问客户端。
            if (toId != null) {//客户端访问客户端
                //获取从客户端获取图片
                Map<String, Object> user = findFirstBySql("select * from " + TableList.INNER_USER + " iu left join " + TableList.ROUTER + " tr on iu.routerId=tr.id where iu.id=" + record.get("userId") );
                Object idHandleImgUrl = user.get("idHandleImgUrl");
                //todo  调用客户端接口,保存数据库并获取图片
                //传入接口参数
                Map<String,String> map =new HashMap<>();
                map.put("outRecordId",String.valueOf(outRecordId));
                map.put("id",String.valueOf(toId));
                map.put("cstatus", String.valueOf(cstatus));
                map.put("companyId",String.valueOf(companyId));
                map.put("orgCode",String.valueOf(orgCode));
                map.put("answerContent",String.valueOf(answerContent));
                ThirdResponseObj obj = HttpUtil.http2Nvp("http://" + user.get("ip") + ":" + user.get("port") + "/visitor/visitorRecord/innerVisitReceive", map, "UTF-8");
                String makePlanJsonResult = obj.getResponseEntity();
                if (makePlanJsonResult == null) {
                    logger.error("routerId={}，调用客户端接口错误，ip或端口号错误", user.get("routerId"));
                    return Result.unDataResult("fail", "查找错误");
                }
                JSONObject jsonObject = JSONObject.parseObject(makePlanJsonResult);
                Map resultMap = JSON.parseObject(jsonObject.toString());
                Map<String, Object> verify = (Map<String, Object>) resultMap.get("verify");
                String sign = BaseUtil.objToStr(verify.get("sign"), "");
                String desc = BaseUtil.objToStr(verify.get("desc"), "");
                if (!"success".equals(sign)) {
                    return Result.unDataResult(sign, desc);
                }
                String photo = jsonObject.getString("data");
                //todo 开启线程将图片发送给图片服务器

                //todo 将图片下发被访者
                return update >0?
                        ResultData.dataResult("success","操作成功",photo):
                        Result.unDataResult("fail","操作失败！");
            }else{ //云端访问客户端 下发图片
                //从图片服务器获取图片
                Map<String, Object> user = findFirstBySql("select idHandleImgUrl from " + TableList.USER + " where id =" + record.get("userId"));
                Object idHandleImgUrl = user.get("idHandleImgUrl");
                String imageServerUrl = paramService.findValueByName("imageServerUrl");
                String photo= Base64.encode(FilesUtils.getImageFromNetByUrl(imageServerUrl+idHandleImgUrl));
                //下发图片
                return  update >0?
                        ResultData.dataResult("success","操作成功",photo):
                        Result.unDataResult("fail","操作失败！")
                        ;
            }
        }
    }
    @Override
    public String sendPhotos(String innerUrl) throws Exception {
        /**
         * 查询
         */
        Map<String, Object> user = findFirstBySql("select idHandleImgUrl from " + TableList.INNER_USER + " where id =" + 1);
        Object idHandleImgUrl = user.get("idHandleImgUrl");
        String imageServerUrl = paramService.findValueByName("imageServerUrl");
        String photo=Base64.encode(FilesUtils.getImageFromNetByUrl(imageServerUrl+idHandleImgUrl));
        return photo;
    }




}
