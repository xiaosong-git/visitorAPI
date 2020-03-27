package com.goldccm.model.compose;

/**
 * 把所有的表列在这边，方便维护
 * @Date  2016/7/25 17:13
 * @Author linyb
 */
public class TableList {
    /**在库*/
    public static final String QRCODE_CSSTATUS_STOCK = "stock";
    /**生成图片*/
    public static final String QRCODE_CSSTATUS_IMAGE = "image";
    /**下发*/
    public static final String QRCODE_CSSTATUS_DOWNLOAD = "download";
    /**已注册*/
    public static final String QRCODE_CSSTATUS_REGISTER = "register";
    /**作废*/
    public static final String QRCODE_CSSTATUS_INVALID = "invalid";


    /**正常*/
    public static final String USER_CSSTATUS_NORMAL = "normal";
    /**冻结*/
    public static final String USER_CSSTATUS_FREEZE = "freeze";
    /**删除*/
    public static final String USER_CSSTATUS_DEL = "del";

    /**正常*/
    public static final String CARD_CSTATUS_NORMAL = "normal";
    /**冻结*/
    public static final String CARD_CSTATUS_DISABLE = "disable";

    //表示 tbl_key中的密钥的正常状态
    public static  final  String KEY_STATUS_NORMAL = "normal";

    /**账户类型**/
    public static final String ACCOUNT_TYPE_BALANCE = "balance";//余额账户
    public static final String ACCOUNT_TYPE_FREEZE = "freeze";//冻结余额账户

    // 用户表
    public static final String USER = "tbl_user";
    // 用户账户表
    public static final String USER_ACCOUNT = "tbl_user_account";
    // 二维码
    public static final String QRCODE = "tbl_qrcode";
    //手机验证码表
    public static final String CODE = " tbl_code ";
    // 业务参数
    public static final String PARAM = " tbl_params ";
    // 系统参数
    public static final String SYS_PARAM = " t_system_param ";
    // 公告
    public static final String NOTICE = " tbl_notice ";
    // 用户已推送的最大公告
    public static final String USER_NOTICE = " tbl_user_notice ";
    // 首页广告轮播图
    public static final String AD_BANNER = " tbl_ad_banner ";
    //字典表
    public static final String DICT_ITEM  = "  t_dict_item ";
    //密钥表
    public static final String KEY = " tbl_key";
    //机构表
    public static final String ORG = " t_org";
    //新闻表
    public static final String NEWS = " tbl_news";
    //通讯录数据
    public static final String USER_FRIEND = " tbl_user_friend";
    //访问记录表
    public static final String VISITOR_RECORD = "tbl_visitor_record";
    //公司名称
    public static final String COMPANY = "tbl_company";
    //上位机
    public static final String POSP = "tbl_posp";
    //上位机
    public static final String VISITOR_RECORD_MAXID = "tbl_visitor_record_maxid";
    //访客记录进出明细
    public static final String VISITOR_ACCESS_RECORD = "t_visit_access_record";
    //公司员工
    public static final String COMPANY_USER = "tbl_company_user";
    //公司部门
    public static final String COMPANY_SECTION = "tbl_company_section";
    //用户离线消息
    public static final String USER_MESSAGE = "tbl_user_message";
    //vip用户表
    public static final String ORG_VIP_USER= "tbl_org_vip_user";
    //预定记录
    public static final String ROOM_APPLY_RECORD= "tbl_room_apply_record";
    //共享会议室
    public static final String SHARE_ROOM= "tbl_share_room";
    //进出
    public static final String IN_OUT= "tbl_in_out";
    //错误日志
    public static final String ERROR_LOG= "t_error_log";
    //商户
    public static final String MERCHANT= "tbl_merchant";
    //账单列表
    public static final String BILL= "tbl_bill";
    //账单详情
    public static final String BILL_DETAI= "tbl_bill_detail";
    //APP菜单
    public static final String APP_MENU= "tbl_app_menu";
    //APP公司的角色菜单关系
    public static final String APP_ORG_ROLE_MENU= "tbl_app_org_role_menu";
    //APP公司角色关系
    public static final String APP_ORG_ROLE_R= "tbl_app_org_role_relation";
    //APP角色菜单关系
    public static final String APP_USER_ROLE_MENU= "tbl_app_user_role_menu";
    //APP用户角色关系
    public static final String APP_USER_ROLE_R= "tbl_app_user_role_relation";
    //APP用户角色关系
    public static final String APP_USER_ROLE= "tbl_app_user_role";
    //打卡规则表
    public static final String WK_GROUP= "wk_group";
    //打卡日期
    public static final String WK_CHECKINDATE = "wk_checkindate";
    //打卡时间
    public static final String WK_CHECKINTIME = "wk_checkintime";
    //打卡日期与时间关系表
    public static final String WK_DATE_TIME_RLAT = "wk_date_time_rlat";
    //打卡规则地址信息
    public static final String WK_LOC_INFOS = "wk_loc_infos";
    //特殊日期
    public static final String WK_SPE_DAYS = "wk_spe_days";
    //特殊日期时间关系
    public static final String WK_SPE_DAYS_TIME_RLAT = "wk_spe_time_rlat";
    //用户规则关系表
    public static final String WK_USER_GROUP_RLAT = "wk_user_group_rlat";
    //白名单
    public static final String WK_WHITE_LIST = "wk_white_list";
    //打卡记录
    public static final String WK_RECORD = "wk_record";
    //统计表
    public static final String WK_DAY_STATISTICS = "wk_day_statistics";
    //请假记录
    public static final String WK_LEAVE_RECORD = "wk_leave_record";
    //流程节点
    public static final String FLOW_NODE = "tbl_fl_node";
    //流程审批人
    public static final String FLOW_AUDIT = "tbl_fl_audit";
    //本地实人认证表
    public static final String USER_AUTH = "tbl_user_auth";
    //app版本
    public static final String APP_VERSION = "tbl_app_version";
    //路由
    public static final String ROUTER ="tbl_router" ;
    //企业内部用户
    public static final String INNER_USER ="v_inner_user" ;

    public static final String BURYING_POINT ="tbl_burying_point" ;


    public static final String WK_OUTWORK ="wk_outWork" ;
    //msg
    public static final String APP_MSG ="tbl_app_msg" ;


}