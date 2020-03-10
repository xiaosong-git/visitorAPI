package com.goldccm.service.buryingPoint;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

public interface BuryingPointService extends IBaseService {

    Result save(Map<String, Object> paramMap);
}
