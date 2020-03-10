package com.goldccm.service.router.impl;

import com.goldccm.model.compose.PageModel;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.router.IRouterService;
import com.goldccm.util.BaseUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: goldccm
 * @description: 路由接口实现类
 * @author: cwf
 * @create: 2019-12-30 17:44
 **/
@Service("routerService")
public class RouterServiceImpl extends BaseServiceImpl implements IRouterService {

    @Override
    public Result router(Map<String, Object> paramMap, Integer pageNum, Integer pageSize) {
        String province = BaseUtil.objToStr(paramMap.get("province"),"0");
        String city = BaseUtil.objToStr(paramMap.get("city"),"0");
        String area = BaseUtil.objToStr(paramMap.get("area"),"0");
        PageModel page = findPage("select * "," from " + TableList.ROUTER+" where " +
                "province like IF('0'='"+province+"',province,'%"+province+"%')  and city like IF('0'='"+city+"',city,'%"+city+"%') and" +
                " area like IF('0'='"+area+"',area,'%"+area+"%')", pageNum, pageSize);
        return ResultData.dataResult("success","操作成功",page);
    }
}
