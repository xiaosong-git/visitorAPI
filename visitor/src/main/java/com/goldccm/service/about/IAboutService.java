package com.goldccm.service.about;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

public interface IAboutService extends IBaseService {
    Result patner(Map<String, Object> paramMap) throws Exception;
}
