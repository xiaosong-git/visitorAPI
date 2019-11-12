package com.goldccm.model.plan;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 消费计划
 * @author Administrator
 *
 */
public class PurchasePlan implements Serializable{
	private String execTime;//执行还款时间,HH:mm:ss
	
	private BigDecimal amount;//消费金额

	private String transName = "消费";

	private String isFirst;//是否是第一笔还款的第一笔消费

	private String isLast;//是否是最后一笔

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

	public String getIsFirst() {
		return isFirst;
	}

	public void setIsFirst(String isFirst) {
		this.isFirst = isFirst;
	}

	public String getTransName() {
		return transName;
	}

	public void setTransName(String transName) {
		this.transName = transName;
	}

	public String getIsLast() {
		return isLast;
	}

	public void setIsLast(String isLast) {
		this.isLast = isLast;
	}
}
