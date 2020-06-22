package com.goldccm.framework;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @program: goldccm
 * @description: 只有在环境为dev的时候才能查看
 * localhost:8080/visitor/docs.html
 * @author: cwf
 * @create: 2020-04-16 21:22
 **/
@Profile("dev")//只在测试模式查看
@EnableSwagger2
@Configuration
public class Swagger2Config {
    /**
     *  配置swagger2文档
     * @author cwf
     * @date 2020/4/16 17:00
     */
    @Bean

    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 扫描的包
                .apis(RequestHandlerSelectors.basePackage("com.goldccm.controller"))
                // 选择API路径
                .paths(PathSelectors.any())
                .build();
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // swagger ui 的标题
                .title("SpringMvc中使用 Swagger2 构建 Restful APIs")
                // 描述
                .description("api文档")
                // 外链
                .termsOfServiceUrl("https://www.jianshu.com/u/008ce054774c")
                // 文档的版本信息
                .version("1.0")
                .build();
    }
}
