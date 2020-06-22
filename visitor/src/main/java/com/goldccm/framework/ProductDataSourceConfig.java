package com.goldccm.framework;

import com.alibaba.druid.pool.DruidDataSource;
import com.goldccm.model.compose.Constant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 框架持久层配置
 * @Date  2016/7/20 10:55
 * @author linyb
 */
@Configuration
@EnableTransactionManagement  //开启事务
@Profile("prod")
public class ProductDataSourceConfig {

    /**
     * 数据源配置
     * @Date  2016/7/21 15:19
     * @author linyb
     */

    @Bean
    public DataSource dataSource()  {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://rm-bp1v30gvxn3h81ytx.mysql.rds.aliyuncs.com:3306/visitor?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&autoReconnect=true");
        dataSource.setUsername("xiaosong");
        dataSource.setPassword("Xsafe!@1v$Lq");
        dataSource.setMaxActive(170);
        dataSource.setInitialSize(5);//50
        dataSource.setMaxWait(60000);
        System.out.println("生产环境启动");
        return dataSource;
    }
    /**
     * 事务配置
     * @Date  2016/7/21 15:19
     * @author linyb
     */
    @Bean
    public PlatformTransactionManager txManager() {
        return new DataSourceTransactionManager(dataSource());
    }
    /**
     * Spring jdbc模板 实例
     * @Date  2016/7/21 15:19
     * @author linyb
     */
    @Bean
    public JdbcTemplate getJdbcTemplate(){
        return new JdbcTemplate(dataSource());
    }
}