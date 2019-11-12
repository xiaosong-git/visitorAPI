package com.goldccm.Quartz;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;

/**
 * @program: visitor
 * @description: 1
 * @author: cwf
 * @create: 2019-09-17 22:36
 **/
@Component
public class MyJobFactory extends AdaptableJobFactory {
    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    /**
     * @see org.springframework.scheduling.quartz.AdaptableJobFactory#createJobInstance(org.quartz.spi.TriggerFiredBundle)
     */
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        Object jobInstance = super.createJobInstance(bundle);
        // 实现Job的IOC管理
        autowireCapableBeanFactory.autowireBean(jobInstance);
        return jobInstance;
    }
}
