package com.goldccm.model.compose;

/**
 * 所有状态放在这，方便维护
 * Created by LZ on 2017/6/25.
 */
public class Status {
    /**已使用*/
    public static final String RECOMMEND_QRCODE_USE = "use";
    /**未使用*/
    public static final String RECOMMEND_QRCODE_UNUSE= "unUse";

    /**推荐用户*/
    public static final String USER_TYPE_RECOMMEND= "recommend";
    /**代理商发展的用户*/
    public static final String USER_TYPE_NORMAL = "normal";

    /**未知状态*/
    public static final String UNKNOW = "unknow";

    /**交易类型：提现*/
    public static final String TRANSCODE_WITHDRAW = "4";

    /**登录方式*/
    public static final String LOGIN_STYLE_PWD = "1";//密码登录

    public static final String LOGIN_STYLE_GESTURE = "2";//手势登录

    /**密码类型**/
    public static final String PWD_TYPE_SYS = "sysPwd";//系统密码类型

    public static final String PWD_TYPE_PAY = "payPwd";//支付密码类型

    /**还款渠道*/
    public static final String PAY_CHANNEL_KFT = "kft";//快付通

    public static final String PAY_CHANNEL_ZM = "zm";//中茂

    /**手续费类型*/
    public static final String RATE_TYPE_COUNT = "count";//按笔收取

    public static final String RATE_TYPE_PERCENT= "percent";//按百分比收取

    public static final String MSG_SYSPWD_LIMIT = "7";

    public static final String MSG_PAYPWD_LIMIT = "8";

    public static final String SUCCESS = "success";

    public static final String FAIL = "fail";

    public static final String CODE_SUCCESS = "0000";

    public static final String APPLY_STATUS_NORMAL = "normal";

    public static final String APPLY_STATUS_DISABLE = "disable";
}
