package online.iwantagift.ui.web;

import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.web.interceptors.AuthPageRedirectInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final AuthPageRedirectInterceptor authPageRedirectInterceptor;
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/landing").setViewName("landing");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authPageRedirectInterceptor)
                .addPathPatterns("/auth/signin", "/auth/signup");
    }
}
