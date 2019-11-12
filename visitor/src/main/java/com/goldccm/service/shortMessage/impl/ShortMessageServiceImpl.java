package com.goldccm.service.shortMessage.impl;

import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.shortMessage.IShortMessageService;
import com.goldccm.util.YMNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("shortMessageService")
public class ShortMessageServiceImpl extends BaseServiceImpl implements IShortMessageService {
    @Autowired
    private IBaseDao baseDao;


    @Override
    public Result sendShortMessage(Map<String, Object> paramMap) {
        return null;
    }

    @Override
    public Map<String, Object> findCompanybyId(Integer companyId) {
        if(companyId == null || companyId == 0){
            return null;
        }
        String sql = " select companyName,addr from "+ TableList.COMPANY +" where id = "+companyId;

        return   baseDao.findFirstBySql(sql);
    }
    /** 
     * 友盟推送，如果有设备类型则按设备类型推送，如果无设备类型，则都进行推送。
     * @param deviceToken 设备号
    * @param deviceType 设备类型 1--ios  2--android
     * @param notification_title 消息标题
    * @param msg_content  消息类容
     * @return boolean 
     * @throws Exception    
     * @author cwf 
     * @date 2019/9/25 15:25
     */
    @Override
    public boolean YMNotification(String deviceToken,String deviceType,String notification_title ,String msg_content,String isOnline) throws Exception{
        boolean IOS=false;
        boolean android=false;
        if (isOnline!="F"){
        if (Constant.DEVICE_IOS.equals(deviceType)) {
             IOS = YMNotification.httpToYMIOS(deviceToken,notification_title , msg_content);
        } else if (Constant.DEVICE_ANDRIOD.equals(deviceType)) {
            android = YMNotification.httpToYMAndroid(deviceToken, notification_title, msg_content);
        }else {//没有Type时都发送
            IOS = YMNotification.httpToYMIOS(deviceToken, notification_title, msg_content);
             android = YMNotification.httpToYMAndroid(deviceToken, notification_title, msg_content);
        }
        }
        if (IOS||android){
            return true;
        }
        return false;
    }

}
