package com.goldccm.service.checkInWork.impl;

import com.goldccm.model.compose.PageModel;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.impl.BaseDaoImpl;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.checkInWork.FlowService;
import com.goldccm.util.BaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: goldccm
 * @description: 流程控制实现
 * @author: cwf
 * @create: 2019-11-20 16:39
 **/
@Service("flowServiceImpl")
public class FlowServiceImpl extends BaseServiceImpl implements FlowService {
    Logger logger = LoggerFactory.getLogger(FlowServiceImpl.class);
    @Autowired
    private BaseDaoImpl baseDao;
    /**
     *
     * @param paramMap
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/11/20 16:42
     */
    @Transactional(rollbackFor=RuntimeException.class)
    @Override
    public Result createFlow(Map<String, Object> paramMap ) {
        //订单号 需要新建函数next_trans_num() 传入paramName
        //从表中查询编号
        String sql= "SELECT CONCAT(right(DATE_FORMAT(now(),'%Y%m%d') ,8),  LPAD((SELECT next_trans_num('leaveRecordSequence')), 6, '0')) id";
        Map<String, Object> leaveMap = findFirstBySql(sql);
        if (leaveMap==null){
            return Result.unDataResult("fail","系统生成编号错误！请联系管理员");
        }
        String leaveType = BaseUtil.objToStr(paramMap.get("leave_type"),null);
        String startDate = BaseUtil.objToStr(paramMap.get("start_date"),null);
        String startTime = BaseUtil.objToStr(paramMap.get("start_time"),null);
        String endDate = BaseUtil.objToStr(paramMap.get("end_date"),null);
        String endTime = BaseUtil.objToStr(paramMap.get("end_time"),null);
        String approUsers = BaseUtil.objToStr(paramMap.get("appro_user"),null);
        String copyFor = BaseUtil.objToStr(paramMap.get("copy_for"),null);
        if (leaveType==null||startDate==null||startTime==null||endDate==null||endTime==null||approUsers==null){
            return Result.unDataResult("fail","主要参数缺失!");
        }

        Long id = BaseUtil.objToLong(leaveMap.get("id"), null);
        paramMap.entrySet().removeIf(m ->
                (m.getValue()==null ||"".equals(m.getValue()))

        );
         paramMap.put("id",id);
         try {
             //保存请假记录
            save(TableList.WK_LEAVE_RECORD, paramMap);
             /**
              * 根据status 为0表示草稿，1表示已提交审批，2表示审批结束  --待开发
              */
            Map<String, Object> flowNodeMap= new HashMap<>();
            flowNodeMap.put("node_name","请假节点");
            flowNodeMap.put("flow_no",id);
            //保存流程节点
            int flowNodeId = save(TableList.FLOW_NODE, flowNodeMap);
            //根据审批人创建流程
            String[] approUser = approUsers.split(",");

            //批量拼接sql
            StringBuffer PrefixSql = new StringBuffer("insert into " + TableList.FLOW_AUDIT + "(flow_node_id,user_id,user_name,isCopy) values");
            StringBuffer suffixSql = new StringBuffer();
            //保存流程审批人
            for (String userId:approUser){
                suffixSql.append("("+flowNodeId+","+userId+","+"(select realName from "+TableList.USER+" where id="+userId+"),'F'),");
            }
            //保存抄送人 isCopy字段为T
            if (copyFor!=null){
                String[] copyUser = copyFor.split(",");
                for (String copyId:copyUser){
                    suffixSql.append("("+flowNodeId+","+copyId+","+"(select realName from "+TableList.USER+" where id="+copyId+"),'T'),");
                }
            }
            //保存流程抄送人 isCopy字段为T

            baseDao.batchUpdate(PrefixSql + suffixSql.substring(0, suffixSql.length() - 1));
            //

             /**
              * 推送审批人  --待开发
              */
        }catch (Exception e){
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//回滚
            return Result.unDataResult("fail","系统插入数据错误");
        }

        return ResultData.unDataResult("success","流程创建成功");
    }

