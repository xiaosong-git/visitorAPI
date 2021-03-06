package com.goldccm.framework;

import com.alibaba.druid.pool.DruidDataSource;
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
@Profile("dev")
public class DevelopDataSourceConfig {

    /**
     * 数据源配置
     * @Date  2016/7/21 15:19
     * @author linyb
     */

    @Bean
    public DataSource dataSource()  {

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

//            dataSource.setUrl("jdbc:mysql://rm-bp1v30gvxn3h81ytx.mysql.rds.aliyuncs.com:3306/visitor?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&autoReconnect=true");
//            dataSource.setUsername("xiaosong");
//            dataSource.setPassword("Xsafe!@1v$Lq");
//         dataSource.setUrl("jdbc:mysql://121.37.25.161:3306/visitor?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&autoReconnect=true");
//         dataSource.setUsername("root");
//         dataSource.setPassword("123456");
         dataSource.setUrl("jdbc:mysql://localhost:3306/visitor?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&autoReconnect=true&useSSL=false");
         dataSource.setUsername("root");
         dataSource.setPassword("root");
//            dataSource.setUrl("jdbc:mysql://134.175.44.10:3306/vistor?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&autoReconnect=true");
//            dataSource.setUsername("test");
//            dataSource.setPassword("pd123456");
            //测试
//            dataSource.setUrl("jdbc:mysql://192.168.1.54:3306/visitor?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&autoReconnect=true");
//            dataSource.setUsername("root");
//            dataSource.setPassword("flzxsqc!11");
        System.out.println("测试环境启动");
        dataSource.setMaxActive(170);
        dataSource.setInitialSize(5);//50
        dataSource.setMaxWait(60000);
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