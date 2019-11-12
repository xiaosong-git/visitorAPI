package com.goldccm.service.checkInWork;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: visitor
 * @description:
 * @author: cwf
 * @create: 2019-11-04 10:25
 **/
public interface CheckInWorkService extends IBaseService {

    Result saveGroup(HttpServletRequest request);

}
