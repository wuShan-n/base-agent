// com/example/gateway/config/CorsConfig.java
package com.example.manus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("*"); // 允许所有HTTP方法
        config.addAllowedOriginPattern("*"); // 允许所有来源
        config.addAllowedHeader("*"); // 允许所有请求头
        config.setAllowCredentials(true); // 允许携带凭证（如Cookie）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config); // 对所有路径应用此配置

        return new CorsWebFilter(source);
    }
}
