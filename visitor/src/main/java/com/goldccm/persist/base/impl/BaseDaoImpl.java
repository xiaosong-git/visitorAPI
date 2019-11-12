package com.goldccm.persist.base.impl;



import com.goldccm.model.compose.Constant;
import com.goldccm.model.compose.PageModel;
import com.goldccm.persist.base.IBaseDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基础数据层增删改查
 */
@Repository("baseDao")
public class BaseDaoImpl implements IBaseDao {

    @Autowired
    public JdbcTemplate jdbcTemplate;

    /**
     * 根据id获取
     * @Date  2016/7/20 13:47
     * @Author linyb
     * @param tableName 表名
     * @param id
     */
    public Map<String, Object> findById(String tableName, Integer id) {
        if( StringUtils.isBlank(tableName) || id==null){
            return null;
        }
        String sql = " select * from " +tableName +" where id = " +id;
        try {
            return jdbcTemplate.queryForMap(sql); // 解决queryForMap找不到数据报错的问题
        }catch (Exception e){
            return  null;
        }
    }
    /**
     * 根据条件判断数据是否存在
     * @Date  2019/7/26 13:47
     * @Author chenwf
     * @param fromSql 从哪张表查询条件
     */
    public Long findExist(String fromSql) {
        if( StringUtils.isBlank(fromSql) ){
            throw new NullPointerException("查询语句不能为空");
        }
        String sql = " select count(*)  "  +fromSql;
        try {
            return jdbcTemplate.queryForObject(sql,Long.class);// 解决queryForObject找不到数据报错的问题
        }catch (Exception e){
            return  null;
        }
    }

    /**
     * 修改状态
     * @Date  2016/7/20 14:01
     * @Author linyb
     * @param tableName  表名
     * @param status  状态
     * @param id
     */
    public int updateStatus(String tableName, Integer status, Integer id) {
        if( StringUtils.isBlank(tableName.toString()) || status == null || id == null){
            return 0;
        }
        String sql = " update "+tableName +" set status = " +status +" where id = " +id;
        return jdbcTemplate.update(sql);
    }

    /**
     * 修改
     * @Date  2016/7/20 14:27
     * @Author linyb
     */
    public int update(String tableName, Map<String, Object> params) {
        if(params==null){
            throw new NullPointerException("修改的参数不能不为空");
        }
        if(params!=null && params.get("id")==null){
            throw new NullPointerException("id不能为空");
        }
        if(params!=null && params.size() <2){
            throw new RuntimeException("参数不能为空");
        }
        StringBuilder sb = new StringBuilder(" update "+tableName +" set ");
        Set<String> keySet = params.keySet();
        for (String key : keySet){
            if("id".equals(key)) continue;
            Object value = params.get(key);
            if("null".equals(value+"")){
                sb.append(key +" = null ,");
            }else if(value instanceof  String){
                sb.append(key +" = '" +value +"' ,");
            }else if(value instanceof Integer){
                sb.append(key +" = " +value +" ,");
            }else if(value instanceof Long){
                sb.append(key +" = " +value +" ,");
            }else if(value instanceof BigDecimal){
                sb.append(key +" = " +value +" ,");
            }else if(value instanceof Date){
                sb.append(key +" = '" +new SimpleDateFormat(Constant.DATE_FORMAT_DEFAULT).format(value)+"' ,");
            }else if(value == null){
                sb.append(key +" = null ,");
            }
        }
        String sql = sb.substring(0,sb.length()-1);  //去拼接的最后一个 ,去掉逗号
        sql += " where id = " + params.get("id") ;
        return jdbcTemplate.update(sql);
    }
    /**
     * 根据id删除(物理删除)
     * @Date  2016/7/20 14:50
     * @Author linyb
     */
    public int delete(String tableName, Integer id) {
        if( StringUtils.isBlank(tableName)  || id == null){
            return 0;
        }
        String sql = " delete from "+tableName +" where id = "+id ;
        return jdbcTemplate.update(sql);
    }
    /**
     * 根据sql删除或者修改
     * @Author linyb
     * @Date 2016/7/27 23:05
     */
    public int deleteOrUpdate(String sql){
        if(StringUtils.isBlank(sql)){
            return 0;
        }
        return jdbcTemplate.update(sql);
    }

