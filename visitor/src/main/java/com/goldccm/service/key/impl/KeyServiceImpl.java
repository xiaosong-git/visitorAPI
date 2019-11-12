package com.goldccm.service.key.impl;

import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.key.IKeyService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.RedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 密钥相关的Service
 * Created by LZ on 2017/5/23.
 */
@Service("keyService")
public class KeyServiceImpl extends BaseServiceImpl implements IKeyService {
    @Override
    public String findKeyByStatus(String cstatus) throws  Exception{
        String key = null;
        //redis修改，原dbNum=8 现在dbNum=32
        key = RedisUtil.getStrVal("key_workKey",32);
        if(key == null){
            key =  findKeyFromDB(cstatus);
            if(key != null){
                //redis修改，原dbNum=8 现在dbNum=32
                RedisUtil.setStr("key_workKey",key, 32, null);
            }
        }
        return key;
    }

    /**
     * 从数据库中获取密钥
     * @param cstatus
     * @return
     */
    private String findKeyFromDB(String cstatus){
        if(!StringUtils.isEmpty(cstatus)){
            String sql = "select workKey from"+ TableList.KEY +" where cstatus = '"+cstatus+"'";
            Map<String,Object> param =  this.findFirstBySql(sql);
            if(param != null){
                return BaseUtil.objToStr(param.get("workKey"),null);
            }
        }
        return null;
    }

}
