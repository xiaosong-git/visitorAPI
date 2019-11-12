package com.goldccm.framework;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 *
 * @Author linyb
 * @Date 2016/12/5 16:48
 *
 */
@Configuration              //Configuration 表示告诉Spring这是一个配置文件，LZ
@ComponentScan(
        basePackages = {"com.goldccm"},//ComponentScan 表示告诉Spring这个文件下带注解的类会被扫描到Spring的bean容器中，LZ
        excludeFilters = {@ComponentScan.Filter(type= FilterType.ANNOTATION,value = EnableWebMvc.class)} //告诉Spring不扫描过滤器， LZ
)
public class RootConfig {

}