package com.goldccm.service.notice.impl;

import com.alibaba.fastjson.JSON;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.notice.INoticeUserService;
import com.goldccm.service.param.IParamService;
import com.goldccm.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/5/9 14:47
 */
@Service("noticeUserSerivce")
public class NoticeUserServiceImpl extends BaseServiceImpl implements INoticeUserService {
    @Autowired
    private IParamService paramService;

    @Override
    public Map<String, Object> findByUserId(Integer userId) {
        Map<String, Object> map = null;

        Integer apiNewAuthCheckRedisDbIndex = Integer.valueOf(paramService.findValueByName("apiNewAuthCheckRedisDbIndex"));//存储在缓存中的位置
        //redis修改
        String noticeUser = RedisUtil.getStrVal(userId+"_noticeUser",apiNewAuthCheckRedisDbIndex);
        if(StringUtils.isBlank(noticeUser)){
            //缓存中不存在,从数据库查询
            map = this.findFirstBySql("select * from "+ TableList.USER_NOTICE +" where userId = "+userId);
        }else{
            map = JSON.parseObject(noticeUser, Map.class);
        }
        return map;
    }
}
