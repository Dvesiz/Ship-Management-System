package com.dhy.shipmanagebackend.config;

import com.dhy.shipmanagebackend.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(loginInterceptor)
                // 拦截所有路径
                .addPathPatterns("/**")
                //  放行登录和注册接口 (一定要排除这俩，否则谁也进不去)
                .excludePathPatterns("/user/login", "/user/register",
                        "/user/loginByEmail",
                        "/user/send-code",
                        "/user/captcha",
                        "/user/reset-password");

    }
}