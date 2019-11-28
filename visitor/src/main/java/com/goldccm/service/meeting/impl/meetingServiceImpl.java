package com.goldccm.service.meeting.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.goldccm.model.compose.*;
import com.goldccm.service.alipay.impl.AliPayServiceImpl;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.meeting.IMeetingService;
import com.goldccm.service.org.impl.OrgServiceImpl;
import com.goldccm.service.shortMessage.impl.ShortMessageServiceImpl;
import com.goldccm.service.user.impl.UserServiceImpl;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("meetingService")
public class meetingServiceImpl extends BaseServiceImpl implements IMeetingService {
    @Autowired
    private OrgServiceImpl orgService;
    Logger logger = LoggerFactory.getLogger(meetingServiceImpl.class);
    @Autowired
    private AliPayServiceImpl aliPayService;
    @Autowired
    private ShortMessageServiceImpl shortMessageService;
    @Autowired
    private UserServiceImpl userService;
    /**
     * 获取会议室
     * @param paramMap	userId
    * @param pageNum	页码
    * @param pageSize	页尺寸
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/9/26 10:38
     */
    @Override
    public Result getMeeting(Map<String, Object> paramMap,Integer pageNum,Integer pageSize) throws Exception {
        Integer userId= BaseUtil.objToInteger(paramMap.get("userId"),0);
        String type= BaseUtil.objToStr(paramMap.get("type"),"");
        if(userId==0){
            return Result.unDataResult("fail","缺少参数！");
        }
        String orgCode = orgService.findOrgCodeByUserId(userId);
        if (orgCode==null){
            return Result.unDataResult("fail","该公司没有大楼编号");
        }
        String typeSql="";
        if (!("".equals(type))){
            typeSql=" and room_type = "+type;
        }
        String sql = "  from " + TableList.SHARE_ROOM + " sr where  room_orgcode = '"+orgCode+"'"+typeSql;
        PageModel pageModel = this.findPage("select * ", sql, pageNum, pageSize);
        return ResultData.dataResult("success", "获取成功", pageModel);
    }

    @Override
    public Result getRoomStatus(Map<String, Object> paramMap) throws Exception {
        Integer room_id= BaseUtil.objToInteger(paramMap.get("room_id"),0);

        if(room_id==0){
            return Result.unDataResult("fail","缺少参数！");
        }
        //拼接5天内时间段
        String sql = " from tbl_share_room sr \n" +
                "left join "+TableList.ROOM_APPLY_RECORD+" rar on sr.id=rar.room_id\n" +
                "where sr.id="+room_id+" and record_status<>4"+Constant.ROOM_STATUS_RESERVE+" and apply_date>=date_format(now(),'%Y-%m-%d') \n " +
                "and apply_date<date_add(now(),interval 5 day)" +
                "GROUP BY sr.id ,apply_date";
//        List list=findList("select apply_date,sr.*,GROUP_CONCAT(time_interval) time_interval ",sql);
        List list=findList("select * ",sql);
        System.out.println(sql);
        return list.isEmpty()? ResultData.dataResult("success", "暂无预定数据", list):
                ResultData.dataResult("success", "获取成功", list);
    }

