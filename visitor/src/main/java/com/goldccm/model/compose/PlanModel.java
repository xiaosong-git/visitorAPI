package com.goldccm.model.compose;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/5/29.
 */
public class PlanModel {
    private PageModel pageModel;
    private BigDecimal shouldRepayMoney;
    private BigDecimal haveRepayMoney;
    private BigDecimal notRepayMoney;
    private String curMonth;

    public String getCurMonth() {
        return curMonth;
    }

    public void setCurMonth(String curMonth) {
        this.curMonth = curMonth;
    }

    public BigDecimal getNotRepayMoney() {
        return notRepayMoney;
    }

    public void setNotRepayMoney(BigDecimal notRepayMoney) {
        this.notRepayMoney = notRepayMoney;
    }

    public PageModel getPageModel() {
        return pageModel;
    }

    public void setPageModel(PageModel pageModel) {
        this.pageModel = pageModel;
    }

    public BigDecimal getShouldRepayMoney() {
        return shouldRepayMoney;
    }

    public void setShouldRepayMoney(BigDecimal shouldRepayMoney) {
        this.shouldRepayMoney = shouldRepayMoney;
    }

    public BigDecimal getHaveRepayMoney() {
        return haveRepayMoney;
    }

    public void setHaveRepayMoney(BigDecimal haveRepayMoney) {
        this.haveRepayMoney = haveRepayMoney;
    }
}
