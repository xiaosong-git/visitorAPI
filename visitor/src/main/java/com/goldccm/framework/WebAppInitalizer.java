package com.goldccm.framework;

import com.goldccm.util.ParamDef;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.*;

/**
 * Java bean代替web.xml配置，继承AbstractAnnotationConfigDispatcherServletInitializer
 * @Date  2016/6/13 10:43
 * @Author linyb
 */
public class WebAppInitalizer extends AbstractAnnotationConfigDispatcherServletInitializer {
    //AbstractAnnotationConfigDispatcherServletInitializer这个类负责配置DispatcherServlet、初始化Spring MVC容器和Spring容器,LZ

    /**用于获取Spring应用容器的配置文件LZ
     *
     * 返回带有@Configuration注解的类
     * 用来配置ContextLoaderListener创建的应用上下文的bean
     * @Date  2016/6/13 11:54
     * @author linyb
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {

       // return new Class<?>[]{RootConfig.class};
        return null;
    }
    /**
     * getServletConfigClasses负责获取Spring MVC应用容器，这里传入预先定义好的WebConfig.class,LZ
     *
     * 返回带有@Configuration注解的类
     * 用来定义DispatcherServlet应用上下文的bean
     * @Date  2016/6/13 00111:53
     * @Author linyb
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {

        return new Class<?>[]{WebConfig.class};
    }
    /**
     * 将DispatcherServlet 映射到 /
     * @Date  2016/6/13 10:43
     * @Author linyb
     */
    @Override
    protected String[] getServletMappings() {

        return new String[]{"/"};
    }


    /**
     * 实现一些额外的配置，比如上传文件配置
     * @Date  2016/6/15 14:42
     * @author linyb
     */
    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        super.customizeRegistration(registration);
        //上传到的临时目录，最大不超过，整个请求不超过
//        registration.setMultipartConfig(new MultipartConfigElement("/java/temp",1024*1024*10,1024*1024*20,10000));
        registration.setMultipartConfig(new MultipartConfigElement(ParamDef.findDirByName("inOutDir"),1024*1024*10,1024*1024*20,0));
        /*registration.setLoadOnStartup(1);
        registration.setInitParameter("","");*/


    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);
        //增加过滤器，并且设置过滤的规则
        /*FilterRegistration.Dynamic filter = servletContext.addFilter("loginFilter",LoginFilter.class);
        filter.addMappingForUrlPatterns(null,false,"*//*");*/
        /**
         *         设置环境为dev还是prod
         */
        servletContext.setInitParameter(
                "spring.profiles.active", "dev");

        //servletContext.addListener();
        /*servletContext.addListener(ProjectStartInit.class);*/

    }

    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return new Filter[] {characterEncodingFilter};
    }

}