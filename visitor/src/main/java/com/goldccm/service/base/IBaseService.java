package com.goldccm.service.base;








import com.goldccm.model.compose.PageModel;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/26 0026.
 */
public interface IBaseService   {


    /**
     * 根据id获取
     * @param tableName 表名
     * @param id
     */
    Map<String,Object> findById(String tableName, Integer id);

    /**
     * 修改状态
     * @param tableName  表名
     * @param status  状态
     * @param id
     */
    int updateStatus(String tableName, Integer status, Integer id);

    /**
     * 修改
     * @param tableName 表名
     * @param params 要修改的字段
     */
    int update(String tableName, Map<String, Object> params);
    /**
     * 根据sql删除或更改
     * @param sql 表名
     */
    int deleteOrUpdate(String sql);
    /**
     * 根据id删除
     * @param tableName 表名
     * @param id
     */
    int delete(String tableName, Integer id);
    /**
     * 插入一条数据，返回主键
     * @Date  2016/7/20 15:20
     * @author linyb
     */
    int save(String tableName, Map<String, Object> params);
    /**
     * 获取分页信息
     * @Date  2016/7/21 16:15
     * @param coloumSql 要查询的列的信息
     * @param fromSql 从哪些表查询
     * @param  pageNum 第几页
     * @param  pageSize 查询多少条
     */
    PageModel findPage(String coloumSql, String fromSql, Integer pageNum, Integer pageSize);
    /**
     * 获取分页信息
     * @Date  2016/7/21 16:15
     * @param  offset 偏移
     * @param  pageSize 查询多少条
     * @param  coloumSql 要查询的列的信息
     * @param  fromSql 从哪些表查询
     */
    PageModel findPage(Integer offset, Integer pageSize, String coloumSql, String fromSql);

    /**
     * 不分页获取列表信息
     * @Date  2016/7/26 08:46
     * @author linyb
     * @param coloumSql 列信息
     * @param fromSql  从哪里查询
     */
    List findList(String coloumSql, String fromSql);
    /**
     * 根据sql获取符合条件的第一条数据
     * @Date  2016/7/28 16:24
     * @author linyb
     */
    Map<String,Object> findFirstBySql(String sql);

}