    /**
     * 保存一条数据，返回主键
     * @Date  2016/7/20 15:23
     * @Author linyb
     */
    public int save(String tableName, Map<String, Object> params) {
        if(params==null){
            throw new NullPointerException("参数不能不为空");
        }
        if(params!=null && params.size() <1){
            throw new RuntimeException("参数不能为空");
        }
        StringBuilder sb = new StringBuilder("insert into "+tableName +"(");
        Set<String> keySet = params.keySet();
        for (String key: keySet) {
            sb.append(key+",");
        }
        sb = new StringBuilder(sb.substring(0,sb.length()-1)+") values( ");
        for (String key: keySet) {
            Object value = params.get(key);
            if(value instanceof  String){
                sb.append(" '"+params.get(key)+"',");
            }else if(value instanceof Integer){
                sb.append(params.get(key)+",");
            }else if(value instanceof Long){
                sb.append(params.get(key)+",");
            }else if(value instanceof Float){
                sb.append(params.get(key)+",");
            }else if(value instanceof Double){
                sb.append(params.get(key)+",");
            }else if(value instanceof BigDecimal){
                sb.append(params.get(key)+",");
            }else if(value instanceof BigInteger){
                sb.append(params.get(key)+",");
            }else if(value instanceof Date){
                sb.append(" '"+new SimpleDateFormat(Constant.DATE_FORMAT_DEFAULT).format(value)+"',");
            }
        }
        final String sql = sb.substring(0,sb.length()-1)+")";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update( new PreparedStatementCreator(){
                                 public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                                     PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                                     return ps;
                                 }
                             },
                keyHolder);
        return keyHolder.getKey().intValue();
    }
    /**
     * 获取分页信息
     * @Date  2016/7/21 16:15
     * @param  coloumSql 要查询的列的xinx
     * @param  fromSql 从哪些表查询
     * @param  pageNum 第几页
     * @param  pageSize 查询多少条
     */
    public PageModel findPage(String coloumSql, String fromSql, Integer pageNum, Integer pageSize) {
        if(StringUtils.isBlank(coloumSql) || StringUtils.isBlank(fromSql) ){
            throw new NullPointerException("分页查询语句不能为空");
        }
        pageNum  = (pageNum  == null || pageNum == 0) ? 1 : pageNum; //为空设置默认为1
        pageSize = pageSize == null ? Constant.PAGESIZE : pageSize;//为空设置默认
        String sql = coloumSql +fromSql + " limit "+(pageNum - 1)*pageSize + ", "+pageSize ;
        String countSql = " select count(*) "+fromSql;

        PageModel page = new PageModel(pageNum,pageSize);
        Long totalSize = jdbcTemplate.queryForObject(countSql,Long.class);
        page.setTotal(totalSize);

        Long totalNum = ((totalSize % pageSize ==0) ? (totalSize / pageSize) : (totalSize / pageSize + 1));
        page.setTotalPage(totalNum);

        List<Map<String,Object>> data = jdbcTemplate.queryForList(sql);
        page.setRows(data);
        return page;
    }
    /**
     * 获取分页信息
     * @Date  2016/7/21 16:15
     * @param  offset 偏移
     * @param  pageSize 查询多少条
     * @param  coloumSql 要查询的列的信息
     * @param  fromSql 从哪些表查询
     */
    public PageModel findPage(Integer offset, Integer pageSize, String coloumSql, String fromSql) {
        if(StringUtils.isBlank(coloumSql) || StringUtils.isBlank(fromSql) ){
            throw new NullPointerException("分页查询语句不能为空");
        }
        offset  = offset  == null ? 0 : offset; //为空设置默认为1
        pageSize = pageSize == null ? Constant.PAGESIZE: pageSize;//为空设置默认
        String sql = coloumSql +fromSql + " limit "+offset + ", "+pageSize ;
        String countSql = " select count(*) "+fromSql;
        Integer pageNum = offset / pageSize + 1 ;

        PageModel page = new PageModel(pageNum,pageSize);
        Long totalSize = jdbcTemplate.queryForObject(countSql,Long.class);
        page.setTotal(totalSize);

        Long totalNum = ((totalSize % pageSize ==0) ? (totalSize / pageSize) : (totalSize / pageSize + 1));
        page.setTotalPage(totalNum);

        List<Map<String,Object>> data = jdbcTemplate.queryForList(sql);
        page.setRows(data);
        return page;
    }

    /**
     * 获取列信息
     * @Date  2016/7/26 08:47
     * @Author linyb
     */
    public List findList(String coloumSql, String fromSql) {
        if(StringUtils.isBlank(coloumSql) || StringUtils.isBlank(fromSql) ){
            throw new NullPointerException("查询语句不能为空");
        }
        List<Map<String,Object>> data = jdbcTemplate.queryForList(coloumSql + " " + fromSql);
        return data;
    }
    /**
     * 批量执行sql
     * @Author linyb
     * @Date 2016/7/27 23:36
     */
    public int[] batchUpdate(String... sql) {
        if(sql != null && sql.length >0 ){
            return jdbcTemplate.batchUpdate(sql);
        }
        return new int[0];
    }

    /**
     * 根据sql获取符合条件的第一条数据
     * @Date  2016/7/28 16:24
     * @Author linyb
     */
    public Map<String,Object> findFirstBySql(String sql){
        if(StringUtils.isBlank(sql)){
            return null;
        }
        List<Map<String,Object>> list = jdbcTemplate.queryForList(sql);
        if(list!=null && !list.isEmpty()){
            return  list.get(0);
        }
        return null;
    }

    /**
     * 返回对应类型的数据
     * @Author Linyb
     * @Date 2016/12/14 15:27
     */
    public Object queryForObject(String sql, Class clazz) {
        return  jdbcTemplate.queryForObject(sql,clazz);
    }
}