package com.goldccm.service.alipay.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.IBaseDao;
import com.goldccm.service.alipay.IAliPayService;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.DateUtil;
import com.goldccm.util.ParamDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service("alipayService")
public class AliPayServiceImpl extends BaseServiceImpl implements IAliPayService {
    @Autowired
    private IBaseDao baseDao;
    Logger logger = LoggerFactory.getLogger(AliPayServiceImpl.class);

    @Override
    public Result trade_precreate(HttpServletRequest request) {
        String subject=request.getParameter("subject");
        String body=request.getParameter("body");
        String total_amount=request.getParameter("total_amount");
        String user_id=request.getParameter("user_id");
        String apply_id=request.getParameter("apply_id");
        //商品的标题/交易标题/订单标题/订单关键字等。 如大乐透
        logger.info("subject:"+subject);
        //对一笔交易的具体描述信息。如果是多种商品，请将商品描述字符串累加传给body。 如Iphone6 16G
        logger.info("body:"+body);
        //订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]
        logger.info("total_amount:"+total_amount);
        logger.info("apply_id:"+apply_id);
        //实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient(ParamDef.findAliByName("UNIFIED_ORDER_URL"),
                ParamDef.findAliByName("APP_ID"),
                ParamDef.findAliByName("APP_PRIVATE_KEY"),
                ParamDef.findAliByName("FORMAT"),
                ParamDef.findAliByName("CHARSET"),
                ParamDef.findAliByName("APP_PUBLIC_KEY"),
                ParamDef.findAliByName("SIGN_TYPE"));
        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest aliRequest = new AlipayTradeAppPayRequest();
    //	  AlipayRequest request = new AlipayTradeQueryRequest();

        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        //对一笔交易的具体描述信息。如果是多种商品，请将商品描述字符串累加传给body。 如Iphone6 16G
        model.setBody(body);
        //商品的标题/交易标题/订单标题/订单关键字等。 如大乐透
        model.setSubject(subject);
        //商户网站唯一订单号 如70501111111S001111119
        Long tradeSuffix=System.currentTimeMillis();
        String tradeNo=user_id+tradeSuffix.toString();
        logger.info(tradeNo+","+user_id);
        logger.info("生成订单号：{},用户id={}",tradeNo,user_id);
        model.setOutTradeNo(tradeNo);
        //该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。 该参数数值不接受小数点， 如 1.5h，可转换为 90m。
        //注：若为空，则默认为15d。
        model.setTimeoutExpress("1m");
//        model.setTimeExpire("1m");
//        model.setTimeExpire(ParamDef.findAliByName("TIMEOUT_EXPRESS"));
        logger.info(ParamDef.findAliByName("TIMEOUT_EXPRESS"));
        //订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]
        model.setTotalAmount(total_amount);
        //销售产品码，商家和支付宝签约的产品码，为固定值 QUICK_MSECURITY_PAY
        model.setProductCode("QUICK_MSECURITY_PAY");
        aliRequest.setBizModel(model);
        aliRequest.setNotifyUrl(ParamDef.findAliByName("NOTIFY_URL"));
        //插入数据库
        Map<String, Object> saveMap=new HashMap<>();
        saveMap.put("trade_no",tradeNo);
        saveMap.put("trade_type",0);
        saveMap.put("user_id",user_id);
        saveMap.put("subject",subject);
        saveMap.put("body",body);
        saveMap.put("total_amount",total_amount);
        saveMap.put("seller","xiaosonanxin");
        saveMap.put("create_time", DateUtil.getSystemTime());
        saveMap.put("trade_status", "1");
        String sql="update "+TableList.ROOM_APPLY_RECORD+" set trade_no="+tradeNo+"" +
                " where id="+apply_id;
        String checkSql="select bd.trade_no,bd.response_body from "+TableList.ROOM_APPLY_RECORD+" rar left join " +
                TableList.BILL_DETAI+" bd on bd.trade_no=rar.trade_no where rar.trade_no >0 and rar.id="+apply_id;
        Map<String, Object> check = findFirstBySql(checkSql);
        logger.info("checksql: "+checkSql);
        logger.info("check: "+check);
        try {
            if (check!=null) {
                if (check.containsKey("response_body")) {
                    logger.info("返回待支付response.getBody成功");
                    return ResultData.dataResult("success", "获取订单成功", check.get("response_body"));
                }
            }
            int update = baseDao.deleteOrUpdate(sql);
            if (update<=0){
                logger.info("更新申请会议室信息失败");
                return Result.unDataResult("fail","更新申请会议室信息失败");
            }
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(aliRequest);
            saveMap.put("response_body", response.getBody());
            logger.info(response.getBody());//就是orderString 可以直接给客户端请求，无需再做处理。
            save(TableList.BILL_DETAI,saveMap);
            return ResultData.dataResult("success","获取订单成功",response.getBody());
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return Result.fail();
        }
    }
    /**
     * <pre>
     * 第一步:验证签名,签名通过后进行第二步
     * 第二步:按一下步骤进行验证
     * 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
     * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
     * 3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
     * 4、验证app_id是否为该商户本身。上述1、2、3、4有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
     * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
     * 在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
     * </pre>
     *
     */
    @Override
    public String paynotify(Map requestParams) {
        logger.info("支付宝支付结果通知"+requestParams.toString());
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<>();

        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
               logger.info(name+"   , values["+i+"]  "+values[i]);
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
           logger.info(valueStr);

        }
        //切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        try {
            boolean flag = AlipaySignature.rsaCheckV1(params,  ParamDef.findAliByName("ALIPAY_PUBLIC_KEY"), ParamDef.findAliByName("CHARSET"), "RSA2");
           logger.info("flag:"+flag);
            if(flag){
                logger.info("支付宝回调签名认证成功");
                // 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，
                // 校验成功后在response中返回success，校验失败返回failure
                int statuCode=1;
                statuCode =setTradeStatus(params.get("trade_status"));

                if(statuCode>1){
                    return this.check(params,statuCode);
                    //附加数据
//                    String passback_params = URLDecoder.decode(params.get("passback_params"));
                }
            }


        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "fail";
    }

    /**
     * 验证app_id是否为该商户本身。上述1、2、3、4有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
     * @param params
     * @param statuCode
     * @return void
     * @throws Exception
     * @author cwf
     * @date 2019/9/6 11:09
     */
    private String check(Map<String, String> params, int statuCode) throws AlipayApiException {
        //订单号
        String outTradeNo = params.get("out_trade_no");
        //付款金额
        String amount = params.get("buyer_pay_amount");

        String sql = "select * from "+TableList.BILL_DETAI+" where trade_no= "+outTradeNo;
        Map<String, Object> order = findFirstBySql(sql);
        // 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        if (order == null||order.isEmpty()) {
            logger.info("alipay订单号验证错误");

            throw new AlipayApiException("out_trade_no错误");
        }
        // 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        if (!amount.equals(BaseUtil.objToStr(order.get("total_amount"),"无"))) {
            logger.info("alipay金额验证错误:"+order.get("total_amount")+"amount:"+amount);
            throw new AlipayApiException("error total_amount");
        }
        // 3、校验通知中的seller_id（或者seller_email)是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
        // 第三步可根据实际情况省略
        // 4、验证app_id是否为该商户本身。
        if (!params.get("app_id").equals(ParamDef.findAliByName("APP_ID"))) {
            logger.info("alipay appid验证错误");
            throw new AlipayApiException("app_id不一致");
        }
        //支付宝交易号
        String ali_trade_no = params.get("trade_no");
        String notify_time = params.get("notify_time");
        String buyer_id = params.get("buyer_id");
        String trade_status =String.valueOf(statuCode);
        String updateSql="update "+TableList.BILL_DETAI+" bd,"+TableList.ROOM_APPLY_RECORD+" rar set bd.ali_trade_no= '"+ali_trade_no+"',bd.notify_time='"+notify_time+
                "',bd.buyer_id='"+buyer_id+"',bd.trade_status='"+trade_status+"',rar.record_status='"+trade_status+"' where bd.trade_no='"+outTradeNo+"' and bd.trade_no=rar.trade_no ";
        logger.info("alipay updateSql: "+updateSql);
        try {

            int update = deleteOrUpdate(updateSql);
            logger.info("alipay update: "+update);
            if (update<=0){
                logger.info("alipay updateSql fail");
                return "fail";
            }else {
                logger.info("alipay return: success ");
                return "success";
            }
        }catch (Exception e){
            logger.error("阿里支付回调接口错误！",e);
            e.printStackTrace();
        }
        return "fail";

    }
    /** 
     * 更新超时未付款订单
     * @param 	 
     * @return void 
     * @throws Exception    
     * @author cwf 
     * @date 2019/9/27 9:13
     */
    @Override
    public void timeOut(){
        //改变会议室预定状态
        String sql="update "+TableList.ROOM_APPLY_RECORD+" rar,tbl_bill_detail bd set record_status="+ Constant.ROOM_STATUS_CANCLE +
                " and trade_status="+ Constant.ROOM_STATUS_CANCLE +
                " where STR_TO_DATE(rar.create_time,'%Y-%m-%d %H:%i:%s')<DATE_SUB(NOW(),INTERVAL 15 MINUTE) and \n" +
                "record_status ="+Constant.ROOM_STATUS_RESERVE+" and rar.trade_no=bd.trade_no";
        logger.debug(sql);
        int update = baseDao.deleteOrUpdate(sql);
        logger.info("定时更新预定会议室状态成功数： "+update);

    }
    /**
     * 支付宝查询订单接口
     * @param tradeNo
     * @return com.alipay.api.response.AlipayTradeQueryResponse
     * @throws Exception
     * @author cwf
     * @date 2019/9/26 22:28
     */
    @Override
    public  AlipayTradeQueryResponse checkTrade(String tradeNo) {

        AlipayClient alipayClient = new DefaultAlipayClient(ParamDef.findAliByName("UNIFIED_ORDER_URL"),
                ParamDef.findAliByName("APP_ID"),
                ParamDef.findAliByName("APP_PRIVATE_KEY"),
                ParamDef.findAliByName("FORMAT"),
                ParamDef.findAliByName("CHARSET"),
                ParamDef.findAliByName("ALIPAY_PUBLIC_KEY"),
                ParamDef.findAliByName("SIGN_TYPE"));
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+tradeNo+"\"," +
//                "\"trade_no\":\""+aliTradeNo+"\"," +
//                "\"org_pid\":\"2088101117952222\"," +
                "      \"query_options\":[" +
                "        \"TRADE_SETTE_INFO\"" +
                "      ]" +
                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return null;
        }
        if(response.isSuccess()){
           logger.info("调用成功");
            return response;
        } else {
           logger.info("调用失败");
            return null;
        }

    }


    /** 
     * 支付宝关闭订单接口
     * @param 	 
     * @return void 
     * @throws Exception    
     * @author cwf 
     * @date 2019/9/27 10:49
     */
    public static void closeTrade(String tradeNo){
        AlipayClient alipayClient = new DefaultAlipayClient(ParamDef.findAliByName("UNIFIED_ORDER_URL"),
                ParamDef.findAliByName("APP_ID"),
                ParamDef.findAliByName("APP_PRIVATE_KEY"),
                ParamDef.findAliByName("FORMAT"),
                ParamDef.findAliByName("CHARSET"),
                ParamDef.findAliByName("ALIPAY_PUBLIC_KEY"),
                ParamDef.findAliByName("SIGN_TYPE"));
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizContent("{" +
//                "\"trade_no\":\"2013112611001004680073956707\"," +
                "\"out_trade_no\":\""+tradeNo+"\"," +
                "\"operator_id\":\"45\"" +
                "  }");
        AlipayTradeCloseResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
    }
    /**
     * 支付宝退款接口
     * @param tradeNo tbl_bill_detail 中的trade_no
     * @param amount tbl_bill_detail 中的total_amount
     * @return com.alipay.api.response.AlipayTradeRefundResponse
     * @throws Exception
     * @author cwf
     * @date 2019/9/26 22:28
     */
   @Override
    public  AlipayTradeRefundResponse Refund(String tradeNo, String amount){
        AlipayClient alipayClient = new DefaultAlipayClient(ParamDef.findAliByName("UNIFIED_ORDER_URL"),
                ParamDef.findAliByName("APP_ID"),
                ParamDef.findAliByName("APP_PRIVATE_KEY"),
                ParamDef.findAliByName("FORMAT"),
                ParamDef.findAliByName("CHARSET"),
                ParamDef.findAliByName("ALIPAY_PUBLIC_KEY"),
                ParamDef.findAliByName("SIGN_TYPE"));
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();//创建API对应的request类
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+tradeNo+"\"," +
//                "    \"trade_no\":\""+aliTradeNo+"\"," +
                "    \"out_request_no\":\"1000001\"," +
                "    \"refund_amount\":\""+amount+"\"" +
                "  }");//设置业务参数
        AlipayTradeRefundResponse response = null;//通过alipayClient调用API，获得对应的response类
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
       logger.info(response.getBody());
        return response;
    }
    //工具类文字转数字
    @Override
    public int setTradeStatus(String status){
        int statuCode=1;
        switch (status) // 判断交易结果
        {
            case "TRADE_CLOSED": //未付款交易超时关闭或支付完成后全额退款
                statuCode= 4;
                break;
            case "TRADE_FINISHED": // 交易结束并不可退款
                statuCode= 3;
                break;
            case "TRADE_SUCCESS": // 交易支付成功
                statuCode= 2;
                break;
            case "WAIT_BUYER_PAY": // 交易创建并等待买家付款
                statuCode= 1;
                break;
            default:
                break;
        }
        return  statuCode;
    }

    public static void main(String[] args) {
//        AlipayTradeQueryResponse alipayTradeQueryResponse = checkTradeTest("451569547870070");
        closeTrade("451569547870070");
        AlipayClient alipayClient = new DefaultAlipayClient(ParamDef.findAliByName("UNIFIED_ORDER_URL"),
                ParamDef.findAliByName("APP_ID"),
                ParamDef.findAliByName("APP_PRIVATE_KEY"),
                ParamDef.findAliByName("FORMAT"),
                ParamDef.findAliByName("CHARSET"),
                ParamDef.findAliByName("ALIPAY_PUBLIC_KEY"),
                ParamDef.findAliByName("SIGN_TYPE"));
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();//创建API对应的request类
        request.setBizContent("{" +
                "    \"out_trade_no\":\"101569591598537\"," +
//                "    \"trade_no\":\""+aliTradeNo+"\"," +
                "    \"out_request_no\":\"1000001\"," +
                "    \"refund_amount\":\"2\"" +
                "  }");//设置业务参数
        AlipayTradeRefundResponse response = null;//通过alipayClient调用API，获得对应的response类
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.print(response.getBody());

    }
}
