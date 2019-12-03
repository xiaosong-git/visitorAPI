package com.goldccm.service.checkInWork.impl;

import com.goldccm.framework.WebConfig;
import com.goldccm.persist.base.impl.BaseDaoImpl;
import com.goldccm.service.base.impl.BaseServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
public class CheckInWorkServiceImplTest extends BaseServiceImpl {

    @Autowired
    private BaseDaoImpl baseDao;
    @Test
    public void saveWork() {

        StringBuffer dateTimeRlatPrefixSql = new StringBuffer("INSERT INTO `visitor`.`tbl_wk_record`(`user_id`, `group_id`, `group_name`, `checkin_type`, `exception_type`, `checkin_time`, `location_title`, `location_detail`, `wifi_name`, `wifi_mac`, `checkin_divice`, `notes`, `mediaids`, `lat`, `lng` ) values");
        StringBuffer dateTimeRlatsuffixSql = new StringBuffer();
        for (int i=1;i<2;i++){
            dateTimeRlatsuffixSql.append("(125, 1, '日常考勤', '上班打卡', '地点异常', '1573747200', '依澜府', '四川省成都市武侯区益州大道中段784号附近', '办公一区', '3c:46:d8:0c:7a:70', '小米note3', '路上堵车，迟到了5分钟', 'WWCISP_G8PYgRaOVHjXWUWFqchpBqqqUpGj0OyR9z6WTwhnMZGCPHxyviVstiv_2fTG8YOJq8L8zJT2T2OvTebANV-2MQ', 30547645, 104063236),");
        }
        int[] locs = baseDao.batchUpdate(dateTimeRlatPrefixSql + dateTimeRlatsuffixSql.substring(0, dateTimeRlatsuffixSql.length() - 1));


    }
}