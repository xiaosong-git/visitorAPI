package com.goldccm.service.news.impl;

import com.goldccm.model.compose.PageModel;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.news.INewsService;
import org.springframework.stereotype.Service;

/**
 * Description:
 * Author: LZ
 * Date:2018/1/22 14:22
 */
@Service("newsServiceImpl")
public class NewsServiceImpl extends BaseServiceImpl implements INewsService {
    @Override
    public Result findByStatus(String status, Integer pageNum, Integer pageSize) throws Exception {
        String sql = "  from "+ TableList.NEWS + " where  newsStatus = '"+status+"' order by newsDate desc";
        PageModel pageModel = this.findPage("select * ",sql,pageNum,pageSize);
        return ResultData.dataResult("success","获取成功",pageModel);
    }
}
