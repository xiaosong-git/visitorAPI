package com.goldccm.service.user;


import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.List;
import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
public interface IUserService extends IBaseService {

    /**
     * 通过账号查看用户信息
     * @param phone
     * @return
     */
    Map<String, Object> getUserByPhone(String phone);

    /**
     * 通过用户id查看用户信息
     * @param userId
     * @return
     */
    Map<String,Object> getUserByUserId(Integer userId);

    /**
     * 通过真实名字查看用户信息
     * @param realName
     * @return
     */
    List<Map<String, Object>> getUserByRealName(String realName);

    /**
     * 通过账号查看用户信息
     * @param phone
     * @return
     */
    List<Map<String, Object>> getListUserByPhone(String phone);

    boolean verifyPhone(String phone);

    boolean isVerify(Integer userId);

    Map<String,Object> getUserByToken(String token);

    /**
     * 手势密码登录
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result login(Map<String, Object> paramMap) throws Exception;

    /**
     * 用户注册，第一步
     * @Author linyb
     * @Date 2017/4/10 23:27
     */
    Result registerOrigin(Map<String, Object> paramMap) throws Exception;

    int createUser(String phone, String realName) throws Exception;

    /**
     * 实人认证
     * @Author linyb
     * @Date 2017/4/11 14:23
     */
    Result verify(Map<String, Object> paramMap);

    /**
     * 根据用户信息和token获取用户信息
     * @Author linyb
     * @Date 2017/4/11 15:46
     */
    Map<String,Object> getUserByUserToken(Integer userId, String token);
    /**
     * 修改系统密码
     * @Author linyb
     * @Date 2017/4/11 16:28
     */
    Result updateSysPwd(Map<String, Object> paramMap);

    /**
     * 修改手势密码
     * @param paramMap
     * @return
     */
    Result updateGesturePwd(Map<String, Object> paramMap);

    /**
     * 修改登录账号,通过短信验证码
     * @param paramMap
     * @return
     */
    Result updatePhone(Map<String, Object> paramMap);
    /**
     * 找回系统密码,通过短信验证码
     * @Author linyb
     * @Date 2017/4/11 16:46
     */
    Result forgetSysPwd(Map<String, Object> paramMap);

    /**
     * 设定手势密码
     * @Author LZ
     * @Date 2017-7-26
     */
    Result setGesturePwd(Map<String, Object> paramMap);
    /**
     * 修改头像 昵称
     * @Author linyb
     * @Date 2017/4/11 18:41
     */
    Result updateNick(Map<String, Object> paramMap);
    /**
     * 根据用户的id获取对应的密钥
     * @Author linyb
     * @Date 2017/4/11 20:42
     */
    String getWorkKeyByUserId(Integer userId);

    /**
     * 判断是否存在某个身份证
     * @param idNo 身份证号码
     * @return
     * @throws Exception
     */
    boolean isExistIdNo(String idNo) throws Exception;

    boolean isExistIdNo(String userId, String idNo) throws Exception;

    /**
     * 更新用户在缓存中的Token和实名
     * @param userId 用户ID
     * @param token 令牌
     * @param isAuth 是否实名
     * @throws Exception
     */
    void updateRedisTokenAndAuth(String userId, String token, String isAuth) throws Exception;

    /**
     * 判断是否短时间重复实名
     * @param userId 用户Id
     * @param cardNo 卡号
     * @return
     * @throws Exception
     */
    boolean isRepeatVerify(String userId, String cardNo) throws Exception;

    /**
     * 修改用户设置交易密码的状态
     * @param userId 用户Id
     * @param status 状态 T：设置 F：未设置
     * @return
     * @throws Exception
     */
    Integer updateUserSetTransPwdStatus(String userId, String status) throws Exception;

    /**
     * 通过短信验证码登录
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result loginByVerifyCode(Map<String, Object> paramMap) throws Exception;

    /**
     * 获取用户信息
     * @param realName
     * @param companyId
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> findByUser(String realName, Integer companyId) throws Exception;

    /**
     * 查询同一公司的员工
     * @param paramMap
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    Result findCompanyId(Map<String, Object> paramMap, Integer pageNum, Integer pageSize) throws Exception;

    /**
     * 添加员工
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result addUser(Map<String, Object> paramMap) throws Exception;

    /**
     * 删除员工
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result deleteUser(Map<String, Object> paramMap) throws Exception;

    /**
     * 查询公司人员
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result findVisitorId(Map<String,Object> paramMap) throws Exception;

    /**
     * 修改用户公司id，角色
     * @param paramMap
     * @return
     * @throws Exception
     */
    Result updateCompanyIdAndRole(Map<String,Object> paramMap) throws Exception;
    /**
     * 根据用户电话与姓名添加好友
     * @param phone
     * @param realName
     * @return
     * @throws Exception
     */
    Map<String, Object> FindFriendByPhoneAndRealName(String phone, String realName) throws Exception;
    /**
     * 检查是否存在此手机号
     * @param phone
     * @return java.lang.Integer
     * @throws Exception
     * @author chenwf
     * @date 2019/7/26 16:49
     */
    Long checkPhone(String phone) throws Exception;
    /**
     * 批量检查是否存在此手机号
     * @param paramMap
     * @return java.lang.Integer
     * @throws Exception
     * @author chenwf
     * @date 2019/7/26 16:49
     */
    Result findIsUserByPhone(Map<String, Object> paramMap)  throws Exception;
    /**
     * 退出app 删除deviceToken
     * @param paramMap
     * @return java.lang.Integer
     * @throws Exception
     * @author chenwf
     * @date 2019/9/24 16:49
     */
    Result appQuit(Map<String, Object> paramMap);
    //改变用户的实人认证状态
    Result modify(Map<String, Object> paramMap);

    /**
     * 通过短信修改密码
     * @param paramMap
     * @return
     */
    Result forget(Map<String, Object> paramMap);

}
