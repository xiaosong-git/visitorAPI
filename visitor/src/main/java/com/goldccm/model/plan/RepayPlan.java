package com.goldccm.model.plan;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 还款计划
 * @author LZ
 *
 */
public class RepayPlan implements Serializable{
	private String repayDetailDate;//还款日期,yyyy-MM-dd
	
	private String execTime;//执行还款时间,HH:mm:ss
	
	private BigDecimal amount;//还款金额

	private String isFirst;//是否是计划中的第一笔还款

	private String isLast;//是否是计划中的最后一笔还款

	private String rechargeType;//交易支付类型,debit(借记卡支付) balance(余额支付)
	
	private Integer purchaseNum;//对应有几笔消费

	private String transName = "还款";
	
	//扩展字段
	private List<PurchasePlan> purchasePlans ;//还款计划对应的消费计划

	public String getRepayDetailDate() {
		return repayDetailDate;
	}

	public void setRepayDetailDate(String repayDetailDate) {
		this.repayDetailDate = repayDetailDate;
	}

	public String getExecTime() {
		return execTime;
	}

	public void setExecTime(String execTime) {
		this.execTime = execTime;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public List<PurchasePlan> getPurchasePlans() {
		return purchasePlans;
	}

	public void setPurchasePlans(List<PurchasePlan> purchasePlans) {
		this.purchasePlans = purchasePlans;
	}

	public Integer getPurchaseNum() {
		return purchaseNum;
	}

	public void setPurchaseNum(Integer purchaseNum) {
		this.purchaseNum = purchaseNum;
	}

	public String getIsFirst() {
		return isFirst;
	}

	public void setIsFirst(String isFirst) {
		this.isFirst = isFirst;
	}

	public String getRechargeType() {
		return rechargeType;
	}

	public void setRechargeType(String rechargeType) {
		this.rechargeType = rechargeType;
	}


	public String getTransName() {
		return transName;
	}

	public void setTransName(String transName) {
		this.transName = transName;
	}

	@Override
	public String toString() {
		return "RepayPlan{" +
				"repayDetailDate='" + repayDetailDate + '\'' +
				", execTime='" + execTime + '\'' +
				", amount=" + amount +
				", isFirst='" + isFirst + '\'' +
				", rechargeType='" + rechargeType + '\'' +
				", purchaseNum=" + purchaseNum +
				", transName='" + transName + '\'' +
				", purchasePlans=" + purchasePlans +
				'}';
	}

	public String getIsLast() {
		return isLast;
	}

	public void setIsLast(String isLast) {
		this.isLast = isLast;
	}
}
