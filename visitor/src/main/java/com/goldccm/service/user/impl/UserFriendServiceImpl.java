package com.goldccm.service.user.impl;

import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.WebSocket.IWebSocketService;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.user.IUserFriendService;
import com.goldccm.service.user.IUserService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.ConsantCode;
import com.goldccm.util.phoneUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
@Service("userFriendService")
public class UserFriendServiceImpl extends BaseServiceImpl implements IUserFriendService {
    Logger logger = LoggerFactory.getLogger(UserFriendServiceImpl.class);
    @Autowired
    private IUserService userService;

    @Autowired
    private IBaseDao baseDao;
    @Autowired
    private IWebSocketService webSocketService;
    @Override
    public Result findUserFriend(Map<String, Object> paramMap) throws Exception {
            Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
            if (userId==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少用户参数!");
        }
            //添加好友对登入人状态 2为删除
        String columnSql = "select uf.id ufId,u.id,u.realName,u.phone,u.orgId,u.province,u.city,u.area,u.addr,u.idHandleImgUrl,u.companyId,u.niceName,u.headImgUrl,uf.remark,c.companyName," +
                "(select fuf.applyType from tbl_user_friend fuf where fuf.userId=uf.friendId and fuf.friendId="+userId+") applyType";
        String fromSql = " from " + TableList.USER_FRIEND + " uf " +
                " left join " + TableList.USER + " u on uf.friendId=u.id" +
                " left join " + TableList.COMPANY + " c on c.id=u.companyId"+
                " where uf.userId = '"+userId+"' and uf.applyType=1 and u.id is not null  ";
        logger.info("查询好友{}",columnSql+fromSql);
        List<Map<String,Object>> list = this.findList(columnSql,fromSql);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取成功",list)
                : Result.unDataResult("success","暂无数据");
    }

    @Override
    public Result findPhone(Map<String, Object> paramMap) throws Exception {
        String phone = BaseUtil.objToStr(paramMap.get("phone"),null);
        if(StringUtils.isBlank(phone)){
            return Result.unDataResult("fail","请输入手机号!");
        }
        //通过手机号查询用户信息
        List<Map<String,Object>> user = userService.getListUserByPhone(phone);
        if (user.size()<1){
            return Result.unDataResult("fail","没有找到此手机用户!");
        }
        return ResultData.dataResult("success","查找此用户成功!",user);
    }

    @Override
    public Result findRealName(Map<String, Object> paramMap) throws Exception {
        String realName = BaseUtil.objToStr(paramMap.get("realName"),null);
        if(StringUtils.isBlank(realName)){
            return Result.unDataResult("fail","请输入真实姓名!");
        }
        //通过手机号查询用户信息
        List<Map<String, Object>> user = userService.getUserByRealName(realName);
        if (user.size()<1){
            return Result.unDataResult("fail","没有找到此用户!");
        }
        return ResultData.dataResult("success","查找此用户成功!",user);
    }
    /**
     * 查找是否为好友
     * @param userId
     * @param friendId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @throws Exception
     * @author cwf
     * @date 2019/11/13 14:43
     */
    @Override
    public Map<String, Object> findFriend(Integer userId, Integer friendId) throws Exception {
        String sql = " select u.id,u.realName,u.phone,u.orgId,u.province,u.city,u.area,u.addr,u.idHandleImgUrl,u.companyId,uf.applyType,u.niceName,u.headImgUrl,uf.id ufId"+
                " from " + TableList.USER_FRIEND + " uf " +
                " left join " + TableList.USER + " u on uf.friendId=u.id" +
                " where uf.userId = '"+userId+"' and uf.friendId = '"+friendId+"'";
        System.out.println(sql);
        return findFirstBySql(sql);
    }
    @Override
    public Long isFriend(Integer userId, Integer friendId) throws Exception {
        String fromSql = "from"+ TableList.USER_FRIEND+" where  and userId='"+userId+"'"+" and friendId='"+friendId+"'";
        return baseDao.findExist(fromSql);
    }

