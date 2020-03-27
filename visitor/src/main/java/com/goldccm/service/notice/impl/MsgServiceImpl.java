package com.goldccm.service.notice.impl;

import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.notice.IMsgService;
import com.goldccm.util.BaseUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 系统消息，工作消息，物业消息
 * @author: cwf
 * @create: 2020-03-24 16:50
 **/
@Service("MsgService")
public class MsgServiceImpl extends BaseServiceImpl implements IMsgService {


    @Override
    public Result getMsg(Map<String, Object> paramMap) {
        Long userId = BaseUtil.objToLong(paramMap.get("userId"), 0L);
//        if (userId.equals(0L)){
//            //查询消息
//            List list = this.findList(" select * ", "from " + TableList.APP_MSG + " where (orgId is null or orgId=1) and status = 'normal' and type='system'");
//            return ResultData.dataResult("success","获取成功",list);
//        }
//        Map<String, Object> relation = findFirstBySql("select o.relation_no from t_org  o  left join tbl_company c on c.orgId=o.id left join tbl_user u on u.companyId =c.id \n" +
//                "where u.id=" + userId);
//        String relationNo="0";
//        if (relation!=null){
//            relationNo= BaseUtil.objToStr(relation.get("relation_no"), "0");
//        }
        String sql = " from "+ TableList.APP_MSG + " where   cstatus = 'normal' AND (orgId=1 or orgId is null) order by FIELD(type,'system','work','property'),create_time desc";
        List sysMsg = findList("select * ", sql);
//        Map<String,Object> resultMap=new HashMap<>();
        System.out.println(sql);
        return ResultData.dataResult("success","获取成功",sysMsg);
    }
}
