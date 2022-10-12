package lt.aigen.geles.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(
        value = "interceptor.requestLogger.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class RequestLoggerInterceptorConfig implements WebMvcConfigurer {
    @Autowired
    RequestLoggerInterceptor requestLoggerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggerInterceptor).excludePathPatterns("/static/**");
    }
}
