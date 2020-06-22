package com.goldccm.service.about.impl;

import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.about.IAboutService;
import com.goldccm.service.base.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/18.
 */
@Service("aboutService")
public class AboutServiceImpl extends BaseServiceImpl implements IAboutService {
//    @Autowired
//    private IParamService paramService;
//    Logger logger = LoggerFactory.getLogger(UserFriendServiceImpl.class);

    @Override
    public Result patner(Map<String, Object> paramMap) throws Exception{

        List list = findList("select *"," from " + TableList.ABOUT_US);
        return ResultData.dataResult("success","获取成功",list);
    }
}
