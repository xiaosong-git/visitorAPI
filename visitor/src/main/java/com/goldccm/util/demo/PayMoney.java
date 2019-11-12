package com.goldccm.util.demo;

import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class PayMoney {

	/**
	 * 本钱
	 */
	private Integer myAmount;
	
	/**
	 * 剩下钱
	 */
	private Integer toPayAmount;
	
	/**
	 * 付款天数
	 */
	private int dayLen;
	
	/**
	 * 不付钱开始时间
	 */
	private String noPayStartTime;
	/**
	 * 不付钱结束时间
	 */
	private String noPayEndTime;
	
	/**
	 * 支付最小
	 */
	private Integer minAmount;
	/**
	 * 银行的钱
	 */
	private Integer bankAmount = 0;
	
	
	/**
	 * 付钱记录
	 */
	private List<Map<String,String>> payResult;

	public Integer getMyAmount() {
		return myAmount;
	}

	public PayMoney setMyAmount(Integer myAmount) {
		this.myAmount = myAmount;
		return this;
	}

	public Integer getToPayAmount() {
		return toPayAmount;
	}

	public PayMoney setToPayAmount(Integer toPayAmount) {
		this.toPayAmount = toPayAmount;
		return this;
	}

	public List<Map<String, String>> getPayResult() {
		return payResult;
	}

	public PayMoney setPayResult(List<Map<String, String>> payResult) {
		this.payResult = payResult;
		return this;
	}
	
	public String getNoPayStartTime() {
		return noPayStartTime;
	}

	public PayMoney setNoPayStartTime(String noPayStartTime) {
		this.noPayStartTime = noPayStartTime;
		return this;
	}

	public String getNoPayEndTime() {
		return noPayEndTime;
	}

	public PayMoney setNoPayEndTime(String noPayEndTime) {
		this.noPayEndTime = noPayEndTime;
		return this;
	}

	public int getDayLen() {
		return dayLen;
	}

	public PayMoney setDayLen(int dayLen) {
		this.dayLen = dayLen;
		return this;
	}

	public Integer getMinAmount() {
		return minAmount;
	}

	public PayMoney setMinAmount(Integer minAmount) {
		this.minAmount = minAmount;
		return this;
	}

	public Integer getBankAmount() {
		return bankAmount;
	}

	public PayMoney setBankAmount(Integer bankAmount) {
		this.bankAmount = bankAmount;
		return this;
	}

	public static PayMoney build(){
		return new PayMoney();
	}
	
	public static List<Map<String,String>> getPayList(int myAmount,int toPayAmount,int miniAmount)throws Exception{
		if(miniAmount > myAmount){
			throw new Exception("本金小于最小还款数");
		}
		
		PayMoney payMoney = PayMoney.build()
				.setMyAmount(myAmount)
				.setToPayAmount(toPayAmount)
				.setMinAmount(miniAmount);
		List<Map<String,String>> results = new ArrayList<Map<String,String>>();
		while(payMoney.getToPayAmount() != 0){
			Map<String,String> result = new HashMap<String,String>();
			Random payR = new Random();
			int payAmount = 0;
			if(payMoney.getToPayAmount() < payMoney.getMinAmount()){//小于最小付款，直接付款
				payAmount = payMoney.getToPayAmount();
			}else{
				payAmount = payMoney.getMinAmount() + payR.nextInt(payMoney.getMyAmount() - payMoney.getMinAmount());
			}
			
			result.put("支付", payAmount+"");
			//支付完扣除我的还款金额，要更新付给银行的钱，和我剩下的钱
			payMoney.setToPayAmount(payMoney.getToPayAmount() - payAmount);
			payMoney.setMyAmount(payMoney.getMyAmount() - payAmount);
			payMoney.setBankAmount(payMoney.getBankAmount()+payAmount);

			Random backR = new Random();
			//银行还完了就不用退了
			if(payMoney.getToPayAmount() == 0){
				int backAmount = payMoney.getBankAmount();
				result.put("退回", backAmount+"");
				//退回的钱加入我的本钱,更新银行的钱
				payMoney.setMyAmount(payMoney.getMyAmount()+backAmount);
				payMoney.setBankAmount(payMoney.getBankAmount() - backAmount);
			}else{
				int backAmount = payMoney.getMinAmount() + backR.nextInt(payMoney.getBankAmount() - payMoney.getMinAmount());
				result.put("退回", backAmount+"");
				//退回的钱加入我的本钱,更新银行的钱
				payMoney.setMyAmount(payMoney.getMyAmount()+backAmount);
				payMoney.setBankAmount(payMoney.getBankAmount() - backAmount);
			}
			results.add(result);
		}
		payMoney.setPayResult(results);
		System.out.println("银行还款金额:"+payMoney.getToPayAmount());
		System.out.println("银行的钱"+payMoney.getBankAmount());
		System.out.println("我的本金"+payMoney.getMyAmount());
		return payMoney.getPayResult();
	}

	public static void main(String[] args)throws Exception{
		List<Map<String,String>> results = PayMoney.getPayList(1000, 10000, 800);
		
		int paySum = 0;
		for(Map<String,String> result:results){
			int pay = Integer.parseInt(result.get("支付"));
			paySum += pay;
		}
		System.out.println("总共还款"+paySum);
		System.out.println(JSONObject.toJSONString(results));
		
	}
	
	
}
