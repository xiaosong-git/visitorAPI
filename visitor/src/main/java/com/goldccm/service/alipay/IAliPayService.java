package com.goldccm.service.alipay;

import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.goldccm.model.compose.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IAliPayService {
   
    /** 
     * 生成订单信息并返回客户端
     * @param request
     * @return java.lang.String
     * @throws Exception    
     * @author cwf 
     * @date 2019/9/2 10:32
     */
    Result trade_precreate(HttpServletRequest request);

    String paynotify(Map requestParams);

    void timeOut();

    AlipayTradeQueryResponse checkTrade(String tradeNo);

   AlipayTradeRefundResponse Refund(String tradeNo, String amount);

    int setTradeStatus(String status);
}