    @Override
    public Result reserveMeeting(Map<String, Object> paramMap) throws Exception {
        //获取参数
        Integer room_id= BaseUtil.objToInteger(paramMap.get("room_id"),0);
        Integer userId= BaseUtil.objToInteger(paramMap.get("userId"),0);
        String apply_date= BaseUtil.objToStr(paramMap.get("apply_date"),null);
        BigDecimal apply_start_time= BaseUtil.objToBigdecimal(paramMap.get("apply_start_time"),null);
        BigDecimal apply_end_time= BaseUtil.objToBigdecimal(paramMap.get("apply_end_time"),null);
        String time_interval= BaseUtil.objToStr(paramMap.get("time_interval"),null);
        if (room_id==0||userId==0||apply_date==null||apply_start_time==null||apply_end_time==null){
            return Result.unDataResult("fail","缺少参数！");
        }
        Map<String, Object> saveMap=new HashMap<>();
        //存入查询
        saveMap.put("room_id",room_id);
        saveMap.put("apply_userid",userId);
        saveMap.put("apply_date",apply_date);
        saveMap.put("apply_start_time",apply_start_time);
        saveMap.put("apply_end_time",apply_end_time);
        saveMap.put("record_status",Constant.ROOM_STATUS_RESERVE);
        saveMap.put("time_interval",time_interval);
        saveMap.put("create_time", DateUtil.getSystemTime());
        //获取房间
        Map<String,Object> room=  findById(TableList.SHARE_ROOM,room_id);
        if (room.get("room_status").equals("-1")){
            return Result.unDataResult("fail","该房间正在维护");
        }
        //获取房间每小时价格
        BigDecimal room_price= BaseUtil.objToBigdecimal(room.get("room_price"),BigDecimal.ZERO);
        //总价=时间*小时数
        BigDecimal price= room_price.multiply(apply_end_time.subtract(apply_start_time));
        saveMap.put("price", price);
//        for (Map.Entry<String,Object> entry:saveMap.entrySet()){
//            System.out.println("key:"+entry.getKey()+"  --value:"+ entry.getValue());
//        }

        //判断是否已被预定
        boolean isReserve= isReserve(room_id,apply_date,apply_start_time,apply_end_time);
        if (isReserve){
            return Result.unDataResult("fail","该时间段已被预定");
        }
        //存入数据库
        int id=save(TableList.ROOM_APPLY_RECORD,saveMap);
        //储存数据库成功，发送websocket给大楼管理员
        if (id>0){
            //查询用户信息
            Map<String,Object> userMap= findByUserId(userId);

            String userName= BaseUtil.objToStr(userMap.get("realName"),"");
            String phone= BaseUtil.objToStr(userMap.get("phone"),"");
            //时间换算
            String start_time=String.valueOf(apply_start_time).replaceAll("\\.0",":00").replaceAll("\\.5",":30");
            String end_time=String.valueOf(apply_end_time).replaceAll("\\.0",":00").replaceAll("\\.5",":30");
            System.out.println(start_time);
            System.out.println(end_time);

//            System.out.println(start_time+","+end_time);
            Integer managerId=  BaseUtil.objToInteger(room.get("room_manager"),null);
            if (managerId!=null){
            String room_name=  BaseUtil.objToStr(room.get("room_name"),null);
            String room_addr=  BaseUtil.objToStr(room.get("room_addr"),null);
            String  companyName= BaseUtil.objToStr(userMap.get("companyName"),"");

            //用户名：于系统时间+预定了预定年月+开始时间——结束时间的room_name,房间地址为_room_addr
            String msg=companyName+"的用户："+userName+"于 "+ DateUtil.getCurDate()+" 预定了会议室 " +room_name+"，地址："+room_addr+
                    "，会议时间："+apply_date +" "+start_time+"——"+end_time  +"，用户手机为："+phone;
            //发送给管理员，管理员不在线则存储数据库

            sendManage(msg,managerId);
            }
        }
        return Result.ResultCode("success","操作成功",String.valueOf(id));
    }

