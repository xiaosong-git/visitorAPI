package com.goldccm.util;

public class TokenUtil {
    /**
     * "发送"
     "request:GET http://99test.51zcm.cc/tcapi/user/querySettleInfo?date=2018-08-28&factor=20180828141110&requestVer=iOS&threshold=BCE3CC1808281411106C78F&token=e756983f-ed63-4398-a124-8a006a23d18b&userId=225"
     "body:"
     "接收:"
     "{\"verify\":{\"sign\":\"userFail\",\"desc\":\"请求非法!\"}}"
     * @param args
     * @throws Exception
     */

    public static void main(String[] args) throws Exception{
        String userId = "834";
        String factor = "20191014173123";
        String token = "7f0b25d8-86d0-445a-b346-7eae009dfd65";
        String threshold = "5B6FD21A29F863AC3561FB4913FCFD3E";
        System.out.println(TokenUtil.checkRequestLegal(userId, factor,token,threshold));
    }

    public static boolean checkRequestLegal(String userId, String factor, String token, String threshold) throws Exception{
        //1.取userId,补足12位，前补F
        userId = MD5Util.MD5(makePreStr(userId,12,"F"));
        //获取当前日期
        String currentDate =  MD5Util.MD5(factor);
        //token
        token = MD5Util.MD5(token);
        StringBuffer sb = new StringBuffer();
        sb.append(userId.substring(6, 12)).append(currentDate.substring(2, 14)).append(token.substring(5,10));
        String key = MD5Util.MD5(sb.toString()).toUpperCase();
        System.out.println("key:"+key);
        return key.equals(threshold);
    }


    public static String makePreStr(String value,int len,String flag){
        if (value==null){
            return "";
        }
        int valueLen = value.length();
        for (int i=valueLen;i<len;i++){
            value = "F"+value;
        }
        return value;
    }
}