    /**
     *  update by cwf  2019/9/18 11:43 Reason:修改 uf.friendId 与uf.userId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Result findBeAgreeFriend(Integer userId) throws Exception {
        String columnSql = " select u.id,u.realName,u.phone,u.orgId,u.province,u.city,u.area,u.addr,u.idHandleImgUrl,u.companyId,uf.applyType,u.niceName,u.headImgUrl";
        String fromSql   = " from " + TableList.USER_FRIEND + " uf " +
                " left join " + TableList.USER + " u on uf.userId=u.id" +
                " where uf.friendId = '"+userId+"' and uf.applyType=0 ";
        List<Map<String,Object>> list = this.findList(columnSql,fromSql);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取申请列表成功",list)
                : Result.unDataResult("success","暂无数据");
    }
    @Override
    public Integer addFriend(Integer userId, Integer friendId, String remark, String applyType, String authentication, String remarkMsg) throws Exception {
        Map<String,Object> userFriend = new HashMap<>();
        userFriend.put("userId",userId);
        userFriend.put("friendId",friendId);
        userFriend.put("remark",remark);
        userFriend.put("applyType",applyType);
        userFriend.put("authentication",authentication);
        userFriend.put("remarkMsg",remarkMsg);
        return this.save(TableList.USER_FRIEND,userFriend);
    }
    @Override
    public Integer updateFriendType(Integer userId,Integer friendId,String remark,Integer applyType) throws Exception {
        String remarkSql="";
        if(remark!=null){
            remarkSql= ", remark ='"+remark+"'";
        }
        String sql = "update " + TableList.USER_FRIEND +" set applyType = '"+applyType+"'"+remarkSql+" where userId = "+userId +
                    " and friendId ="+friendId ;
        return this.deleteOrUpdate(sql);
    }
    /** update by cwf  2019/11/12 16:37 Reason:提交申请时判断是否已删除的好友，如果是，则直接修改状态applyType=1
    */
    //申请好友
    @Override
    public Result applyUserFriend(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        Integer friendId = BaseUtil.objToInteger(paramMap.get("friendId"), null);
        String remark=BaseUtil.objToStr(paramMap.get("remark"),"");
        String authentication=BaseUtil.objToStr(paramMap.get("authentication"),"");
        String remarkMsg=BaseUtil.objToStr(paramMap.get("remarkMsg"),"");
        if (userId==null||friendId==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少参数!");
        }
        if (userId.equals(friendId)){
            return Result.unDataResult(ConsantCode.FAIL,"无法添加自己为好友!");
        }
        //如果存在好友申请
        Map<String,Object> ifUserFriend = findFriend(userId,friendId);
        Map<String,Object> newUserMap=new HashMap<>();
        if (ifUserFriend!=null) {
            String applyType = BaseUtil.objToStr(ifUserFriend.get("applyType"),null);
            //对方对我状态
            Map<String,Object> friendUser = findFriend(friendId,userId);
            if (applyType ==null||"0".equals(applyType)) {
                logger.info("{}申请中的好友!{}",userId,friendId);
                return Result.unDataResult("fail", "申请中的好友!");
            }else if ("1".equals(applyType)){
                logger.info("{}你们已经是好友啦!{}",userId,friendId);
                if (friendUser!=null) {
                    String friendType = BaseUtil.objToStr(friendUser.get("applyType"), null);
                    logger.info("{}对于{}的好友状态{}", friendId, userId, friendType);
                    //ifUserFriend的id
                    long id = BaseUtil.objToLong(ifUserFriend.get("ufId"), null);
                    //如果对方在申请我，直接添加好友
                    if ("0".equals(friendType)) {
                        Integer updateFriendType = updateFriendType(friendId, userId, null, 1);
                        if (updateFriendType > 0 ) {
                            logger.info("{}重新申请好友成功!{}", userId, friendId);
                            return Result.unDataResult("success", "添加好友成功");
                        }
                    }else if ("2".equals(friendType)){

                        logger.info("更新好友状态id：{}",friendType);
                        newUserMap.put("id",id);
                        newUserMap.put("applyType","0");
                        int update = update(TableList.USER_FRIEND, newUserMap);
                        if (update>0){
                            return Result.unDataResult("success", "重新申请好友成功!");
                        }
                    }
                }
                return Result.unDataResult("fail", "你们已经是好友啦!");
                //重新添加好友
            }else if ("2".equals(applyType)){

                //查看对方是否也删除了我
                //如果无数据，返回错误
             if (friendUser!=null){
                 String friendType = BaseUtil.objToStr(friendUser.get("applyType"),null);
                 logger.info("{}对于{}的好友状态{}",friendId,userId,friendType);
                 //ifUserFriend的id
                 long id = BaseUtil.objToLong(ifUserFriend.get("ufId"), null);
                 //如果对方也在申请我
                 if("0".equals(friendType)){
                     Integer updatemyType = updateFriendType(userId, friendId, remark, 1);
                     Integer updateFriendType = updateFriendType(friendId, userId, null, 1);
                     if (updateFriendType>0&&updatemyType>0){
                         logger.info("{}重新申请好友成功!{}",userId,friendId);
                         return Result.unDataResult("success","重新申请好友成功");
                     }
                     //对方没删除我，直接修改回状态为1
                 } else if ("1".equals(friendType)){

                     newUserMap.put("id",id);
                     newUserMap.put("applyType","1");
                     int update = update(TableList.USER_FRIEND, newUserMap);
                     if (update>0){
                         return Result.unDataResult("success", "重新申请好友成功!");
                     }
                     //对方也删除了我，重新发起申请
                 }else if("2".equals(friendType)){
                     logger.info("更新好友状态id：{}",friendType);
                     newUserMap.put("id",id);
                     newUserMap.put("applyType","0");
                     int update = update(TableList.USER_FRIEND, newUserMap);
                     if (update>0){
                         return Result.unDataResult("success", "重新申请好友成功!");
                     }
                 }
                 return Result.unDataResult("fail", "好友数据丢失，请联系管理员!");
             }
            }
        }
            //添加至数据库 用户id 好友id 备注 applytype为0 对方同意后改为1
            Integer save = addFriend(userId,friendId,remark,"0",authentication,remarkMsg);
        if (save > 0){
            //发送websocket给好友
            for (Map.Entry<Object, WebSocketSession> entry: Constant.SESSIONS.entrySet()){
                logger.info("当前在线：user: "+entry.getKey()+" value: "+entry.getValue());
            }

            if (Constant.SESSIONS.containsKey((long)friendId)){
                JSONObject obj = new JSONObject();
                obj.put("fromUserId", userId);
                obj.put("toUserId", friendId);
                obj.put("message", "申请好友");
                obj.put("type", 4);
                String sql="select count(*) c from "+TableList.USER_FRIEND+" where friendId="+friendId+" and applyType=0";
                Map<String, Object> count = findFirstBySql(sql);
                if (count!=null){
                    obj.put("count",count.get("c"));
                }
                webSocketService.sendMessageToUser(Constant.SESSIONS.get((long)friendId), (long)userId, (long)friendId, "申请好友",(long) 4, new TextMessage(obj.toJSONString()));
            }else {
                webSocketService.saveMessage((long)userId,(long)friendId,"申请好友",(long)4);
            }
            return Result.unDataResult("success","提交好友申请成功");
        }
        return Result.unDataResult("fail","提交好友申请失败");
    }
    //删除好友
    @Override
    public Result deleteUserFriend(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        Integer friendId = BaseUtil.objToInteger(paramMap.get("friendId"), null);

        Map<String,Object> friendMap=new HashMap<>();
        if (userId==null||friendId==null){
            return Result.unDataResult("fail","缺少参数!");
        }
        friendMap.put("userId",userId);
        friendMap.put("friendId",friendId);
        friendMap.put("applyType",2);
//        int update = update(TableList.USER_FRIEND, friendMap);//id不能为空
        int update = deleteOrUpdate("update " + TableList.USER_FRIEND + " set applyType=2 where userId=" + userId + " and " +
                " friendId=" + friendId);
        if(update > 0){
            logger.info("{}删除好友{}成功",userId,friendId);
            return  Result.unDataResult("success","删除成功");
        }else{
            return Result.unDataResult("fail","删除失败");
        }
    }

