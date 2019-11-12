package com.goldccm.framework;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @program: visitor
 * @description: 监听器
 * @author: cwf
 * @create: 2019-09-17 15:38
 **/
@WebListener
@Component
public class AppContextListener implements ServletContextListener {

    /**
     * tomcat启动初始化
     */
    @Override
    public void contextInitialized(ServletContextEvent event)  {
        System.out.println("tomcat已经启动！");
    }

    /**
     * tomcat关闭
     */
    @Override
    public void contextDestroyed(ServletContextEvent event)  {
        System.out.println("tomcat已经关闭！开始关闭quartz！");
        try {
            SchedulerFactory sf = new StdSchedulerFactory();//创建新的调度器工厂
            Scheduler scheduler = null;
            scheduler = sf.getScheduler();//获取当前进程的所有定时器线程数据
            scheduler.shutdown(false);//关闭定时器线程

            System.out.println("关闭定时器线程成功！");
        } catch (Exception e) {
            System.out.println("关闭定时器线程失败！");
            e.printStackTrace();
        }
    }
}
