package com.codesquad.issueTracker.common.config;

import com.codesquad.issueTracker.common.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AuthInterceptorConfig implements WebMvcConfigurer {
    private final AuthInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/users/signup", "/api/users/signin","/api/users/refresh","/api/auth/**");
    }
}