    @Override
    public Result addFriendByPhoneAndRealName(Map<String, Object> paramMap) throws Exception {

            Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
            String phone = BaseUtil.objToStr(paramMap.get("phone"), null);
            String remark = BaseUtil.objToStr(paramMap.get("remark"), null);
            if (userId == null||phone==null) {
            return Result.unDataResult(ConsantCode.FAIL, "缺少用户参数!");
            }

            if (userService.checkPhone(phone)==null){
                return Result.unDataResult(ConsantCode.FAIL, "未找到手机号!");
            }
            Map<String, Object> findPhoneAndReal=userService.FindFriendByPhoneAndRealName((String)paramMap.get("phone"),(String)paramMap.get("realName"));
            if(findPhoneAndReal==null){
                return Result.unDataResult("fail","用户姓名与手机不匹配!");
            }
            Integer friendId = Integer.parseInt(findPhoneAndReal.get("id").toString());
            if (userId.equals(friendId)){
                return Result.unDataResult("fail","无法添加自己为好友!");
            }
            Map<String, Object> addFriendMap = new HashMap<>();
            addFriendMap.put("userId", userId);
            addFriendMap.put("friendId", friendId);
            addFriendMap.put("remark", remark);

        return applyUserFriend(addFriendMap);
    }
    /**
     *  同意好友申请
     *  1、判断是否存在（UserId，friendID）形式的数据，如果有 改变状态为1。如果没有则新增（UserId,friendID）的数据 状态为1
     * @param paramMap
     * @return
     * @throws Exception
     * @author cwf
     * @date 2019/9/18 10:21
     */
    @Override
    public Result agreeFriend(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        Integer friendId = BaseUtil.objToInteger(paramMap.get("friendId"), null);
        String remark = BaseUtil.objToStr(paramMap.get("remark"), "");
        //type=1为添加单独好友，type=2为全部添加
        Integer type = BaseUtil.objToInteger(paramMap.get("type"), null);
        if (userId==null||friendId==null){
            return Result.unDataResult("fail","缺少参数!");
        }

        //查找是否已经为好友 我存在好友
        Map<String,Object> ifUserFriend = findFriend(userId,friendId);
       logger.info("ifUserFriend: "+ifUserFriend);
        //好友存在我
        Long isfriend = isFriend(friendId, userId);
        //我存在好友
        if (ifUserFriend!=null) {
            //已通过验证
            String applyType = BaseUtil.objToStr(ifUserFriend.get("applyType"),null);
            if ("1".equals(applyType)) {
                return Result.unDataResult("fail", "你们已经是好友啦!");
            }
            //未通过验证 已删除的好友
            else if ("0".equals(applyType)||"2".equals(applyType)) {

                updateFriendType(userId,friendId,remark,1);
                updateFriendType(friendId, userId,null, 1);
                return Result.unDataResult("success","通过好友申请成功");
            }
            //如果不存在好友记录
        }else{
            addFriend(userId,friendId,remark,"1","","");
            updateFriendType(friendId,userId,null,1);
            return Result.unDataResult("success","通过好友申请成功");
        }
        return Result.unDataResult("fail","添加好友失敗！");
    }
    //查找好友列表与申请我的陌生人列表
    /** 
     * update by cwf  2019/8/28 16:51 cause
     */
    @Override
    public Result findFriendApplyMe(Map<String, Object> paramMap) throws Exception {
        String userId=BaseUtil.objToStr(paramMap.get("userId"),null);
        if(userId==null){
            return Result.unDataResult("fail","缺少参数！");
        }
        String columnSql = "select uf.userId,uf.friendId,uf.applyType,u.realName,u.phone,u.orgId,u.province,u.city" +
                ",u.area,u.addr,u.idHandleImgUrl,u.companyId,u.niceName,u.headImgUrl";
        String fromSql   = " from " + TableList.USER_FRIEND + " uf " +
                " left join " + TableList.USER + " u on uf.friendId=u.id" +
                " where uf.userId = '"+userId+"'";
        String union=" union all \n" +
                "select uf.userId,uf.friendId,uf.applyType,u.realName,u.phone,u.orgId,u.province,u.city," +
                "u.area,u.addr,u.idHandleImgUrl,u.companyId,u.niceName,u.headImgUrl\n" +
                "from " + TableList.USER_FRIEND + "   uf \n" +
                "left join " + TableList.USER + " u on uf.userid=u.id \n" +
                "where uf.friendId = "+userId+" ";
       logger.info(columnSql+fromSql+union);
        List<Map<String,Object>> list = this.findList(columnSql,fromSql+union);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取成功",list)
                : Result.unDataResult("success","暂无数据");
    }
    @Override
    public Map findFriend(Object userId,Object friendId){
     return findFirstBySql("select * from "+TableList.USER_FRIEND+" where userId="+userId+" and friendId="+friendId);
    }
    @Override
    public Result updateFriendRemark(Map<String, Object> paramMap) {
        String userId=BaseUtil.objToStr(paramMap.get("userId"),null);
        String friendId=BaseUtil.objToStr(paramMap.get("friendId"),null);
        String remark=BaseUtil.objToStr(paramMap.get("remark"),null);
        String detail=BaseUtil.objToStr(paramMap.get("detail"),null);
        if (userId==null||friendId==null){
            return Result.unDataResult("fail","缺少参数！");
        }
        if (remark==null&&detail==null){
            return Result.unDataResult("fail","缺少参数！");
        }

        Map<String, Object> updateMap=new HashMap<>();
        updateMap.put("userId",userId);
        updateMap.put("friendId",friendId);
        updateMap.put("remark",remark);
        updateMap.put("detail",detail);
        StringBuffer sql=new StringBuffer("update "+TableList.USER_FRIEND+" set ");
        if (remark!=null){
            sql.append("remark='"+remark+"' ");
        }
        if (detail!=null){
            sql.append(" detail='"+detail+"' ");
        }
        sql.append(" where userId="+userId+" and friendId="+friendId);

        int update=baseDao.deleteOrUpdate(sql.toString());

        if (update>0){

            return Result.unDataResult("success","修改成功！");
        }
        return Result.unDataResult("fail","修改失败");
    }

