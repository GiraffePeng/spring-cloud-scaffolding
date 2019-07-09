package com.peng.projectservice.server.swagger;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Bean
    public Docket createRestApi() {
		ParameterBuilder ticketPar = new ParameterBuilder();
    	List<Parameter> pars = new ArrayList<>();
        Parameter build = ticketPar.name("Authorization").description("jwt的Token")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(false).build(); //header中的ticket参数非必填，传空也可以
        pars.add(build);  
		return new Docket(DocumentationType.SWAGGER_2)
				  .apiInfo(apiInfo()) // 配置说明
			      .select() // 选择那些路径和 api 会生成 document
			      .apis(RequestHandlerSelectors.basePackage("com.peng"))
	              .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
	              .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
	              .paths(PathSelectors.any())
			      .build().globalOperationParameters(pars); // 创建
			      
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("项目服务 Swagger API").description("spring cloud 脚手架")
                .termsOfServiceUrl("").version("1.0").build();
    }
}
