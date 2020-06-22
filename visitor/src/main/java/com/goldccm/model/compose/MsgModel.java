package com.goldccm.model.compose;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: goldccm
 * @description: websocket消息封装
 * @author: cwf
 * @create: 2020-04-09 14:05
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgModel {

    private Integer type; //消息类型
    private Long fromUserId; //来源
    private Long toUserId;//去向
    private String idHandleImgUrl;//真实照片
    private String headImgUrl;//虚拟照片
    private String realName;//真名
    private String niceName;//昵称
    private Object data;//需要传递的数据

    public static void main(String[] args) {
        MsgModel msgModel=new MsgModel();
        msgModel.setFromUserId(1l);
        msgModel.setToUserId(1l);
        String s = JSON.toJSONString(msgModel);
        String s1 = msgModel.toString();
        System.out.println(s);
        System.out.println(s1);
        int i = msgModel.hashCode();
        System.out.println(i);
    }
}
