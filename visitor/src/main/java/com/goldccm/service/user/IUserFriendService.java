package com.goldccm.service.user;


import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
public interface IUserFriendService extends IBaseService {

    /**
     * 通讯录数据
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result findUserFriend(Map<String, Object> paramMap) throws Exception;
    /**
     * 是否好友
     * @param userId
     * @param friendId
     * @return
     * @throws Exception
     */
    Long isFriend(Integer userId, Integer friendId) throws Exception;
    /**
     * 通过手机号添加
     * @return
     * @throws Exception
     */
    Result findPhone(Map<String, Object> paramMap) throws Exception;

    /**
     * 通过真实姓名添加
     * @return
     * @throws Exception
     */
    Result findRealName(Map<String, Object> paramMap) throws Exception;

    /**
     * 查询是否有此好友
     * @param userId
     * @param friendId
     * @return
     * @throws Exception
     */
    Map<String, Object> findFriend(Integer userId,Integer friendId) throws Exception;
    /**
     * 根据真名查找好友
     * @param paramMap
     * @return
     * @throws Exception
     */
//    Result checkFriendByPhoneAndRealName(Map<String, Object> paramMap) throws Exception;
    /**
     * 更新好友申请状态
     * @param userId
     * @param friendId
     * @return
     * @throws Exception
     */
    Integer updateFriendType(Integer userId,Integer friendId,String remark,Integer applyType) throws Exception;

    /**
     * 查询被申请好友列表
     * @param userId
     * @return
     * @throws Exception
     */
    Result findBeAgreeFriend(Integer userId) throws Exception;
    /**
     * 添加通讯录
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result applyUserFriend(Map<String, Object> paramMap) throws Exception;

    /**
     * 删除通讯录
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result deleteUserFriend(Map<String, Object> paramMap) throws Exception;
    /**
     * 根据真名与手机号添加好友
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result addFriendByPhoneAndRealName(Map<String, Object> paramMap) throws Exception;
    /**
     * 同意好友申请
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result agreeFriend(Map<String, Object> paramMap) throws Exception;
    /**
     * 申请好友
     * @param userId
     * @param friendId
     * @return
     * @throws Exception
     */
    Integer addFriend(Integer userId,Integer friendId,String remark,String applyType) throws Exception;
    /** 
     * 我的好友列表与申请我的陌生人列表
     * @param userId
     * @return com.goldccm.model.compose.Result
     * @throws Exception    
     * @author cwf 
     * @date 2019/8/28 11:29
     *  update by cwf  2019/8/28 16:51 cause
     */
    Result findFriendApplyMe(Map<String, Object> userId) throws Exception;
    
}
