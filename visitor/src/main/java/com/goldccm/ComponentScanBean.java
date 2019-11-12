package com.goldccm;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 开启扫描spring扫描组件的功能，扫描该类对应包以及子包下所有的组件
 * @Date  2016/6/3 16:30
 * @Author linyb
 */
@Configuration //表名此Java类是一个配置类
@ComponentScan
public class ComponentScanBean {
}