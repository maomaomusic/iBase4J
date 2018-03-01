package org.ibase4j.core.config;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

import top.ibase4j.core.filter.CsrfFilter;
import top.ibase4j.core.filter.TokenFilter;
import top.ibase4j.core.filter.XssFilter;
import top.ibase4j.core.interceptor.EventInterceptor;
import top.ibase4j.core.interceptor.LocaleInterceptor;
import top.ibase4j.core.interceptor.MaliciousRequestInterceptor;
import top.ibase4j.core.util.InstanceUtil;

@Configuration
@ComponentScan({"org.iteachs.web"})
@SuppressWarnings("deprecation")
public class WebConfig extends WebMvcConfigurerAdapter {
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> encodingFilterRegistration() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setForceEncoding(true);
        FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<CharacterEncodingFilter>(
            encodingFilter);
        registration.setName("encodingFilter");
        registration.addUrlPatterns("/*");
        registration.setAsyncSupported(true);
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilterRegistration() {
        FilterRegistrationBean<CsrfFilter> registration = new FilterRegistrationBean<CsrfFilter>(new CsrfFilter());
        registration.setName("csrfFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<XssFilter>(new XssFilter());
        registration.setName("xssFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(3);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TokenFilter> tokenFilterRegistration() {
        FilterRegistrationBean<TokenFilter> registration = new FilterRegistrationBean<TokenFilter>(new TokenFilter());
        registration.setName("tokenFilter");
        registration.addUrlPatterns("/app/*");
        registration.setOrder(4);
        return registration;
    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setViewClass(JstlView.class);
        return viewResolver;
    }

    @Bean
    public EventInterceptor eventInterceptor() {
        return new EventInterceptor();
    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/index.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        super.addViewControllers(registry);
    }

    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        List<MediaType> mediaTypes = InstanceUtil.newArrayList();
        mediaTypes.add(MediaType.valueOf("application/json;charset=UTF-8"));
        mediaTypes.add(MediaType.valueOf("text/html"));
        converter.setSupportedMediaTypes(mediaTypes);
        converter.setFeatures(SerializerFeature.QuoteFieldNames, SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNonStringValueAsString);
        converters.add(converter);
    }

    public void addInterceptors(InterceptorRegistry registry) {
        LocaleInterceptor localeInterceptor = new LocaleInterceptor();
        localeInterceptor.setNextInterceptor(eventInterceptor(), new MaliciousRequestInterceptor());
        registry.addInterceptor(localeInterceptor).addPathPatterns("/**").excludePathPatterns("/*.ico", "/*/api-docs",
            "/swagger**", "/swagger-resources/**", "/webjars/**", "/configuration/**");
    }

    // 资源重定向(仅作为后台使用不提供静态资源)
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("upload/**").addResourceLocations("/WEB-INF/upload/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
