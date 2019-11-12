package com.goldccm.Quartz;

import com.goldccm.service.alipay.impl.AliPayServiceImpl;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * @program: visitor
 * @description: test
 * @author: cwf
 * @create: 2019-09-16 22:26
 **/
@Component
public class QuartzJob implements Job {
    Logger logger = LoggerFactory.getLogger(QuartzInit.class);
    @Autowired
    public AliPayServiceImpl alipayService;
    /**
     * Created by cwf
     * Date: on 2019/9/17 16:28.
     * Description: 定时扫描工作
     */

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

            logger.info("——————定时器——————");
            alipayService.timeOut();

        }


}
