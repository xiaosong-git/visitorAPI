package com.goldccm.framework;

import com.goldccm.inteceptor.AuthCheckInteceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 使用Java类来配置  DispatcherServlet
 */
@Configuration
@EnableWebMvc  //启用springmvc
@ComponentScan( basePackages = {"com.goldccm"})  //启用扫描，
@EnableScheduling  //启用定时器
//@EnableCaching //开启缓存，可以跟redis等第三方缓存架构集成
public class WebConfig extends WebMvcConfigurerAdapter {

    /**
     * 配置静态资源的处理
     * @Date  2016/6/13 10:52
     * @author linyb
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        //将对静态资源的请求转发到默认的servlet上，而不是使用DispatcherServlet本身来请求
        configurer.enable();
    }


    /**
     * 配置视图解析器，
     * 没配置，默认使用BeanNameViewResolver（ID与视图名称匹配的Bean,并且查找bean要实现的view）
     * @Date  2016/6/13 10:53
     * @author linyb
     */
    @Bean
    public ViewResolver viewResolver(){
        //配置jsp视图解析器
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/view/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setExposeContextBeansAsAttributes(true);
        return viewResolver;
    }
    /**
     * @ResponseBody转为json配置
     * @Author Linyb
     * @Date 2016/7/23 15:07
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(){
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        List<MediaType> types = new ArrayList<MediaType>();
        types.add(new MediaType("text","html", Charset.forName("utf-8")));
        types.add(new MediaType("text","json", Charset.forName("utf-8")));
        types.add(new MediaType("application","json", Charset.forName("utf-8")));
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(types);
        return  mappingJackson2HttpMessageConverter;
    }
    /**
     * @Responseb转为json配置
     * @Author Linyb
     * @Date 2016/7/23 15:07
     */
    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter(){
        RequestMappingHandlerAdapter requestMappingHandlerAdapter = new RequestMappingHandlerAdapter();
        List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
        list.add(mappingJackson2HttpMessageConverter());
        requestMappingHandlerAdapter.setMessageConverters(list);
        return requestMappingHandlerAdapter;
    }

    /**
     * 配置多文件上传
     * @Date  2016/9/1 10:44
     * @author linyb
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    @Bean
    AuthCheckInteceptor getAuthCheckInteceptor(){
        return new AuthCheckInteceptor();
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getAuthCheckInteceptor()).addPathPatterns("/**");
    }
}