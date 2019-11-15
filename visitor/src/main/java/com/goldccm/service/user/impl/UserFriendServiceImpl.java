package com.goldccm.service.user.impl;

import com.goldccm.Quartz.QuartzInit;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.user.IUserFriendService;
import com.goldccm.service.user.IUserService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.ConsantCode;
import com.sun.org.apache.regexp.internal.REUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
                " where uf.userId = '"+userId+"' and uf.applyType=1  ";
        List<Map<String,Object>> list = this.findList(columnSql,fromSql);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取通讯录记录成功",list)
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
    public Integer addFriend(Integer userId,Integer friendId,String remark,String applyType) throws Exception {
        Map<String,Object> userFriend = new HashMap<>();
        userFriend.put("userId",userId);
        userFriend.put("friendId",friendId);
        userFriend.put("remark",remark);
        userFriend.put("applyType",applyType);
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
        if (userId==null||friendId==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少参数!");
        }
        //如果存在好友申请
        Map<String,Object> ifUserFriend = findFriend(userId,friendId);
        Map<String,Object> newUserMap=new HashMap<>();
        if (ifUserFriend!=null) {
            String applyType = BaseUtil.objToStr(ifUserFriend.get("applyType"),null);
            if (applyType ==null||"0".equals(applyType)) {
                return Result.unDataResult("fail", "申请中的好友!");
            }else if ("1".equals(applyType)){
                return Result.unDataResult("fail", "你们已经是好友啦!");
                //重新添加好友
            }else if ("2".equals(applyType)){
                //查看对方是否也删除了我
                Map<String,Object> friendUser = findFriend(friendId,userId);
                //如果无数据，返回错误
             if (friendUser!=null){
                 String friendType = BaseUtil.objToStr(friendUser.get("applyType"),null);
                 long id = BaseUtil.objToLong(friendUser.get("ufId"), null);
                 //如果对方也在申请我
                 if("0".equals(friendType)){
                     Integer updatemyType = updateFriendType(userId, friendId, remark, 1);
                     Integer updateFriendType = updateFriendType(friendId, userId, null, 1);
                     if (updateFriendType>0&&updatemyType>0){
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
            Integer save = addFriend(userId,friendId,remark,"0");
        if (save > 0){
            return Result.unDataResult("success","提交好友申请成功");
        }
        return Result.unDataResult("fail","提交好友申请失败");
    }

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
            if (userId==friendId){
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
        System.out.println("ifUserFriend: "+ifUserFriend);
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
            addFriend(userId,friendId,remark,"1");
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
        System.out.println(columnSql+fromSql+union);
        List<Map<String,Object>> list = this.findList(columnSql,fromSql+union);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","获取列表成功",list)
                : Result.unDataResult("success","暂无数据");
    }
}
