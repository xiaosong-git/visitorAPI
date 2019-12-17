package com.goldccm.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.*;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.code.ICodeService;
import com.goldccm.service.key.IKeyService;
import com.goldccm.service.notice.INoticeService;
import com.goldccm.service.notice.INoticeUserService;
import com.goldccm.service.org.IOrgService;
import com.goldccm.service.param.IParamService;
import com.goldccm.service.password.IPasswordService;
import com.goldccm.service.user.IUserAccountService;
import com.goldccm.service.user.IUserService;
import com.goldccm.util.Base64;
import com.goldccm.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author linyb
 * @Date 2017/4/3 16:51
 */
@Service("userService")
public class UserServiceImpl extends BaseServiceImpl implements IUserService {
    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private IBaseDao baseDao;
    @Autowired
    private IUserAccountService userAccountService;
    @Autowired
    private ICodeService codeService;
    @Autowired
    private IPasswordService passwordService;
    @Autowired
    private IParamService paramService;
    @Autowired
    private IKeyService keyService;
    @Autowired
    private INoticeUserService noticeUserService;
    @Autowired
    private INoticeService noticeService;
    @Autowired
    private IOrgService orgService;
    @Override
    public Map<String, Object> getUserByPhone(String phone) {
        String sql = " select * from "+ TableList.USER +" where phone = '"+phone+"' ";
        return baseDao.findFirstBySql(sql);
    }

    @Override
    public Map<String, Object> getUserByUserId(Integer userId) {
        String sql = " select * from "+ TableList.USER +" where id = '"+userId+"' ";
        return baseDao.findFirstBySql(sql);
    }

    @Override
    public List<Map<String, Object>> getUserByRealName(String realName) {
        String coloumSql = " select u.id,u.realName,u.phone,u.orgId,u.province,u.city,u.area,u.addr,u.idHandleImgUrl,u.companyId";
        String fromSql = " from "+ TableList.USER +" u" +
                " where u.realName = '"+realName+"' ";
        return baseDao.findList(coloumSql, fromSql);
    }

    @Override
    public List<Map<String, Object>> getListUserByPhone(String phone) {
        String coloumSql = " select u.id,u.realName,u.phone,u.orgId,u.province,u.city,u.area,u.addr,u.idHandleImgUrl,u.companyId";
        String fromSql = " from "+ TableList.USER +" u" +
                " where u.phone = '"+phone+"' ";
        return baseDao.findList(coloumSql, fromSql);
    }

    @Override
    public boolean verifyPhone(String phone) {
        return this.getUserByPhone(phone) == null;
    }

    @Override
    public boolean isVerify(Integer userId) {
        Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
        String key = userId + "_isAuth";
        //redis修改
        String isAuth = RedisUtil.getStrVal(key, apiNewAuthCheckRedisDbIndex);
        if(StringUtils.isBlank(isAuth)){
            //缓存中不存在，从数据库查询
            Map<String,Object> user = baseDao.findById(TableList.USER,userId);
            if(user == null){
                return false;
            }
            Object verifyObj = user.get("isAuth");
            if (verifyObj == null) return false;
            isAuth = verifyObj+"";
            //redis修改
            RedisUtil.setStr(key, isAuth, apiNewAuthCheckRedisDbIndex, null);
        }
        return "T".equalsIgnoreCase(isAuth);
    }

    @Override
    public Map<String, Object> getUserByToken(String token) {
        String sql = " select * from "+ TableList.USER +" where token = '"+token+"' ";
        return baseDao.findFirstBySql(sql);
    }