    @Override
    public boolean isReserve (Integer room_id,String apply_date,BigDecimal start_time,BigDecimal end_time){
        String sql="select * from "+TableList.ROOM_APPLY_RECORD+" where apply_date='"+apply_date+"' and record_status=1 " +
                "and room_id="+room_id+" and apply_start_time<"+end_time+" and apply_end_time>"+start_time+"";

       Map<String,Object> map= findFirstBySql(sql);
        if (map!=null){
            return true;
        }
        return false;
    }
    @Override
    public Result getMyReserveMeeting(Map<String, Object> paramMap,Integer pageNum,Integer pageSize) throws Exception {
        Integer userId= BaseUtil.objToInteger(paramMap.get("userId"),null);
        Integer record_status= BaseUtil.objToInteger(paramMap.get("record_status"),null);
        String apply_date= BaseUtil.objToStr(paramMap.get("apply_date"),null);
        if(userId==null){
            return Result.unDataResult("fail","缺少参数！");
        }
        String and="";
        //查询该时间段之后的信息
        if (apply_date!=null){
            and=" and apply_date>=date_format('"+apply_date+"','%Y-%m-%d') ";
        }
        String sqlRecords=" and record_status ="+record_status;
        if(record_status==null||record_status==0){
            sqlRecords="";
        }
        String coloumSql="select rar.*,bd.trade_status,room_name,room_addr,room_size,room_type,room_short_content,room_image,room_open_time," +
                "room_close_time,room_price,room_manager,room_status,room_orgcode,room_cancle_hour,room_percent,room_mode";

        String fromsql = "  from " + TableList.ROOM_APPLY_RECORD + " rar\n" +
                "left join "+TableList.SHARE_ROOM+" sr on sr.id = rar.room_id " +
                "left join "+TableList.BILL_DETAI+" bd on rar.trade_no=bd.trade_no " +
                "where  apply_userid = "+userId+"" +
                sqlRecords+and+"  order by rar.create_time desc ";
        System.out.println(coloumSql+fromsql);
        PageModel pageModel = this.findPage(coloumSql, fromsql, pageNum, pageSize);
        return ResultData.dataResult("success", "获取成功", pageModel);
    }
    /**
     * 取消预定会议室，
     * if(是否在规定时间内取消预定==true){
     *     if（是否已付款==true）{
     *         退款
     *     }else{
     *        修改房间状态为取消预定
     *     }
     *     return 成功取消预定
     * }else{
     *     return 取消预定失败
     * }
     * 还原会议室状态，修改支付状态。
     * @param paramMap record_id=tbl_apply_record表的id userid =用户id
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/9/26 16:29
     */
    @Override
    public Result cancleMeeting(Map<String, Object> paramMap) throws Exception {
        Integer id= BaseUtil.objToInteger(paramMap.get("record_id"),null);
        Integer userId= BaseUtil.objToInteger(paramMap.get("userId"),null);
        if(id==null||userId==null){
            return Result.unDataResult("fail","缺少参数！");
        }
        //查询是否超时
        String checkSql="select record_status,room_cancle_hour,apply_date, apply_start_time,apply_end_time,room_manager,room_name" +
                ",room_addr,bd.trade_no,trade_status from "+  TableList.ROOM_APPLY_RECORD+" rar " +
                "left join " + TableList.SHARE_ROOM+ " sr on rar.room_id=sr.id " +
                "left join "+TableList.BILL_DETAI+ " bd on bd.trade_no=rar.trade_no where rar.id="+id+" and  \n" +
                "CONCAT(rar.apply_date,' ',SEC_TO_TIME((rar.apply_start_time-sr.room_cancle_hour)*3600))>date_format(SYSDATE(),'%Y-%m-%d %H:%i:%s')";
        System.out.println(checkSql);
        Map<String, Object> recordMap= findFirstBySql(checkSql);
        if (recordMap==null){
            return Result.unDataResult("fail","超出取消预定时间");
        }
        Integer record_status= BaseUtil.objToInteger(recordMap.get("record_status"),1);

        if (record_status==Constant.ROOM_STATUS_CANCLE){
           return Result.unDataResult("fail","该房间已取消预定，请勿重复操作");
        }
        //查询订单并进行退款操作
        String tradeNo = BaseUtil.objToStr(recordMap.get("trade_no"),"");
        Map<String, Object> updateMap=new HashMap<>();
        updateMap.put("id",id);
        updateMap.put("record_status",Constant.ROOM_STATUS_CANCLE);
        int update=0;
        boolean refundSuc=false;
        Result result=Result.unDataResult("success","取消成功");
        //如果没有订单号则表示没有付款不进行退款，更新表状态并return
        if(tradeNo!=""){
            logger.info("apply_record 有tradeNo："+tradeNo);
                //查询阿里订单状态
                AlipayTradeQueryResponse response = aliPayService.checkTrade(tradeNo);
                int aliTradeStatus =1;
                String totalAmount="";
                //返回状态为null时表示支付宝没有订单号
                if (response!=null){
                    logger.info("阿里有 response "+response);
                    //获取转为int类型的支付宝回应状态
                    aliTradeStatus = aliPayService.setTradeStatus(response.getTradeStatus());
                    //如果订单状态2，则表示可以退款
                    if (aliTradeStatus==2){
                        logger.info("阿里有 response且 aliTradeStatus= "+aliTradeStatus);
                        //获取支付宝传回的金额
                        totalAmount = response.getTotalAmount();
                        //退款
                        AlipayTradeRefundResponse refund = aliPayService.Refund(tradeNo, totalAmount);
                        //支付宝传回FundChange为Y表示退款成功
                        if( "Y".equals(refund.getFundChange())){
                            logger.info("退款getFundChange= "+refund.getFundChange());
                            //更新apply_record与bill_detail两表状态
                            String  sql="update "+TableList.BILL_DETAI+" bd,"+TableList.ROOM_APPLY_RECORD+" rar set " +
                                    "bd.trade_status="+Constant.ROOM_STATUS_CANCLE+",rar.record_status="+Constant.ROOM_STATUS_CANCLE+"" +
                                    " where bd.trade_no=rar.trade_no and rar.trade_no='"+tradeNo+"'";
                             update = deleteOrUpdate(sql);
                            refundSuc=true;
                             result=Result.unDataResult("success","取消预定并退款成功");
                        }
                    }
                }

        }
        //如果refundsuc=false
        if (!refundSuc){
            update = update(TableList.ROOM_APPLY_RECORD, updateMap);
        }
        String room_cancle_hour= BaseUtil.objToStr(recordMap.get("room_cancle_hour"),null);
        //如果update>0 取消预定成功
        if (update>0){
            //查询用户信息
            Map<String,Object> userMap= findByUserId(userId);
            //获取房间
            Integer room_manager= BaseUtil.objToInteger(recordMap.get("room_manager"),null);
            if (room_manager!=null) {
                String userName= BaseUtil.objToStr(userMap.get("realName"),"");
                String  phone= BaseUtil.objToStr(userMap.get("phone"),"");
                String  companyName= BaseUtil.objToStr(userMap.get("companyName"),"");
                String  apply_date= BaseUtil.objToStr(recordMap.get("apply_date"),"");
                String  start_time= BaseUtil.objToStr(recordMap.get("apply_start_time"),"");
                String  end_time= BaseUtil.objToStr(recordMap.get("apply_end_time"),"");
                String room_name = BaseUtil.objToStr(recordMap.get("room_name"), "");
                String room_addr = BaseUtil.objToStr(recordMap.get("room_addr"), "");
                //时间换算
                start_time = String.valueOf(start_time).replaceAll("\\.0", ":00").replaceAll("\\.5", ":30");
                end_time = String.valueOf(end_time).replaceAll("\\.0", ":00").replaceAll("\\.5", ":30");
                //公司名+用户名：于系统时间+预定了预定年月+开始时间——结束时间的room_name,房间地址为_room_addr
                String msg = companyName + "的用户：" + userName + "于： " + DateUtil.getCurDate() + " 取消预定会议室 " + room_name + "，地址：" + room_addr +
                        "，会议时间：" + apply_date + " " + start_time + "——" + end_time + "，用户手机为：" + phone;
                sendManage(msg, room_manager);
            }
            return result;

        }
        return Result.unDataResult("fail","超出规定取消时间:"+room_cancle_hour+"小时");
    }
    @Override
    public Result reTryReserve(Map<String, Object> paramMap) throws Exception {
        Integer id= BaseUtil.objToInteger(paramMap.get("id"),null);
        Integer room_id= BaseUtil.objToInteger(paramMap.get("room_id"),0);
        String apply_date= BaseUtil.objToStr(paramMap.get("apply_date"),null);
        BigDecimal apply_start_time= BaseUtil.objToBigdecimal(paramMap.get("apply_start_time"),null);
        BigDecimal apply_end_time= BaseUtil.objToBigdecimal(paramMap.get("apply_end_time"),null);
        String time_interval= BaseUtil.objToStr(paramMap.get("time_interval"),null);
        boolean isReserve= isReserve(room_id,apply_date,apply_start_time,apply_end_time);
        if (isReserve){
            return Result.unDataResult("fail","该时间段已被预定");
        }
        if (room_id==0||room_id==0||apply_date==null||apply_start_time==null||apply_end_time==null||time_interval==null){
            return Result.unDataResult("fail","缺少参数！");
        }

    String sql = "update " + TableList.ROOM_APPLY_RECORD +
            " set record_status="+ Constant.ROOM_STATUS_RESERVE +",room_id= "+room_id+
            ",apply_date= '"+apply_date+"',apply_start_time='"+apply_start_time+"'" +
            ",apply_end_time='"+apply_end_time+"',time_interval= '"+time_interval+"'"+
            " where id="+id;
        int update=deleteOrUpdate(sql);
        if (update>0){
            return Result.success();
        }

        return Result.unDataResult("fail","重新预定失败");
    }