    @Override
    public Result newFriend(Map<String, Object> paramMap) {
        String phoneStr = BaseUtil.objToStr(paramMap.get("phoneStr"),",");
        String userId=BaseUtil.objToStr(paramMap.get("userId"),"0");
        String[] phones = phoneStr.split(",");
        logger.info("传入手机号为：{}",phoneStr);
        StringBuffer newPhones=new StringBuffer();
        for (String phone:phones){
            if( phoneUtil.isPhoneLegal(phone)){
                newPhones.append(phone).append(",");
            }
        }
        if (newPhones.length()==0){
            String columsql="select u.id,u.realName,u.phone,u.orgId,u.province,u.city,u.area,u.addr,u.idHandleImgUrl,u.companyId,u.niceName,u.headImgUrl,'同意' applyType, null\n" +
                    " remark ";
          String sql=   "from  "+ TableList.USER_FRIEND +" uf  left join "+ TableList.USER +" u on uf.userId=u.id where uf.friendId = '"+userId+"' and uf.applyType=0";
            logger.info(columsql+sql);
            List <Map<String, Object>> list=findList(columsql,sql);
            return list != null && !list.isEmpty()
                    ? ResultData.dataResult("success","查询用户成功",list)
                    : Result.unDataResult("success","暂无数据");
        }
        newPhones.deleteCharAt(newPhones.length() - 1);
//        logger.info("最终查询的手机号为：{}",newPhones);

        String columsql="select * from ";
        String sql = "(select u.id,u.realName,u.phone,u.orgId,u.province,u.city,u.area,u.addr,u.idHandleImgUrl,u.companyId,u.niceName,u.headImgUrl,'同意' applyType, null\n" +
                " remark  from  "+ TableList.USER_FRIEND +" uf  left join "+ TableList.USER +" u on uf.userId=u.id where uf.friendId = '"+userId+"'  uf.applyType=0 \n" +
                " union " +
                "select u.id,u.realName,u.phone,u.orgId,u.province,u.city,u.area,u.addr,u.idHandleImgUrl,u.companyId,u.niceName,u.headImgUrl," +
                " case (select  applyType from "+ TableList.USER_FRIEND +" uf where uf.friendId=u.id and uf.userId="+userId+" )  when 0 then '申请中' when 1 then '已添加' else '添加' end \n" +
                "\t applyType," +
                "(select  remark from "+ TableList.USER_FRIEND +" uf where uf.friendId=u.id and uf.userId="+userId+" ) remark"+
                " from "+ TableList.USER +"  u where phone in ("+newPhones+") and isAuth='T' " +
                "ORDER BY FIELD(applyType, '同意', '添加', '申请中','已添加'),convert(realName using gbk))x where id >0 and id <>"+userId;
        logger.info(columsql+sql);
        List <Map<String, Object>> list=findList(columsql,sql);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","查询用户成功",list)
                : Result.unDataResult("success","暂无数据");

    }


}
