package com.goldccm.service.errorLog.impl;

import cn.hutool.crypto.SmUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.errorLog.IErrorLogService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.DESUtil;
import com.goldccm.util.newworld.NewWorldAuth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @program: visitor
 * @description: 错误日志上传
 * @author: cwf
 * @create: 2019-08-26 23:32
 **/
@Service("errorLogService")
public class IErrorLogServiceImpl extends BaseServiceImpl implements IErrorLogService {
    Logger log = LoggerFactory.getLogger(IErrorLogServiceImpl.class);

    @Override
    public Result saveErrorLog(Map<String, Object> paramMap) throws Exception {

        int save = save(TableList.ERROR_LOG, paramMap);
        return save>0?Result.success():Result.fail();
    }

    @Override
    public Result test(Map<String, Object> paramMap) {

        List<Map<String,Object>> list = findList("select id,idNO,realName from", " tbl_user where (bid is null or bid='')" +
                "and isAuth='T' and idNO is not null and idNO<>'' limit 100");
        String key = "iB4drRzSrC";
        String decode =null;
        for (Map<String, Object> map : list) {
           String  idNO = BaseUtil.objToStr(map.get("idNO"),"");
           String  realName = BaseUtil.objToStr(map.get("realName"),"");

            decode= DESUtil.decode(key, idNO);
            JSONObject jsonObject = NewWorldAuth.sendPost(decode, realName, null);
            if ("0".equals(jsonObject.getString("code"))){
                String data1=jsonObject.getString("data");
                if (StringUtils.isNotBlank(data1)){
                    data1 = SmUtil.sm4(NewWorldAuth.SERVER_KEY.getBytes()).decryptStrFromBase64(data1);

                    JSONObject value = JSON.parseObject(data1);
                    System.out.println(data1);
                    log.info("data信息为{}",value.toJSONString());
                    String  bid = value.getString("bid");
                    map.put("bid",bid);

                    update(TableList.USER,map);
                    log.info("服务端响应解密后数据：" + jsonObject);
                }
            }else{
                log.info("失败原因：{}",jsonObject.getString("msg"));
            }
        }
        return null;
    }
}
