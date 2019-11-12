package com.goldccm.model.compose;


import java.util.ArrayList;
import java.util.List;

/**
 * 分页
 * @Date  2016/7/21 15:52
 * @Author linyb
 */
public class PageModel {

    private Integer pageNum = 1; //第几页
    private Integer pageSize =10;//一页几条
    private List rows = new ArrayList();       //数据
    private Long totalPage;//总页数
    private Long total;//总记录数

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public Long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Long totalPage) {
        this.totalPage = totalPage;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public PageModel(Integer pageNum, Integer pageSize) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }
}