package com.goldccm.service.inAndOutService;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author chenwf
 * @date 2019/8/12 14:36
 */
public interface IInAndOutService extends IBaseService {



  Result getInOutTxt(Map<String, Object> paramMap, HttpServletResponse response) throws Exception;
}
