package com.example.manus.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定一些接口不需要登录验证
            SaRouter.match("/**")
                   .notMatch("/auth/login", "/auth/register", "/favicon.ico", "/error")
                   .notMatch("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-resources/**")
                   .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
