package com.goldccm.service.visitor;

import com.goldccm.model.compose.Result;
import com.goldccm.service.base.IBaseService;

import java.util.Map;

public interface IInnerVisitorService extends IBaseService {

    Result innerVisitRequest(Map<String, Object> paramMap) throws Exception;

    Result innerVisitResponse(Map<String, Object> paramMap) throws Exception;

    String sendPhotos(String innerUrl) throws Exception;



}
