package com.goldccm.controller.about;

import com.goldccm.annotation.AuthCheckAnnotation;
import com.goldccm.controller.base.BaseController;
import com.goldccm.model.compose.Result;
import com.goldccm.service.about.IAboutService;
import com.goldccm.service.alipay.impl.AliPayServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/about")
@Api(tags="关于",value = "短信管理")
public class aboutController extends BaseController {

	@Autowired
	public IAboutService aboutService;

  @AuthCheckAnnotation(checkLogin = false,checkVerify = true, checkRequestLegal = true)
  @PostMapping("/us")
  @ResponseBody
  @ApiOperation(value="关于我们")
  @ApiImplicitParams({
  		@ApiImplicitParam(name = "test",dataType="String",paramType="query"),
  		@ApiImplicitParam(name = "test1",dataType="String",paramType="query")
  })
  public Result us(HttpServletRequest request){

		try {
			Map<String,Object> paramMap = getParamsToMap(request);
			Result s = aboutService.patner(paramMap);
			return s;
		}catch (Exception e){
			e.printStackTrace();
			return Result.fail();
		}
  }

}