    @Override
    public Result statistics(Map<String, Object> paramMap) {
        String start_date= BaseUtil.objToStr(paramMap.get("start_date"),"2019-01-01");
        String end_date= BaseUtil.objToStr(paramMap.get("end_date"),DateUtil.getCurDate());
        String userId= BaseUtil.objToStr(paramMap.get("userId"),null);
        if(userId==null){
            return Result.unDataResult("fail","用户不能为空");
        }

       String sql = "select sum(apply_end_time-apply_start_time) totalTime,sum(price) payCount from tbl_room_apply_record"+
        " where apply_userid="+userId+" and apply_date between '"+start_date+"' and '"+end_date+"'  and record_status="+Constant.ROOM_STATUS_RESERVE;
       Map<String, Object> map= findFirstBySql(sql);
       if (map.isEmpty()||map==null){
           return Result.unDataResult("sucsess","暂无预定数据");
       }
       return ResultData.dataResult("sucsess","获取成功",map);
    }
    //发送给大楼管理员 //不在线时推送
    public void sendManage(String msg,Integer managerId) throws Exception{
        if (Constant.SESSIONS.containsKey(managerId)){
            System.out.println("大楼管理员在线");
            JSONObject obj=new JSONObject();
            obj.put("message",msg);
            obj.put("fromUserId",0);
            obj.put("toUserId",managerId);
            obj.put("type",Constant.MASSEGETYPE_NOMAL);
            //将预定信息发送给管理员
            Constant.SESSIONS.get(managerId).sendMessage(new TextMessage(obj.toJSONString()));

        }else {
            //存储数据库
            Map<String, Object> message = new HashMap<>();
            message.put("message", msg);
            message.put("fromUserId", 0);
            message.put("toUserId", managerId);
            message.put("type", Constant.MASSEGETYPE_NOMAL);
            message.put("update_time", DateUtil.getSystemTime());
            int save = save(TableList.USER_MESSAGE, message);
            if (save > 0) {
                //推送
                Map<String, Object> userUser = userService.getUserByUserId(managerId);
                if (userUser!=null){
                String deviceToken = BaseUtil.objToStr(userUser.get("deviceToken"), null);
                if (deviceToken != null&&!"".equals(deviceToken)) {
                    String isOnlineApp = BaseUtil.objToStr(userUser.get("isOnlineApp"), "T");
                    String notification_title = "房间取消预定通知";
                    String msg_content = "【朋悦比邻】您好，您管理的大楼有房间被取消预定！，请登入app查收!";
                    String deviceType = BaseUtil.objToStr(userUser.get("deviceType"), "0");
                    shortMessageService.YMNotification(deviceToken, deviceType, notification_title, msg_content, isOnlineApp);
                }
            }
            }
        }
    }

