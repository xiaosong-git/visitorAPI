package com.goldccm.service.param.impl;

import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.param.IParamService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.RedisUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author linyb
 * @Date 2017/4/15 21:08
 */
@Service("paramService")
public class ParamServiceImpl extends BaseServiceImpl implements IParamService {

    @Override
    public String findValueByName(String paramName) {
        //先从缓存中读取数据
        String value = null;
        value = findValueByNameFromRedis(paramName);
        //缓存中不存在，就从数据库中取值，并把值存入缓存中
        if (value == null){
            value = findValueByNameFromDB(paramName);
            if(value != null){
                //redis修改，原dbNum=8 现在dbNum=32
                RedisUtil.setStr("params_" + paramName, value, 32, null);
            }
        }
        return value;
    }

    /**
     * 从缓存中获取参数
     * @param paramName 参数名
     * @return
     */
    private String findValueByNameFromRedis(String paramName){
        //redis修改，原dbNum=8 现在dbNum=32
        String value = RedisUtil.getStrVal("params_" + paramName,32);
        return value;
    }

    /**
     * 从数据库中获取系统参数
     * @param paramName
     * @return
     */
    private String findValueByNameFromDB(String paramName){
        String sql =" select * from "+ TableList.PARAM +" where paramName = '"+paramName+"'";
        Map<String,Object> param =  this.findFirstBySql(sql);
        if( param != null){
            String value = BaseUtil.objToStr(param.get("paramText"),null);
            return value;
        }
        return null;
    }
}


