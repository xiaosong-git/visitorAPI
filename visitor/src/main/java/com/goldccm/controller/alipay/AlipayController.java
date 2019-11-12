package com.goldccm.controller.alipay;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.alipay.impl.AliPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/pay")
public class AlipayController extends BaseController {

	@Autowired
	public AliPayServiceImpl payService;

  @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
  @RequestMapping("/createOrder")
  @ResponseBody
  public Result createOrder(HttpServletRequest request){

		try {

			Result s = payService.trade_precreate(request);
			return s;
		}catch (Exception e){
			e.printStackTrace();
			return Result.fail();
		}
  }

	@AuthCheckAnnotation(checkLogin = false,checkVerify = false, checkRequestLegal = false)
	@RequestMapping("/paynotify")
	@ResponseBody
	public String paynotify(HttpServletRequest request){
//		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		return payService.paynotify(requestParams);


	}

}
