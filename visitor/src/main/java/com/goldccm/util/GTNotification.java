package com.goldccm.util;

/**
 * @program: goldccm
 * @description: PushtoSingle
 * @author: cwf
 * @create: 2019-12-09 11:03
 **/

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.base.notify.Notify;
import com.gexin.rp.sdk.exceptions.RequestException;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.NotificationTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.gexin.rp.sdk.template.style.Style0;
import org.apache.commons.lang3.StringUtils;

public class GTNotification {
    // 详见【概述】-【服务端接入步骤】-【STEP1】说明，获得的应用配置

    private static String appId = "VAnoRfQ8kk69vx2rrR9tS4";
    private static String appKey = "dnYA43YSz16MGwebloMGA6";
    private static String masterSecret = "Hr87hFYtt08cDbhIauIsY6";
//    static String CID = "3d2e89613f875ac8fecf1568a8d06e9d";
    // 别名推送方式
//     static String Alias = "18150797748";
    static String host = "http://sdk.open.api.igexin.com/apiex.htm";

    public static void main(String[] args) throws Exception {
        Single("92dda6957559467478435f0c8f0e3988","15659154890","素芬","素芬","透传");
//        Single("32d7047feb5e24dcafa91656a13b350e","18150797748","发","发","发");
////        Single("9fde86d15925a4eb3bb14f0feade83fb","18150797748","塑封","塑封","塑封");
//        Single("d23bedd070498c0c4f0fdd20fb2b2f68","18150797748","发哥","发哥","塑封");
//        Single("683a18644fd5f8bcdf5555f1a9a083fd","18150797748","宋伟","宋伟","宋伟");
    }
    public static boolean Single(String CID,String Alias,String title,String text,String transmissionContent){
        IGtPush push = new IGtPush(host, appKey, masterSecret);
        NotificationTemplate template = notificationTemplate(title,text,transmissionContent);
        SingleMessage message = new SingleMessage();
        message.setOffline(true);
        // 离线有效时间，单位为毫秒
        message.setOfflineExpireTime(24 * 3600 * 1000);
        message.setData(template);
        // 可选，1为wifi，0为不限制网络环境。根据手机处于的网络情况，决定是否下发
        message.setPushNetWorkType(0);
        Target target = new Target();
        target.setAppId(appId);
        if (StringUtils.isBlank(CID)){
            target.setAlias(Alias);
        }else {
            target.setClientId(CID);
        }
        IPushResult ret = null;
        try {
            ret = push.pushMessageToSingle(message, target);
        } catch (RequestException e) {
            e.printStackTrace();
            ret = push.pushMessageToSingle(message, target, e.getRequestId());
        }
        if (ret != null) {
            String str = ret.getResponse().toString();
            System.out.println(str);
            str = str.substring(str.indexOf("status=")+7,str.length()-1);
            if("successed_online".equals(str)){
                return true;
            }else {
                return false;
            }
        } else {
            System.out.println("个推服务器响应异常");
            return false;
        }

    }
    //厂商通道透传
    public static TransmissionTemplate transmissionTemplateDemo(String title,String Text,String transmissionContent){
        TransmissionTemplate template = new TransmissionTemplate();
        template.setAppId(appId);
        template.setAppkey(appKey);
        template.setTransmissionContent(transmissionContent);
        template.setTransmissionType(2);
        Notify notify = new Notify();
        notify.setTitle(title);
        notify.setContent(Text);
        //长度小于 1000 字节，通知带 intent 传递参数 推荐使用，intent 最好由 Android 开发工程师生成，生成方式见附录
//        notify.setIntent("intent:#Intent;launchFlags=0x10000000;package=com.pp.yl;component=你的包名 /com.getui.demo.MainActivity;i.parm1=12;end");
//
//        notify.setType(GtReq.NotifyInfo.Type._intent);
// notify.setUrl("https://dev.getui.com/");
//notify.setType(Type._url);
        template.set3rdNotifyInfo(notify);//设置第三方通知
        return template;
    }
//    public static NotificationTemplate getNotificationTemplate(String title,String Text,String transmissionContent) {
//        NotificationTemplate template = new NotificationTemplate();
//        // 设置APPID与APPKEY
//        template.setAppId(appId);
//        template.setAppkey(appKey);
//
//        Style0 style = new Style0();
//        // 设置通知栏标题与内容
//        style.setTitle(title);
//        style.setText(Text);
//        // 配置通知栏图标
//        style.setLogo("icon.png");
//        // 配置通知栏网络图标
//        style.setLogoUrl("");
//        // 设置通知是否响铃，震动，或者可清除
////        style.setRing(true);
////        style.setVibrate(true);
////        style.setClearable(true);
//            style.setChannel("123");
//            style.setChannelName("哈哈");
//        style.setChannelLevel(3); //设置通知渠道重要性
//        template.setStyle(style);
//
//        template.setTransmissionType(1);  // 透传消息接受方式设置，1：立即启动APP，2：客户端收到消息后需要自行处理
//        //透传消息
//        template.setTransmissionContent(transmissionContent);
//        return template;
//    }
    public static NotificationTemplate notificationTemplate(String title,String Text,String transmissionContent) {
        NotificationTemplate template = new NotificationTemplate();
        // 设置APPID与APPKEY
        template.setAppId(appId);
        template.setAppkey(appKey);
        // 透传消息接受方式设置，1：立即启动APP，2：客户端收到消息后需要自行处理
        template.setTransmissionType(1);
        template.setChannel("123");
        //设置通知渠道重要性
        template.setChannelLevel(3);
        template.setChannelName("haha");
        Style0 style = new Style0();
        // 设置通知栏标题与内容
        style.setTitle(title);
        style.setText(Text);
        // 配置通知栏图标
        style.setLogo("icon.png");
        style.setLogoUrl("");
        template.setStyle(style);
        return template;
    }

}