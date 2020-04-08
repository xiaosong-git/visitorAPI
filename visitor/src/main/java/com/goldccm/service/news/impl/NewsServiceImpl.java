package com.goldccm.service.news.impl;

import com.goldccm.model.compose.*;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.news.INewsService;
import com.goldccm.util.BaseUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Description:
 * Author: LZ
 * Date:2018/1/22 14:22
 */
@Service("newsServiceImpl")
public class NewsServiceImpl extends BaseServiceImpl implements INewsService {
    @Override
    public Result findByStatus(Map<String, Object> paramMap, Integer pageNum, Integer pageSize) throws Exception {
        Long userId = BaseUtil.objToLong(paramMap.get("userId"), 0L);
//        String sql = "  from "+ TableList.NEWS + " where  newsStatus = '"+Status.APPLY_STATUS_NORMAL+"' order by newsDate desc";
//        PageModel pageModel = this.findPage("select * ",sql,pageNum,pageSize);
//        return ResultData.dataResult("success","获取成功",pageModel);
        if (userId.equals(0L)){
            //查询默认新闻
            String sql = "  from "+ TableList.NEWS + " where  newsStatus = '"+Status.APPLY_STATUS_NORMAL+"' and orgId =1 or orgId is null order by headlines desc, newsDate desc";
            PageModel pageModel = this.findPage("select * ",sql,pageNum,pageSize);
            return ResultData.dataResult("success","获取成功",pageModel);
        }
        //根据用户id查找relationNo
        Map<String, Object> relation = findFirstBySql("select o.relation_no from t_org  o  left join tbl_company c on c.orgId=o.id left join tbl_user u on u.companyId =c.id \n" +
                "where u.id=" + userId);
        String relationNo="0";
        if (relation!=null){
            relationNo=BaseUtil.objToStr(relation.get("relation_no"), "0");
        }
        String sql = "  from ((select * from "+ TableList.NEWS + " where  newsStatus = '"+ Status.APPLY_STATUS_NORMAL+"' AND relationNo like concat(('"+relationNo+"'),'%')" +
                " order by headlines desc,newsDate desc limit 100) " +
                "union" +
                " ( select * from tbl_news where newsStatus = '"+ Status.APPLY_STATUS_NORMAL+"' AND (orgId =1 or orgId is null) order by headlines desc,newsDate desc  limit 100))x";
        System.out.println("select * "+sql);
        PageModel pageModel = this.findPage("select * ",sql,pageNum,pageSize);
        return ResultData.dataResult("success","获取成功",pageModel);
    }

    @Override
    public Result findNews(Map<String, Object> paramMap) {
        Long userId = BaseUtil.objToLong(paramMap.get("userId"), 0L);
        Integer pageNum = BaseUtil.objToInteger(paramMap.get("pageNum"), 0);
        Integer pageSize = BaseUtil.objToInteger(paramMap.get("pageSize"), 10);
        if (userId.equals(0L)){
            //查询默认新闻
            String sql = "  from "+ TableList.NEWS + " where  newsStatus = '"+Status.APPLY_STATUS_NORMAL+"' and orgId is null order by newsDate desc";
            PageModel pageModel = this.findPage("select * ",sql,pageNum,pageSize);
            return ResultData.dataResult("success","获取成功",pageModel);
        }
        //根据用户id查找relationNo
        Map<String, Object> relation = findFirstBySql("select o.relation_no from t_org  o  left join tbl_company c on c.orgId=o.id left join tbl_user u on u.companyId =c.id \n" +
                "where u.id=" + userId);
        String relationNo="0";
        if (relation!=null){
            relationNo=BaseUtil.objToStr(relation.get("relation_no"), "0");
        }
        String sql = "  from ((select * from "+ TableList.NEWS + " where  newsStatus = '"+ Status.APPLY_STATUS_NORMAL+"' AND relationNo like concat(('"+relationNo+"'),'%')" +
                " order by headlines desc,newsDate desc limit 100) " +
                "union" +
                " ( select * from tbl_news where newsStatus = '"+ Status.APPLY_STATUS_NORMAL+"' AND  orgId is null order by headlines desc,newsDate desc  limit 100))x";
        System.out.println("select * "+sql);
        PageModel pageModel = this.findPage("select * ",sql,pageNum,pageSize);
        return ResultData.dataResult("success","获取成功",pageModel);
    }
}