    @Override
    public Result login(Map<String, Object> paramMap) throws Exception {
        String phone = paramMap.get("phone")+"";
        Map<String,Object>  user = this.getUserByPhone(phone);
        if(user == null){
            return  Result.unDataResult("fail","用户不存在");
        }
        Integer userId = Integer.parseInt(user.get("id")+"");
        //判断密码输入次数是否超出限制，超出无法登录
        if(passwordService.isErrInputOutOfLimit(userId.toString(),Status.PWD_TYPE_SYS)){
            String limitTime = paramService.findValueByName("errorInputSyspwdWaitTime");
            codeService.sendMsg(user.get("loginName")+"", 2,null,null,null,null);
            return  Result.unDataResult("fail","由于您多次输入错误密码，为保证您的账户与资金安全，"+limitTime+"分钟内无法登录");
        }
        Map<String,Object> userAccount = userAccountService.findUserAccountByUser(userId);
        if(userAccount == null){
            return  Result.unDataResult("fail","找不到用户的账户信息");
        }
        String loginStyle = null;
        Object obj = paramMap.get("style");
        if(obj == null){
            //默认选择：密码登录
            loginStyle = Status.LOGIN_STYLE_PWD;
        }else{
            loginStyle = obj.toString();
        }
        String password = paramMap.get("sysPwd")+"";//用户输入密码
        String dbPassword = null;
        if(Status.LOGIN_STYLE_PWD.equals(loginStyle)){
            dbPassword = userAccount.get("sysPwd")+"";//正确密码
        }else{
            dbPassword = userAccount.get("gesturePwd")+"";//正确密码
        }
        if(password.equals(dbPassword)){
            //重置允许用户输入错误密码次数
            passwordService.resetPwdInputNum(userId.toString(), Status.PWD_TYPE_SYS);
            String cstatus = userAccount.get("cstatus")+"";
            if("normal".equals(cstatus)){
                Map<String,Object> userUpdate = new HashMap<String, Object>();
                userUpdate.put("id",userId);
                userUpdate.put("token", UUID.randomUUID().toString());
                this.update(TableList.USER,userUpdate);

                user = this.findById(TableList.USER, userId);
                //实名有效日期过了
                if ("T".equals(user.get("isAuth").toString())){
                    if (user.get("validityDate")!=null && !user.get("validityDate").equals("") && !StringUtils.isBlank(user.get("validityDate").toString())){
                        String validityDate = user.get("validityDate").toString();
                        Calendar curr = Calendar.getInstance();
                        Calendar start = Calendar.getInstance();
                        start.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(validityDate));
                        if (curr.after(start)){
                            Map<String,Object> userValidity = new HashMap<String, Object>();
                            userValidity.put("id",userId);
                            userValidity.put("authDate", "");
                            userValidity.put("authTime", "");
                            userValidity.put("idHandleImgUrl", "");
                            userValidity.put("realName", "");
                            userValidity.put("isAuth", "F");//F:未实名 T：实名 N:正在审核中 E：审核失败
                            userValidity.put("idType", "");
                            userValidity.put("idNO", "");
                            userValidity.put("validityDate","");
                            userValidity.put("addr", "");
                            this.update(TableList.USER,userValidity);
                            user = this.findById(TableList.USER, userId);
                        }
                    }
                }
                //更新缓存中的Token,实名
                String token = BaseUtil.objToStr(user.get("token"), null);
                String isAuth = BaseUtil.objToStr(user.get("isAuth"), null);
                updateRedisTokenAndAuth(BaseUtil.objToStr(user.get("id"), null), token, isAuth);
                /** update by cwf  2019/9/24 10:08 Reason:添加储存设备号用来推送消息
                 */
                updateDeviceToken(userId,paramMap);

                //获取密钥
                String workKey = keyService.findKeyByStatus(TableList.KEY_STATUS_NORMAL);
                if(workKey != null){
                    user.put("workKey",workKey);
                }

                /**
                 * 获取用户的公告
                 */
                Map<String,Object> noticeUser = noticeUserService.findByUserId(userId);
                List<Map<String,Object>> notices = null;
                Map<String,Object> result = new HashMap<String, Object>();
                Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
                Integer expire = Integer.valueOf(paramService.findValueByName("apiAuthCheckRedisExpire"));//过期时间(分钟)
                String redisValue = null;
                if(noticeUser == null || noticeUser.isEmpty()){
                    //获取所有"normal"的公告
                    notices  = noticeService.findList(" select * ", "from "+TableList.NOTICE +" where cstatus = 'normal' order by createDate desc ");
                    if(notices != null && !notices.isEmpty()){
                        //获取最新的公告id
                        Integer maxNoticeId = (Integer) baseDao.queryForObject("select max(id) from "+TableList.NOTICE,Integer.class);
                        Map<String,Object> userNotice = new HashMap<String, Object>();
                        userNotice.put("userId",userId);
                        userNotice.put("maxNoticeId",maxNoticeId);
                        noticeUserService.save(TableList.USER_NOTICE,userNotice);
                        userNotice = noticeUserService.findByUserId(userId);
                        redisValue = JSON.toJSONString(userNotice);
                        //redis修改
                        RedisUtil.setStr(userId+"_noticeUser",redisValue , apiNewAuthCheckRedisDbIndex, expire*60);
                    }
                }else{
                    //查询是否有最新的公告
                    notices = noticeService.findList(" select * ",
                            "from "+TableList.NOTICE +" where cstatus = 'normal' and id > "+noticeUser.get("maxNoticeId")+" order by createDate desc ");
                    if(notices != null && !notices.isEmpty()) {
                        Integer maxNoticeId = (Integer) baseDao.queryForObject("select max(id) from " + TableList.NOTICE, Integer.class);
                        Map<String, Object> userNotice = new HashMap<String, Object>();
                        userNotice.put("maxNoticeId", maxNoticeId);
                        userNotice.put("id", BaseUtil.objToInteger(noticeUser.get("id"), 0));
                        noticeUserService.update(TableList.USER_NOTICE, userNotice);
                        redisValue = JSON.toJSONString(userNotice);
                        //redis修改
                        RedisUtil.setStr(userId+"_noticeUser",redisValue , apiNewAuthCheckRedisDbIndex, expire*60);
                    }
                }

                result.put("notices",notices);
                result.put("user",user);
                String  applyType="";
                String  companyName="";

                if (user.get("companyId")!=null){
                    Map<String,Object> conpany =this.findById(TableList.COMPANY,Integer.parseInt(BaseUtil.objToStr(user.get("companyId"),null)));

                    if(conpany!=null){
                    if (conpany.get("applyType")!=null){
                        applyType = conpany.get("applyType").toString();
                    }
                    if (conpany.get("companyName")!=null){
                        companyName = conpany.get("companyName").toString();
                    }
                }
                }

                user.put("applyType",applyType);
                user.put("companyName",companyName);
                //增加获取orgCode
                String orgCode = BaseUtil.objToStr(orgService.findOrgCodeByUserId(userId),"无");
                user.put("orgCode",orgCode);

                return ResultData.dataResult("success","登录成功",result);
            }else{

                //返回登录失败原因
                String handleCause = userAccount.get("handleCause").toString();
                return  Result.unDataResult("fail",handleCause);
            }
        }else {

            Long leftInputNum = passwordService.addErrInputNum(userId.toString(),Status.PWD_TYPE_SYS);
            return  Result.unDataResult("fail","密码错误:剩余" + leftInputNum + "次输入机会");
        }
    }
    //为了非好友邀约新建aop
    @Override
    public Result registerOrigin(Map<String, Object> paramMap) throws Exception {
        paramMap.remove("token");
        String code = BaseUtil.objToStr(paramMap.get("code"),"");
        String phone = BaseUtil.objToStr(paramMap.get("phone"),"");
        Map<String, Object> userByPhone = getUserByPhone(phone);
        String sysPwd = BaseUtil.objToStr(paramMap.get("sysPwd"),"");
        if ("".equals(code)||"".equals(phone)||"".equals(sysPwd)){
            return Result.unDataResult("fail","缺少参数");
        }
        if(userByPhone!=null){
            //查看tbl_account表中是否存在账户
            int userId = BaseUtil.objToInteger(userByPhone.get("id"),0);
            Map<String, Object> account = findFirstBySql("select * from " + TableList.USER_ACCOUNT + " where " + " userId=" + userId);
            //没有账户 则创建账户
            if (account==null){
                return creatAccount(userId,sysPwd);
            }
            //有账户则返回已被注册
            return Result.unDataResult("fail","手机号已经被注册");
        }
        boolean flag = codeService.verifyCode(phone,code,1);
       if(!flag){
            return Result.unDataResult("fail","验证码错误");
        }
        paramMap.remove("code");
        paramMap.remove("sysPwd");
        //添加用户信息
        int userId = createUser(phone, "");
        if(userId < 1){
            return Result.unDataResult("fail","注册失败");
        }
        //添加用户账户表
        return creatAccount(userId,sysPwd);
    }
    /**
     * 根据userId创建账户
     */
    public Result creatAccount(int userId,String sysPwd ){
        Map<String,Object> userAccount = new HashMap<>();
        userAccount.put("userId",userId);
        userAccount.put("sysPwd",sysPwd);
        userAccount.put("cstatus",TableList.USER_CSSTATUS_NORMAL);
        int userAccountId = this.save(TableList.USER_ACCOUNT,userAccount);
        //重新获取用户信息
        Map<String,Object> user = this.findById(TableList.USER,userId);
        return userAccountId > 0 ?ResultData.dataResult("success","注册成功",user) :Result.unDataResult("fail","注册失败");
    }
    /**
     * 为邀约者创建账号
     * @param
     * @return
     * @throws Exception
     */
    @Override
    public int createUser(String phone,String realName) throws Exception {
        Map<String, Object> paramMap =new HashMap<>();
        //添加用户信息
        Date date = new Date();
        paramMap.put("createDate",new SimpleDateFormat("yyyy-MM-dd").format(date));
        paramMap.put("createTime",new SimpleDateFormat("HH:mm:ss").format(date));
        paramMap.put("token",UUID.randomUUID().toString());
        paramMap.put("loginName",phone);
        paramMap.put("isAuth","F");
        paramMap.put("phone",phone);
        paramMap.put("realName",realName);
        paramMap.put("workKey", NumberUtil.getRandomWorkKey(10));
        paramMap.put("isSetTransPwd","F");
        paramMap.put("soleCode",OrderNoUtil.genOrderNo("C", 16));
       return this.save(TableList.USER, paramMap);
    }
    @Override
    public Result verify(Map<String, Object> paramMap) {
        try {
            paramMap.remove("token");
            Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), Integer.valueOf(0));

            if (isVerify(userId)) {
               logger.info("已经实名认证过");
                return Result.unDataResult("fail", "已经实名认证过");
            }

            /**
             * 验证 短信验证码
             */
            /*String phone = BaseUtil.objToStr(paramMap.get("phone"), null);
            String code = BaseUtil.objToStr(paramMap.get("code"), null);
            boolean flag = this.codeService.verifyCode(phone, code, 1).booleanValue();
            if (!flag) {
                return Result.unDataResult("fail", "验证码错误");
            }*/

            String idNO = BaseUtil.objToStr(paramMap.get("idNO"), null);
            String realName = URLDecoder.decode(BaseUtil.objToStr(paramMap.get("realName"), null), "UTF-8");
            if(idNO == null){
                return Result.unDataResult("fail", "身份证不能为空!");
            }
            String workKey = keyService.findKeyByStatus("normal").toString();
            // update by cwf  2019/10/15 10:36 Reason:暂时修改为后端加密