    /**
     * 流程查看 --已提交
     * @return
     */
    @Override
    public Result checkFlow(Map<String, Object> paramMap,Integer pageNum,Integer pageSize) {
        Long userId = BaseUtil.objToLong(paramMap.get("user_id"), null);
        Integer state = BaseUtil.objToInteger(paramMap.get("state"), null);
        Long companyId = BaseUtil.objToLong(paramMap.get("company_id"), null);
        StringBuffer andSql=new StringBuffer();
        if (userId==null||companyId==null){
            return Result.unDataResult("fail","缺少参数");
        }
        if (state!=null){
            andSql.append(" and state ="+state);
        }
        String coloumSql="select *";
        String fromSql ="from  "+TableList.WK_LEAVE_RECORD+" where user_id="+userId+andSql;
        PageModel page = findPage(coloumSql, fromSql, pageNum, pageSize);
        return ResultData.dataResult("success","查看成功",page);
    }

    /**
     * 流程审批
     * 所有审批人同时同意才能算作流程正常结束
     * 单人拒绝表示流程驳回
     * @author cwf
     * @date 2019/11/21 17:03
     */
    @Override
    public Result approveFlow(Map<String, Object> paramMap) {
        Long id = BaseUtil.objToLong(paramMap.get("id"), null);
        Long user_id = BaseUtil.objToLong(paramMap.get("user_id"), null);
        Long appro_type = BaseUtil.objToLong(paramMap.get("appro_type"), null);
        if(id==null||user_id==null||appro_type==null){
            return Result.unDataResult("fail","参数缺失");
        }
        String updateSql= "update "+TableList.FLOW_NODE+" n,"+TableList.FLOW_AUDIT+" a set  appro_type ="+appro_type+
                " where n.id=a.flow_node_id and flow_no="+id+" and user_id="+user_id;
        int update = deleteOrUpdate(updateSql);
        if (update>0){
            return Result.unDataResult("success","审批成功！");
        }else {
            return Result.unDataResult("success","审批失败！");
        }
    }

    /**
     *  我审批的流程  待处理 已处理 抄送我
     * @param paramMap
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/11/21 17:41
     */
    @Override
    public Result myApprove(Map<String, Object> paramMap,Integer pageNum,Integer pageSize) {
        Long userId = BaseUtil.objToLong(paramMap.get("user_id"), null);
        Long companyId = BaseUtil.objToLong(paramMap.get("company_id"), null);
        if (userId==null||companyId==null){
            return Result.unDataResult("fail","缺少参数");
        }
        //查询流程表单
        String columSql="select x.*," +
                "(select appro_type from tbl_fl_audit  a left join tbl_fl_node n  on n.id =a.flow_node_id " +
                "left join tbl_wk_leave_record lr on lr.id=flow_no " +
                "where a.user_id="+userId+" and company_id="+companyId+") appro_type ";
        String fromSql ="from (\n" +
                "select lr.start_date,lr.end_date,lr.start_time,lr.end_time,flow_no, \n" +
                "(case when sum(appro_type)<0 then '驳回' when  sum(appro_type)%100>0 then  '申请中' else '已同意' end) appro_state" +
                " from "+ TableList.FLOW_AUDIT+ " a  left join "+TableList.FLOW_NODE+" n  " +
                "on n.id =a.flow_node_id \n" +
                "left join "+TableList.WK_LEAVE_RECORD+" lr on lr.id=flow_no\n" +
                "where isCopy='F' and flow_no in (select flow_no from "+ TableList.FLOW_AUDIT+ "  a " +
                "left join "+TableList.FLOW_NODE+" n  on n.id =a.flow_node_id " +
                "left join "+TableList.WK_LEAVE_RECORD+" lr on lr.id=flow_no where a.user_id="+userId+" and company_id="+companyId+")\n" +
                "group by flow_no)x ";
        //查询审批结果
        PageModel page = findPage(columSql, fromSql,pageNum,pageSize);
        return ResultData.dataResult("success","成功",page);
    }

    /**
     * 根据单号查看表单与流程情况
     * @param paramMap
     * @return
     */
    @Override
    public Result approveDetail(Map<String, Object> paramMap) {
        Long id = BaseUtil.objToLong(paramMap.get("id"), null);
        if (id==null){
            return Result.unDataResult("fail","缺少参数");
        }
        String sql="select * from "+TableList.WK_LEAVE_RECORD + " where id="+id;

        Map<String, Object> leaveRecord = findFirstBySql(sql);

        String columSql="select a.* from  ";
        String fromSql="tbl_fl_audit  a left join "+TableList.FLOW_NODE+" n  on n.id =a.flow_node_id left join " +
                ""+TableList.WK_LEAVE_RECORD+" lr on lr.id=flow_no where flow_no="+id;
        List list = findList(columSql, fromSql);
        leaveRecord.put("flow",list);
        return ResultData.dataResult("success","成功",leaveRecord);
    }


}
