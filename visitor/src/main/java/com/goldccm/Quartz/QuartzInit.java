package com.goldccm.Quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @program: visitor
 * @description: 初始化
 * @author: cwf
 * @create: 2019-09-17 11:39
 **/
@Component
public class QuartzInit {
    Logger logger = LoggerFactory.getLogger(QuartzInit.class);
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

        @PostConstruct
        public  void timerInit() throws Exception{

            // 1、创建调度器Scheduler

            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            // 2、创建JobDetail实例，并与PrintWordsJob类绑定(Job执行内容)
            JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class)
                    .withIdentity("job1", "group1").build();
            // 3、构建Trigger实例,每隔1s执行一次
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "triggerGroup1")
                    .startNow()//立即生效
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(300)//每隔300s执行一次
                            .repeatForever()).build();//一直执行

            //4、执行

            scheduler.scheduleJob(jobDetail, trigger);
            logger.info("--------scheduler start ! ------------");
            scheduler.start();
        }
    }