//            String idNoMW = DESUtil.encode(workKey,idNO);
            //原先为前端加密后端解密
            String idNoMW = DESUtil.decode(workKey, idNO);
//            String idNoMW = idNO;

            if(realName == null){
                return Result.unDataResult("fail", "真实姓名不能为空!");
            }
            String idHandleImgUrl = BaseUtil.objToStr(paramMap.get("idHandleImgUrl"), null);
            /**
             * 验证 身份证
             */
            // update by cwf  2019/10/15 10:54 Reason:改为加密后进行数据判断 原 idNO 现idNoMw
            // update by cwf  2019/11/6 13:42 Reason:改为回前端加密 原 idNoMW 现 idNO
            if(this.isExistIdNo(userId.toString(),idNO)){
                return Result.unDataResult("fail", "该身份证已实名，无法再次进行实名认证！");
            }

            try{
                //实人认证  update by cwf  2019/11/25 11:30 Reason:先查询本地库是否有实名认证 如果没有 则调用CTID认证
                String sql="select distinct * from "+TableList.USER_AUTH +" where idNo='"+idNO+"' and realName='"+realName+"'";
                Map<String, Object> userAuth = findFirstBySql(sql);
                if (userAuth!=null){
                    idHandleImgUrl=BaseUtil.objToStr(userAuth.get("idHandleImgUrl"),idHandleImgUrl);
                    logger.info("本地实人认证成功上一张成功图片为：{}",idHandleImgUrl);
                } else {
                   String photoResult = phoneResult(idNoMW, realName, idHandleImgUrl);
                   if (!"success".equals(photoResult)){
                       return Result.unDataResult("fail", photoResult);
                   }
               }
            }catch (Exception e){
                e.printStackTrace();
                return Result.unDataResult("fail", "图片上传出错!");
            }

            String address = BaseUtil.objToStr(paramMap.get("address"), null);
            //非空判断
            if(idHandleImgUrl == null){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//回滚
                return Result.unDataResult("fail", "图片上传失败，请稍后再试!");
            }
            idHandleImgUrl = URLDecoder.decode(idHandleImgUrl, "UTF-8");
            //暂时注释
