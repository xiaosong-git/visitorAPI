package com.goldccm.controller.alipay;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.alipay.impl.AliPayServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/pay")
@Api(tags="支付宝",value = "支付宝")
public class AlipayController extends BaseController {

	@Autowired
	public AliPayServiceImpl payService;

  @AuthCheckAnnotation(checkLogin = true,checkVerify = true, checkRequestLegal = true)
  @PostMapping("/createOrder")
  @ResponseBody
  @ApiImplicitParams({
		  @ApiImplicitParam(name = "subject",dataType="String",paramType="query"),
		  @ApiImplicitParam(name = "body",dataType="String",paramType="query")
  })
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
