package cn.joinhealth.monitor.web.interceptor;


import cn.mw.monitor.common.constant.Constants;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yeshengqi on 2019/5/13.
 */
@Configuration
@EnableSwagger2
public class WebConfigurerAdapter extends WebMvcConfigurationSupport {

    @Value("${mwmonitor.accessControlAllowOrigin}")
    private List<String> accessControlAllowOrigins;
    @Value("${swagger.show}")
    private Boolean swaggerShow;

    @Value("${file.url}")
    private String urlPath;

    @Value("${basicUrl}")
    private String basicUrl;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration addInterceptor = registry.addInterceptor(globalInterceptor());
        addInterceptor.excludePathPatterns(Constants.PLUGINS_RESOURCE);
        addInterceptor.excludePathPatterns(Constants.APP_RESOURCE);
        addInterceptor.addPathPatterns("/**");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (this.swaggerShow) {
            registry.addResourceHandler(Constants.APP_RESOURCE).addResourceLocations("classpath:/static/app/");
            registry.addResourceHandler(Constants.PLUGINS_RESOURCE).addResourceLocations("classpath:/static/plugins/");
            registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
            registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
            registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
        registry.addResourceHandler("/mwapi/upload/**").addResourceLocations("file:" + new File(urlPath).getAbsolutePath() + File.separator);
        registry.addResourceHandler("/mwapi/basics/**").addResourceLocations("file:" + new File(basicUrl).getAbsolutePath() + File.separator);
        super.addResourceHandlers(registry);
    }

    @Bean
    public HttpMessageConverter responseBodyConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        return converter;
    }

    @Bean
    public GlobalInterceptor globalInterceptor() {
        GlobalInterceptor globalInterceptor = new GlobalInterceptor();
        globalInterceptor.setAccessControlAllowOrigins(accessControlAllowOrigins);
        return globalInterceptor;
    }


//    @Override
//
//    public void configureMessageConverters(List converters) {
//        super.configureMessageConverters(converters);
//        //解决中文乱码
//        converters.add(responseBodyConverter());
//    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        // 1、需要先定义一个 convert 转换消息的对象;
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();

        //2、添加fastJson 的配置信息，比如：是否要格式化返回的json数据;
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.getParserConfig().setSafeMode(true);
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastJsonConfig.setSerializerFeatures(
                // 防止循环引用
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat
        );
        //处理中文乱码
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);

        //3、在convert中添加配置信息.
        fastConverter.setFastJsonConfig(fastJsonConfig);

        HttpMessageConverter<?> converter = fastConverter;
        converters.add(fastConverter);

        //解决中文乱码
        converters.add(responseBodyConverter());

    }


    @Bean
    public Docket createRestApi() {

        ParameterBuilder xtoken = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<Parameter>();
        pars.add(xtoken.name("X-Token").description("token")//Token 以及Authorization 为自定义的参数，session保存的名字是哪个就可以写成那个
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(true).build());    //根据每个方法名也知道当前方法在设置什么参数

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //swagger要扫描的包
                .apis(RequestHandlerSelectors.basePackage("cn.mw.monitor"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(pars);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("猫维项目restfulApi")
                .description("猫维项目api")
                .termsOfServiceUrl("localhost:10081")
                .contact(new Contact("鲁明明", "localhost:port/doc.html", "1120564654@qq.com"))
                .version("1.0")
                .build();
    }

}
