package com.goldccm.service.notice.impl;

import com.goldccm.model.compose.PageModel;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.notice.INoticeService;
import com.goldccm.service.user.IUserService;
import com.goldccm.service.visitor.impl.VisitorRecordServiceImpl;
import com.goldccm.util.BaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/16 10:03
 */
@Service("noticeService")
public class NoticeServiceImpl extends BaseServiceImpl implements INoticeService {

    @Autowired
    private IUserService userService;
    Logger logger = LoggerFactory.getLogger(NoticeServiceImpl.class);
    @Override
    public Result findNoticeByUser(Integer userId, Integer pageNum, Integer pageSize) {
        Map<String, Object> user = userService.findById(TableList.USER, userId);
        //String relationNo = BaseUtil.objToStr(user.get("relationNo"),null);
//        String sql = "  from "+TableList.NOTICE +" where relationNo like '%"+relationNo+"%' and castatus = 'normal' order by createDate desc ";
        String sql = "  from tbl_notice n left join t_org o on o.id=n.orgId left join tbl_company c on c.orgId=o.id left join \n" +
                "tbl_user u on u.companyId=c.id where u.id="+userId+" order by n.createDate desc,n.createTime desc  ";
        PageModel pageModel = this.findPage("select n.* ", sql, pageNum, pageSize);
        if (pageModel.getRows().isEmpty()){
            pageModel=findPage("select * ","from "+TableList.NOTICE +" where orgId=1 order by createDate desc,createTime desc ",pageNum, pageSize);
        }
        return ResultData.dataResult("success", "获取成功", pageModel);
    }

    @Override
    public Result findBySidCompany(Integer userId, Integer pageNum, Integer pageSize) throws Exception {
        if (userId == 0) {
            return ResultData.unDataResult("fail", "缺少参数！");
        }
        //获取用户信息
        Map<String, Object> user = userService.findById(TableList.USER, userId);
        //获取用户的公司Id
       if (user.get("companyId")==null){
           return ResultData.unDataResult("fail", "该员工缺少公司！");
       }
        //获取用户companyId
        Integer companyId = BaseUtil.objToInteger(user.get("companyId"),  0);
        //获取用户公司信息
        Map<String, Object> company = userService.findById(TableList.COMPANY, companyId);

        String coloumSql = "select * ";
        String sql = "from ( select a.*,'company' orgType from " + TableList.NOTICE + "a where  cstatus = 'normal' and a.companyId=" + companyId + " ";
        String union = "";
        String companyUnion = "";
        String Suffix  = ")x";
        //获取tbl_user的orgId
        Integer userOrgId = BaseUtil.objToInteger(user.get("orgId"),  0);
        //用户有orgId，则添加根据orgId与sId进行搜索
        if (userOrgId !=  0) {
            union = union(userOrgId);
        }
        //获取用户公司的orgId
        if (company.get("orgId")!=null){
            Integer orgId=BaseUtil.objToInteger(company.get("orgId"),0);
            companyUnion=union(orgId);
        }

        logger.info("查询notice:: {}",sql + union+companyUnion+Suffix);
        PageModel pageModel = this.findPage(coloumSql, sql + union+companyUnion+Suffix, pageNum, pageSize);
        return pageModel.getTotalPage().intValue()==0?ResultData.dataResult("success", "获取成功,暂无数据", pageModel):
        ResultData.dataResult("success", "获取成功", pageModel);
    }

    public String union(Integer orgId){
        String union = "union \n" +
                "select b.*,c.orgType from tbl_notice b\n" +
                "\tleft join t_org c on c.id=b.orgId\twhere b.cstatus = 'normal' and b.orgId in\n" +
                "\t\t(select t2.id\n" +
                "\t\t\tfrom\n" +
                "\t\t\t\t(\n" +
                "\t\t\t\tselect @r AS _id,\n" +
                "        (SELECT @r := sid FROM t_org WHERE id = _id) AS sid,\n" +
                "        @l := @l + 1 AS lvl\n" +
                "\t\t\t\tfrom\n" +
                "\t\t\t\t(select @r := "+orgId+", @l := 0) vars,\n" +
                "\t\t\t\tt_org h\n" +
                "    where @r <> 0\n" +
                "\t\t) t1\n" +
                "join t_org t2 ON t1._id = t2.id\n" +
                "order BY t1.lvl desc)";
    return union;
    }
}
