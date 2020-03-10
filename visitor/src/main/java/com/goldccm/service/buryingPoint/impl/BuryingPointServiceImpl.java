package com.goldccm.service.buryingPoint.impl;

import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.buryingPoint.BuryingPointService;
import com.goldccm.util.BaseUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 埋点
 * @author: cwf
 * @create: 2020-02-27 15:23
 **/
@Service("BuryingPointService")
public class BuryingPointServiceImpl extends BaseServiceImpl implements BuryingPointService {
    @Override
    public Result save(Map<String, Object> paramMap) {
        String type = BaseUtil.objToStr(paramMap.get("type"), "");
        if ("".equals(type)){
            return Result.unDataResult("fail","缺少type");
        }
        if ("record".equals(type)){
            String recordType = BaseUtil.objToStr(paramMap.get("recordType"), "");
            if ("".equals(recordType)){
                return Result.unDataResult("fail","recordType");
            }
            int update = deleteOrUpdate("update " + TableList.BURYING_POINT + " set total=total+1 where type='record'" +
                    " and  recordType='" + recordType + "'");
            if (update>0){
                return Result.unDataResult("success","埋点成功");
            }else{
                return Result.unDataResult("fail","埋点失败");
            }
        }else {
            Integer typeId = BaseUtil.objToInteger(paramMap.get("typeId"), 0);
            if (typeId==0){
                return Result.unDataResult("fail","缺少typeId");
            }
            Map<String, Object> firstBySql = findFirstBySql("select id from " + TableList.BURYING_POINT + " where type='" + type + "' and " +
                    "type_id=" + typeId);
            if (firstBySql==null){
                Map<String, Object> burying=new HashMap<>();
                burying.put("type",type);
                burying.put("type_id",typeId);
                burying.put("total",1);
                int save = save(TableList.BURYING_POINT, burying);
                if (save>0){
                    return Result.unDataResult("success","埋点成功");
                }else {
                    return Result.unDataResult("fail","埋点失败");
                }
            }else {
                int update = deleteOrUpdate("update " + TableList.BURYING_POINT + " set total=total+1 where type='"+type+"'" +
                        " and  type_id=" + typeId + "");
                if (update>0){
                    return Result.unDataResult("success","埋点成功");
                }else{
                    return Result.unDataResult("fail","埋点失败");
                }
            }
        }
    }
}