//            String idType = URLDecoder.decode(BaseUtil.objToStr(paramMap.get("idType"), null), "UTF-8");

            Date date = new Date();
            String authDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
            String authTime = new SimpleDateFormat("HH:mm:ss").format(date);
            Map verifyMap = new HashMap();
            verifyMap.put("authDate", authDate);
            verifyMap.put("authTime", authTime);
            verifyMap.put("id", BigInteger.valueOf(userId.intValue()));
            verifyMap.put("idHandleImgUrl", idHandleImgUrl);
            verifyMap.put("realName", realName);
            String isAuth = "T";
            verifyMap.put("isAuth", isAuth);//F:未实名 T：实名 N:正在审核中 E：审核失败
            // update by cwf  2019/10/15 10:54 Reason: 暂时改为01
            verifyMap.put("idType", "01");
            verifyMap.put("idNO", idNO);
            String verifyTermOfValidity = paramService.findValueByName("verifyTermOfValidity");
            Calendar c = Calendar.getInstance();
            c.add(Calendar.YEAR, Integer.parseInt(verifyTermOfValidity));
            String validityDate = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
            verifyMap.put("validityDate",validityDate);
            if( address != null){
                verifyMap.put("addr", address);
            }
            if(update("tbl_user", verifyMap) > 0){
                Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
                String key = userId + "_isAuth";
                //redis修改
                RedisUtil.setStr(key, "T", apiNewAuthCheckRedisDbIndex, null);
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("isAuth", isAuth);
                Map<String, Object> userMap = this.findById(TableList.USER, userId);
                // update by cwf  2019/11/25 16:33 Reason:如果本地实名不存在则插入本地
                int authSave = deleteOrUpdate("insert into " + TableList.USER_AUTH + "(userId,idNO,realName,idHandleImgUrl,authDate) " +
                        "values('"+userId+"','"+idNO+"','"+realName+"','"+idHandleImgUrl+"',SYSDATE())");
                logger.info("插入本地实人："+authSave);
                resultMap.put("isSetTransPwd", BaseUtil.objToStr(userMap.get("isSetTransPwd"),"F"));
                resultMap.put("validityDate",validityDate);
                return ResultData.dataResult("success", "实名认证成功", resultMap);
            }
            return Result.unDataResult("fail", "实名认证失败");
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//回滚
            return Result.unDataResult("fail", "异常，请稍后再试");
        }
    }

    public String phoneResult(String idNO,String realName,String idHandleImgUrl) throws Exception{
        String merchOrderId = OrderNoUtil.genOrderNo("V", 16);//商户请求订单号
        String merchantNo="100000000000006";//商户号
        String productCode="0003";//请求的产品编码
        String key="2B207D1341706A7R4160724854065152";//秘钥
        String dateTime=DateUtil.getSystemTimeFourteen();//时间戳
        String certNo = DESUtil.encode(key,idNO);
        logger.info("名称加密前为：{}",realName);
        String userName =DESUtil.encode(key,realName);
        logger.info("名称加密后为：{}",userName);
        String imageServerUrl = paramService.findValueByName("imageServerUrl");
        String photo=Base64.encode(FilesUtils.getImageFromNetByUrl(imageServerUrl+idHandleImgUrl));
        String signSource = merchantNo + merchOrderId + dateTime + productCode + key;//原始签名值
        String sign = MD5Util.MD5Encode(signSource);//签名值


        Map<String, String> map = new HashMap<String, String>();
        map.put("merchOrderId", merchOrderId);
        logger.info(merchOrderId);
        map.put("merchantNo", merchantNo);
        map.put("productCode", productCode);
        map.put("userName", userName);//加密
        map.put("certNo", certNo);// 加密);
        map.put("dateTime", dateTime);
        map.put("photo", photo);//加密
        map.put("sign", sign);
        String userIdentityUrl = paramService.findValueByName("userIdentityUrl");
        ThirdResponseObj obj	=	HttpUtil.http2Nvp(userIdentityUrl,map,"UTF-8");
        String makePlanJsonResult = obj.getResponseEntity();
        JSONObject jsonObject = JSONObject.parseObject(makePlanJsonResult);
        Map resultMap = JSON.parseObject(jsonObject.toString());
        logger.info(jsonObject.toString());
        if ("1".equals(resultMap.get("bankResult").toString())){
            return "success";
        }else{
            return resultMap.get("message").toString();
        }
    }



    @Override
    public Map<String, Object> getUserByUserToken(Integer userId, String token) {
        String sql =" select * from "+TableList.USER +" where token = '"+token+"' and id = "+userId;
        return this.findFirstBySql(sql);
    }

    @Override
    public Result updateSysPwd(Map<String, Object> paramMap) {
        paramMap.remove("token");

        String newPassword = BaseUtil.objToStr(paramMap.get("newPassword"),null);
        String oldPassword = BaseUtil.objToStr(paramMap.get("oldPassword"),null);
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),0);
        Map<String,Object> userAccount = userAccountService.findUserAccountByUser(userId);
        userAccount.remove("payPwd");
        userAccount.remove("cstatus");
        userAccount.remove("userId");
        String dbPassword = BaseUtil.objToStr(userAccount.get("sysPwd"),null);
        if(dbPassword.equals(oldPassword)){
            userAccount.put("sysPwd",newPassword);
            return this.update(TableList.USER_ACCOUNT,userAccount) > 0 ?Result.unDataResult("success","修改系统密码成功"):Result.unDataResult("fail","修改系统密码失败");
        }
        return Result.unDataResult("fail","旧密码错误");
    }

    @Override
    public Result updateGesturePwd(Map<String, Object> paramMap) {
        paramMap.remove("token");
        String newPassword = BaseUtil.objToStr(paramMap.get("newPassword"),null);
        String oldPassword = BaseUtil.objToStr(paramMap.get("oldPassword"),null);
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),0);
        Map<String,Object> userAccount = userAccountService.findUserAccountByUser(userId);
        String dbPassword = BaseUtil.objToStr(userAccount.get("gesturePwd"),null);
        userAccount.remove("payPwd");
        userAccount.remove("cstatus");
        userAccount.remove("userId");
        if(dbPassword.equals(oldPassword)){
            userAccount.put("gesturePwd",newPassword);
            return this.update(TableList.USER_ACCOUNT,userAccount) > 0 ?Result.unDataResult("success","修改手势密码成功"):Result.unDataResult("fail","修改手势密码失败");
        }
        return Result.unDataResult("fail","旧手势密码错误");
    }

    @Override
    public Result updatePhone(Map<String, Object> paramMap) {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),null);
        String  code = BaseUtil.objToStr(paramMap.get("code"),null); //验证码
        String  phone = BaseUtil.objToStr(paramMap.get("phone"),null); //银行预留手机号
        boolean flag = codeService.verifyCode(phone,code,1);//3魔板
       if(!flag){
            return Result.unDataResult("fail","验证码错误");
        }
        Map<String,Object> user = this.getUserByPhone(phone);
        if(user != null){
            return Result.unDataResult("fail","手机号已被注册");
        }
        Map<String,Object> upUser = this.getUserByUserId(userId);
        upUser.put("phone",phone);
        upUser.put("loginName",phone);
        this.update(TableList.USER,upUser);
        Map<String,Object> userAccount = userAccountService.findUserAccountByUser(userId);
        userAccount.remove("payPwd");
        userAccount.remove("cstatus");
        userAccount.remove("userId");
        return this.update(TableList.USER_ACCOUNT,userAccount) > 0 ?Result.unDataResult("success","找回系统密码成功"):Result.unDataResult("fail","找回系统密码失败");
    }

    @Override
    public Result forgetSysPwd(Map<String, Object> paramMap) {
        paramMap.remove("token");
        String  code = BaseUtil.objToStr(paramMap.get("code"),null); //验证码
        String  phone = BaseUtil.objToStr(paramMap.get("phone"),null); //银行预留手机号
        boolean flag = codeService.verifyCode(phone,code,1);
       if(!flag){
            return Result.unDataResult("fail","验证码错误");
        }
        String  newPassword = BaseUtil.objToStr(paramMap.get("newPassword"),null);
        Map<String,Object> user = this.getUserByPhone(phone);
        if(user == null){
            return Result.unDataResult("fail","手机号还未注册");
        }
        Integer userId = BaseUtil.objToInteger(user.get("id"),0);
        Map<String,Object> userAccount = userAccountService.findUserAccountByUser(userId);
        userAccount.remove("payPwd");
        userAccount.remove("cstatus");
        userAccount.remove("userId");
        userAccount.put("sysPwd",newPassword);
        return this.update(TableList.USER_ACCOUNT,userAccount) > 0 ?Result.unDataResult("success","找回系统密码成功"):Result.unDataResult("fail","找回系统密码失败");
    }

    @Override
    public Result setGesturePwd(Map<String, Object> paramMap) {
        paramMap.remove("token");
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),0);
        Map<String,Object> userAccount = userAccountService.findUserAccountByUser(userId);
        userAccount.remove("sysPwd");
        userAccount.remove("cstatus");
        userAccount.remove("userId");
        userAccount.remove("payPwd");
        String gesturePwd = BaseUtil.objToStr(paramMap.get("gesturePwd"),null);
        userAccount.put("gesturePwd",gesturePwd);
        return this.update(TableList.USER_ACCOUNT,userAccount) > 0 ?Result.unDataResult("success","设定手势密码成功"):Result.unDataResult("fail","设定手势密码失败");
    }

    @Override
    public Result updateNick(Map<String, Object> paramMap) {
        try{
            paramMap.remove("token");
            Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),0);
            String  niceName = BaseUtil.objToStr(paramMap.get("niceName"),null);
            Map<String,Object> userUpdate = new HashMap<String, Object>();
            if(StringUtils.isNotBlank(niceName)){
                niceName = URLDecoder.decode(niceName,"UTF-8"); //昵称
                userUpdate.put("niceName",niceName);
            }

            String  headImgUrl = BaseUtil.objToStr(paramMap.get("headImgUrl"),null);
            if(StringUtils.isNotBlank(headImgUrl)){
                headImgUrl = URLDecoder.decode(headImgUrl,"UTF-8"); //头像路径
                userUpdate.put("headImgUrl",headImgUrl);
            }
            userUpdate.put("id",userId);
            return this.update(TableList.USER,userUpdate) > 0 ?Result.unDataResult("success","保存成功"):Result.unDataResult("fail","保存失败");
        }catch (Exception e){
            return Result.unDataResult("fail","异常，请稍后再试");
        }
    }

    @Override
    public String getWorkKeyByUserId(Integer userId) {
        Map<String,Object> user = this.findById(TableList.USER,userId);
        if(user != null){
            return BaseUtil.objToStr(user.get("workKey"),null);
        }
        return null;
    }

    @Override
    public boolean isExistIdNo(String idNo) throws Exception {
        String sql = "select * from " + TableList.USER + " where idNo = '"+idNo+"'";
        Map<String, Object> map = this.findFirstBySql(sql);
        return map != null;
    }

    @Override
    public boolean isExistIdNo(String userId, String idNo) throws Exception {
        String sql = "select * from " + TableList.USER + " where idNo = '"+idNo+"' and id ="+userId;
        Map<String, Object> map = this.findFirstBySql(sql);
        return map != null;
    }

    @Override
    public void updateRedisTokenAndAuth(String userId, String token, String isAuth) throws Exception {
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(token) || StringUtils.isBlank(isAuth)){
            return;
        }
        Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
        Integer expire = Integer.valueOf(paramService.findValueByName("apiAuthCheckRedisExpire"));//过期时间(分钟)
        //redis修改
        RedisUtil.setStr(userId+"_token", token, apiNewAuthCheckRedisDbIndex, expire*60);
        //redis修改
        RedisUtil.setStr(userId+"_isAuth", isAuth, apiNewAuthCheckRedisDbIndex, expire*60);
    }

    @Override
    public boolean isRepeatVerify(String userId, String cardNo) throws Exception {
        String key = "verify_"+userId+"_"+cardNo;
        if(RedisUtil.getStrVal(key,14) == null){
            return false;
        }
        return true;
    }

    @Override
    public Integer updateUserSetTransPwdStatus(String userId, String status) throws Exception {
        String sql = "update " + TableList.USER +" set isSetTransPwd = '"+status+"' where id = "+userId;
        return baseDao.deleteOrUpdate(sql);
    }

    @Override
    public Result loginByVerifyCode(Map<String, Object> paramMap) throws Exception {
        /**
         * 1,获取参数并判断
         */
        String phone = BaseUtil.objToStr(paramMap.get("phone"), null);//登录账号
        String code = BaseUtil.objToStr(paramMap.get("code"), null);//短信验证码
        if(StringUtils.isBlank(phone)){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少登录账号!");
        }
        if(StringUtils.isBlank(code)){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少验证码!");
        }
        Map<String,Object>  user = this.getUserByPhone(phone);
        if(user == null){
            return  Result.unDataResult(ConsantCode.FAIL,"用户不存在");
        }
        Integer userId = Integer.parseInt(user.get("id")+"");
        Map<String,Object> userAccount = userAccountService.findUserAccountByUser(userId);
        if(userAccount == null){
            return  Result.unDataResult(ConsantCode.FAIL,"未查询到相关账户信息");
        }
        String cstatus = userAccount.get("cstatus")+"";
        if("normal".equals(cstatus)){
            /**
             * 2,验证短信验证码
             */
            if(codeService.verifyCode(phone, code, 1)){
                //短信验证码正确
                passwordService.resetPwdInputNum(userId.toString(), Status.PWD_TYPE_SYS);
                Map<String,Object> userUpdate = new HashMap<String, Object>();
                userUpdate.put("id",userId);
                userUpdate.put("token", UUID.randomUUID().toString());
                this.update(TableList.USER,userUpdate);

                user = this.findById(TableList.USER, userId);
                //实名有效日期过了
                if ("T".equals(user.get("isAuth").toString())){
                    if (user.get("validityDate")!=null && !user.get("validityDate").equals("") && !StringUtils.isBlank(user.get("validityDate").toString())){
                        String validityDate = user.get("validityDate").toString();
                        Calendar curr = Calendar.getInstance();
                        Calendar start = Calendar.getInstance();
                        start.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(validityDate));
                        if (curr.after(start)){
                            Map<String,Object> userValidity = new HashMap<String, Object>();
                            userValidity.put("id",userId);
                            userValidity.put("authDate", "");
                            userValidity.put("authTime", "");
                            userValidity.put("idHandleImgUrl", "");
                            userValidity.put("realName", "");
                            userValidity.put("isAuth", "F");//F:未实名 T：实名 N:正在审核中 E：审核失败
                            userValidity.put("idType", "");
                            userValidity.put("idNO", "");
                            userValidity.put("validityDate","");
                            userValidity.put("addr", "");
                            this.update(TableList.USER,userValidity);
                            user = this.findById(TableList.USER, userId);
                        }
                    }
                }
                //更新缓存中的Token,实名
                String token = BaseUtil.objToStr(user.get("token"), null);
                logger.info("登入人为:{}，token：{}",token,userId);
                String isAuth = BaseUtil.objToStr(user.get("isAuth"), null);
                updateRedisTokenAndAuth(BaseUtil.objToStr(user.get("id"), null), token, isAuth);
                /** update by cwf  2019/9/24 10:08 Reason:添加储存设备号用来推送消息
                 */
                updateDeviceToken(userId,paramMap);
                //获取密钥
                String workKey = keyService.findKeyByStatus(TableList.KEY_STATUS_NORMAL);
                if(workKey != null){
                    user.put("workKey",workKey);
                }
                /**
                 * 获取用户的公告
                 */
                Map<String,Object> noticeUser = noticeUserService.findByUserId(userId);
                List<Map<String,Object>> notices = null;
                Map<String,Object> result = new HashMap<String, Object>();
                Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
                Integer expire = Integer.valueOf(paramService.findValueByName("apiAuthCheckRedisExpire"));//过期时间(分钟)
                String redisValue = null;
                if(noticeUser == null || noticeUser.isEmpty()){
                    //获取所有"normal"的公告
                    notices  = noticeService.findList(" select * ", "from "+TableList.NOTICE +" where cstatus = 'normal' order by createDate desc ");
                    if(notices != null && !notices.isEmpty()){
                        //获取最新的公告id
                        Integer maxNoticeId = (Integer) baseDao.queryForObject("select max(id) from "+TableList.NOTICE,Integer.class);
                        Map<String,Object> userNotice = new HashMap<String, Object>();
                        userNotice.put("userId",userId);
                        userNotice.put("maxNoticeId",maxNoticeId);
                        noticeUserService.save(TableList.USER_NOTICE,userNotice);
                        userNotice = noticeUserService.findByUserId(userId);
                        redisValue = JSON.toJSONString(userNotice);
                        //redis修改
                        RedisUtil.setStr(userId+"_noticeUser",redisValue , apiNewAuthCheckRedisDbIndex, expire*60);
                    }
                }else{
                    //查询是否有最新的公告
                    notices = noticeService.findList(" select * ",
                            "from "+TableList.NOTICE +" where cstatus = 'normal' and id > "+noticeUser.get("maxNoticeId")+" order by createDate desc ");
                    if(notices != null && !notices.isEmpty()) {
                        Integer maxNoticeId = (Integer) baseDao.queryForObject("select max(id) from " + TableList.NOTICE, Integer.class);
                        Map<String, Object> userNotice = new HashMap<String, Object>();
                        userNotice.put("maxNoticeId", maxNoticeId);
                        userNotice.put("id", BaseUtil.objToInteger(noticeUser.get("id"), 0));
                        noticeUserService.update(TableList.USER_NOTICE, userNotice);
                        redisValue = JSON.toJSONString(userNotice);
                        //redis修改
                        RedisUtil.setStr(userId+"_noticeUser",redisValue , apiNewAuthCheckRedisDbIndex, expire*60);
                    }
                }
                result.put("notices",notices);
                result.put("user",user);
                String  applyType="";
                String  companyName="";
                if (user.get("companyId")!=null){
                    Map<String,Object> company =this.findById(TableList.COMPANY,Integer.parseInt(user.get("companyId").toString()));
                    if (company!=null){
                        applyType = BaseUtil.objToStr(company.get("applyType"),"");
                        companyName = BaseUtil.objToStr(company.get("companyName"),"");
                    }
                }
                user.put("companyName",companyName);
                user.put("applyType",applyType);
                //增加获取orgCode
                String orgCode = BaseUtil.objToStr(orgService.findOrgCodeByUserId(userId),"无");
                user.put("orgCode", orgCode);
                return ResultData.dataResult(ConsantCode.SUCCESS,"登录成功",result);
            }else{
                //验证码输入错误
                return  Result.unDataResult(ConsantCode.FAIL,"验证码输入错误，请重新获取!");
            }
        }else{
            //返回账户冻结原因
            String handleCause = userAccount.get("handleCause").toString();
            return  Result.unDataResult(ConsantCode.FAIL, handleCause);
        }
    }

    @Override
    public List<Map<String, Object>> findByUser(String realName,Integer companyId) throws Exception {
        String columnSql =" select cu.*,u.phone,u.id visitorId ";
        String fromSql = " from "+ TableList.COMPANY_USER +" cu" +
                " left join " + TableList.USER + " u on cu.userId=u.id" +
                " where cu.userName = '"+realName+"' and cu.companyId ='"+companyId+"' and cu.status='applySuc'";
        return baseDao.findList(columnSql,fromSql);
    }

    @Override
    public Result findCompanyId(Map<String, Object> paramMap, Integer pageNum, Integer pageSize) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),null);
        Map<String, Object> user = getUserByUserId(userId);
        if (user==null || !"manage".equals(user.get("role")) || user.get("companyId")==null){
            return Result.unDataResult("fail","获取失败，你没有权限!");
        }
        String columnSql =" select u.* ";
        String fromSql = "  from "+  TableList.COMPANY_USER + " cu" +
                " left join " + TableList.USER + " u on cu.userId=u.id" +
                " where cu.companyId = '"+user.get("companyId")+"'";
        PageModel pageModel = this.findPage(columnSql,fromSql,pageNum,pageSize);
        return pageModel != null
                ? ResultData.dataResult("success","获取成功",pageModel)
                : ResultData.dataResult("success","暂无数据",new PageModel(pageNum,pageSize));
    }

    @Override
    public Result addUser(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"), null);
        Integer companyId = BaseUtil.objToInteger(paramMap.get("companyId"),null);
        String role = BaseUtil.objToStr(paramMap.get("role"),null);
        String realName = BaseUtil.objToStr(paramMap.get("realName"),null);
        String phone = BaseUtil.objToStr(paramMap.get("phone"),null);

        if(userId==null||companyId==null||StringUtils.isBlank(role)||StringUtils.isBlank(realName)||StringUtils.isBlank(phone)){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少用户信息!");
        }
        Map<String, Object> loginName = getUserByPhone(phone);
        if (loginName!=null && loginName.get("companyId")!=null){
            return ResultData.unDataResult("fail", "该手机号已经被其他公司注册过!");
        }else if (loginName!=null && loginName.get("companyId")==companyId){
            return ResultData.unDataResult("fail", "已经添加该员工!");
        }else if (loginName!=null && loginName.get("companyId")==null){
            //修改
            Map<String, Object> update = new HashMap<String, Object>();
            update.put("id",loginName.get("id"));
            update.put("companyId",companyId);
            update.put("role",role);
            Integer updateResult =update(TableList.USER, update);
            if(updateResult > 0){
                return  Result.unDataResult("success","修改成功");
            }else{
                return Result.unDataResult("fail","修改失败");
            }
        }
        //添加
        Map<String,Object> save = new HashMap<String, Object>();
        Map<String,Object> user = findById(TableList.USER, userId);
        Map<String,Object> org = findById(TableList.ORG, Integer.parseInt(user.get("orgId").toString()));
        String relationNo = org.get("relation_no").toString();
        save.put("orgId",user.get("orgId").toString());
        save.put("relationNo",relationNo);
        save.put("realName",realName);
        Date date = new Date();
        save.put("createDate",new SimpleDateFormat("yyyy-MM-dd").format(date));
        save.put("createTime",new SimpleDateFormat("HH:mm:ss").format(date));
        save.put("token",UUID.randomUUID().toString());
        save.put("loginName",phone);
        save.put("phone",phone);
        save.put("isAuth","F");
        save.put("workKey",NumberUtil.getRandomWorkKey(10));
        save.put("isSetTransPwd","F");
        save.put("companyId",companyId);
        save.put("role",role);
        save.put("soleCode",OrderNoUtil.genOrderNo("C" , 16));
        Integer saveResult = this.save(TableList.USER,save);
        if (saveResult > 0){
            Map<String,Object> saveUserAccount = new HashMap<String, Object>();
            saveUserAccount.put("userId",saveResult);
            saveUserAccount.put("sysPwd",MD5Util.MD5("000000"));
            saveUserAccount.put("cstatus","normal");
            this.save(TableList.USER_ACCOUNT,saveUserAccount);
            return Result.unDataResult("success","添加成功");
        }
        return Result.unDataResult("fail","添加失败");
    }

    @Override
    public Result deleteUser(Map<String, Object> paramMap) throws Exception {
        Integer id = BaseUtil.objToInteger(paramMap.get("id"),null);
        if(id==null){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少参数!");
        }
        String sql = "update " +TableList.USER +
        " set orgId = null,relationNo='',companyId=null,role=''" +
                " where id = "+id;
        Integer delete = baseDao.deleteOrUpdate(sql);
        String sql2 = "update"+TableList.COMPANY_USER +"set currentStatus = 'deleted'  where userId = "+id;
        Integer delete2 = baseDao.deleteOrUpdate(sql2);
        if(delete > 0&&delete2>0){
            return  Result.unDataResult("success","删除成功");
        }else{
            return Result.unDataResult("fail","删除失败");
        }
    }

    @Override
    public Result findVisitorId(Map<String, Object> paramMap) throws Exception {
        Integer companyId = BaseUtil.objToInteger(paramMap.get("companyId"),null);
        String realName = BaseUtil.objToStr(paramMap.get("realName"),null);
        if(companyId==null||StringUtils.isBlank(realName)){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少参数!");
        }
        List<Map<String, Object>> list = findByUser(realName,companyId);
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","查询公司员工成功",list)
                : Result.unDataResult("success","查无此人");
    }

    @Override
    public Result updateCompanyIdAndRole(Map<String, Object> paramMap) throws Exception {
        Integer userId = BaseUtil.objToInteger(paramMap.get("userId"),null);
        Integer companyId = BaseUtil.objToInteger(paramMap.get("companyId"),null);
        String role = BaseUtil.objToStr(paramMap.get("role"),null);
        if (userId==null || companyId==null || StringUtils.isBlank(role)){
            return  Result.unDataResult(ConsantCode.FAIL,"缺少参数!");
        }
        Map<String,Object> conpany =this.findById(TableList.COMPANY, companyId);
        //* update by cwf  2019/9/26 14:16 Reason:空值判断
        if (conpany==null) {
            return Result.unDataResult("fail","修改失败,该公司不存在！");
        }
        Map<String, Object> update = new HashMap<String, Object>();
        update.put("id",userId);
        update.put("companyId",companyId);
        update.put("role",role);
        Integer updateResult =update(TableList.USER, update);
        if(updateResult > 0){
            String applyType = "";
            String companyName = "";
            Map<String,Object> map = new HashMap<String, Object>();
                if (conpany.get("applyType") != null) {
                    applyType = conpany.get("applyType").toString();
                }
                if (conpany.get("companyName") != null) {
                    companyName = conpany.get("companyName").toString();
                }

            map.put("applyType",applyType);
            map.put("companyName",companyName);
            map.put("role",role);
            map.put("companyId",companyId);
            return  ResultData.dataResult("success","修改成功",map);
        }else{
            return Result.unDataResult("fail","修改失败");
        }
    }
    @Override
    public Map<String, Object> FindFriendByPhoneAndRealName(String phone, String realName) throws Exception {
        String coloumSql = " select id,orgId,companyId"+" from "+ TableList.USER  +
                " where phone = '"+phone+"' and realName='"+realName+"' ";
        return baseDao.findFirstBySql(coloumSql);

    }

    @Override
    public Long checkPhone(String phone) throws Exception {
        String fromSql = "from "+ TableList.USER+" where  phone='"+phone+"'";
        return baseDao.findExist(fromSql);
    }

     /**
     *  update by cwf  2019/10/8 9:15 Reason:增加查询是否为好友
     *  update by cwf  2019/11/26 10:19 Reason: 增加实名与手机号查询，查询好友真实姓名如果相同则返回姓名。不同则返回脱敏姓名
      *
     */
    @Override
    public Result findIsUserByPhone(Map<String, Object> paramMap)  throws Exception{
        long startTime=System.currentTimeMillis();
        String phoneStr = BaseUtil.objToStr(paramMap.get("phoneStr"),",");
        String userId=BaseUtil.objToStr(paramMap.get("userId"),"0");
//        System.out.println(phoneStr);
        String[] phones = phoneStr.split(",");
//        System.out.println("phones.length"+phones.length);
//        System.out.println("phones[0]"+phones[0]);
        StringBuffer newPhones=new StringBuffer();
        for (String phone:phones){
            if( phoneUtil.isPhoneLegal(phone)){
                newPhones.append(phone).append(",");
            }

        }
//        System.out.println("newPhones"+newPhones);
//        System.out.println("newPhones.length()"+newPhones.length());

//        if(phoneStr.endsWith(",")){
//            phoneStr= phoneStr.substring(0,phoneStr.length() -1);
//        }
        if (newPhones.length()==0){
            return Result.unDataResult("success","暂无数据");
        }
        newPhones.deleteCharAt(newPhones.length() - 1);
        logger.info("最终查询的手机号为："+newPhones);

//        String columsql="select u.*,uf.applyType,uf.remark";
//        String sql = " from "+ TableList.USER +"  u left join "+ TableList.USER_FRIEND +" uf on uf.friendId=u.id " +
//                "where phone in ("+newPhones+") and uf.userId ="+userId;
        // update by cwf  2019/11/8 15:44 Reason:查询是否存在用户，并显示是否为好友
        String columsql="select *,(select  applyType from "+ TableList.USER_FRIEND +" uf where uf.friendId=u.id and uf.userId="+userId+" ) applyType," +
                "(select  remark from "+ TableList.USER_FRIEND +" uf where uf.friendId=u.id and uf.userId="+userId+" ) remark";
        String sql = " from "+ TableList.USER +"  u where phone in ("+newPhones+") and isAuth='T'";
        logger.info(columsql+sql);
        List <Map<String, Object>> list=findList(columsql,sql);
        long endTime=System.currentTimeMillis();
        logger.info("本次查询时间为："+(endTime-startTime));
        return list != null && !list.isEmpty()
                ? ResultData.dataResult("success","查询用户成功",list)
                : Result.unDataResult("success","暂无数据");
    }
    //退出app
    @Override
    public Result appQuit(Map<String, Object> paramMap) {
        int userId=BaseUtil.objToInteger(paramMap.get("userId"),0);
       String sql= "update "+TableList.USER+" set isOnlineApp='F' where " +
               "id="+userId;
        int update = baseDao.deleteOrUpdate(sql);
        return  update>0?Result.success():Result.fail();
    }



    /**
     * 如果有deviceToken则保存，如果没有则不变
     * @param userId
     * @param paramMap
     * @return
     */
    public Result updateDeviceToken(Integer userId,Map<String, Object> paramMap){
        String deviceToken = BaseUtil.objToStr(paramMap.get("deviceToken"), "");
        String deviceType = BaseUtil.objToStr(paramMap.get("deviceType"), "");
        System.out.println("设备号"+deviceToken);
        Map<String, Object> save=new HashMap<>();
        save.put("id",userId);
        if (deviceToken!=null&&!"".equals(deviceToken)){
            save.put("deviceToken",deviceToken);
            if (deviceType!=""){
                save.put("deviceType",deviceType);
            }
        }
        save.put("isOnlineApp","T");
        int update = baseDao.update(TableList.USER, save);
        if (update > 0) {
            logger.info("存储app登入信息成功: {},{}", deviceToken,deviceType);
        }else {

            logger.info("存储app登入信息失败");
        }
        return null;
    }

    /**
     * 修改小松员工的实名状态
     * @param paramMap
     * @return
     */
    @Override
    public Result modify(Map<String, Object> paramMap) {
        Long phone = BaseUtil.objToLong(paramMap.get("phone"), null);
        String realName = BaseUtil.objToStr(paramMap.get("realName"), null);
        String isAuth = BaseUtil.objToStr(paramMap.get("isAuth"), null);
        if (phone==null||realName==null){
            return Result.unDataResult("fail","空phone或realName");
        }
        Map<String, Object> user = findFirstBySql("select * from " + TableList.USER + " where phone=" + phone+" and realName='"+realName+"'");
        if (user==null||"刘春雨".equals(user.get("realName").toString())
                ||"宋炜".equals(user.get("realName").toString())
                ||"徐素芬".equals(user.get("realName").toString())){
            return Result.unDataResult("fail","不是小松员工,无法操作！");
        }
        Object id = user.get("id");
        //g区一号楼小松安信
        Map<String, Object> isCompany = findFirstBySql("select cu.* FROM " + TableList.COMPANY_USER + " cu left JOIN  " + TableList.COMPANY + " c  on " +
                "cu.`companyId` =c.id WHERE c.`id` =18  and currentStatus='normal' and userId=" + id);
        if (isCompany==null||isCompany.isEmpty()){
            return Result.unDataResult("fail","不是小松员工,无法操作！");
        }
        Map<String, Object> newParamMap=new HashMap<>();
        newParamMap.put("id",id);
        newParamMap.put("isAuth",isAuth);
        newParamMap.put("idNo","test");
        int update = update(TableList.USER, newParamMap);
        String key = id + "_isAuth";
        Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
        //redis修改
        String s = RedisUtil.setStr(key, isAuth, apiNewAuthCheckRedisDbIndex, null);
       logger.info(update+" s: "+s);

        return Result.unDataResult("数据库更新成功状态："+update,"redis修改状态："+s);
    }
}