  @Override
  public Map<String, Object> findByUserId(Integer userId){
        String sql="select tu.phone,tu.realName,tc.companyName from "+TableList.USER +" tu \n" +
                "LEFT JOIN "+TableList.COMPANY+" tc on  tc.id=companyId\n" +
                "where tu.id = "+ userId;
      Map<String, Object> map= findFirstBySql(sql);
      return  map;

  }

    /**
     * 增加房间 填入房间信息
     * @param paramMap
     * @return
     */
    @Override
    public Result addRoom(Map<String, Object> paramMap) {

        int save = save(TableList.SHARE_ROOM, paramMap);
        return save>0?Result.success():Result.fail() ;
    }

    /**
     * 根据大楼编码获取会议室预定信息
     * @param paramMap
     * @return
     */
    @Override
    public Result getFromOrgCode(Map<String, Object> paramMap,Integer pageNum,Integer pageSize) {
        String orgCode= BaseUtil.objToStr(paramMap.get("orgCode"),"");
        if (pageNum != 1) {
            return Result.unDataResult("fail", "页数不对!");
        }
        if ("".equals(orgCode)){
            return Result.unDataResult("fail","缺少大楼编码");
        }
        Result isPosp=isPosp(paramMap);
        String sign = BaseUtil.objToStr(isPosp.getVerify().get("sign"),"fail");
        if ("fail".equals(sign)){
            return isPosp;
        }

        //record_status 1 预定未付款 2 已付款可退款 3 已付款不可退款 4 已退款
        String coloumSql="select rar.room_id,apply_userid,u.soleCode ,u.realName ,apply_date,apply_start_time,apply_end_time,rar.id record_id,record_status,price,\n" +
                "room_addr,room_open_time,room_close_time,room_status,room_orgcode,room_mode,isFlag ";
        String fromSql="from "+TableList.ROOM_APPLY_RECORD+" rar left join "+TableList.SHARE_ROOM+" sr on rar.room_id=sr.id\n" +
                "left join "+TableList.USER+" u on u.id =rar.apply_userid  \n" +
                "where room_orgcode ='"+orgCode+"' and record_status <>1 AND isFlag='F' and apply_date >=DATE_FORMAT(SYSDATE(),'%Y-%m-%d')";
        logger.info("根据大楼编号获取会议室sql:\n{}",coloumSql+fromSql);
        PageModel page = findPage(coloumSql, fromSql, pageNum, pageSize);
        return ResultData.dataResult("success","获取大楼会议室预定信息成功",page);
    }
    /**
     * 确认拉取会议室预定信息
     * 修改isflag 并存入上位机编号
     * @param paramMap
     * @author cwf
     * @date 2019/11/21 11:00
     */
    @Override
    public Result getFromOrgCodeConfirm(Map<String, Object> paramMap) {
        String idStr = BaseUtil.objToStr(paramMap.get("idStr"), "");
        Result isPosp=isPosp(paramMap);
        if ("".equals(idStr)){
            return Result.unDataResult("fail","缺少id字符串");
        }

        String sign = BaseUtil.objToStr(isPosp.getVerify().get("sign"),"fail");
        if ("fail".equals(sign)){
            return isPosp;
        }
        String desc = BaseUtil.objToStr(isPosp.getVerify().get("desc"),"0");
        int update = deleteOrUpdate("update " + TableList.ROOM_APPLY_RECORD + " set isFlag='T',ext1='"+desc+"' where id in (" + idStr + ")");
        if (update>0){
            return Result.success();
        }
        return Result.fail();
    }
    public Result isPosp(Map<String, Object> paramMap){
        String orgCode= BaseUtil.objToStr(paramMap.get("orgCode"),"");
        String pospCode= BaseUtil.objToStr(paramMap.get("pospCode"),"");
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
        return Result.unDataResult("success",pospCode);
    }

}
