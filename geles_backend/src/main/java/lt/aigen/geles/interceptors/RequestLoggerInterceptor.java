package lt.aigen.geles.interceptors;

import lt.aigen.geles.models.RequestLog;
import lt.aigen.geles.repositories.ConfigurationRepository;
import lt.aigen.geles.repositories.RequestLogRepository;
import lt.aigen.geles.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RequestLoggerInterceptor implements HandlerInterceptor {
    @Autowired
    RequestLogRepository requestLogRepository;

    @Autowired
    ConfigurationRepository configurationRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public void afterCompletion(
        HttpServletRequest request, HttpServletResponse response,
        Object handler, Exception ex) {

        if(!(handler instanceof HandlerMethod)) {
            System.out.println("Request logger not invoked " + handler);
            return;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        String username = null;
        try {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("user")){
                    username = cookie.getValue();
                    break;
                }
            }
        } catch (NullPointerException e) {
        }

        if (username != null) {
            var user = userRepository.findByUsername(username);
            if(user.isEmpty()) {
                return;
            }
            requestLogRepository.save(new RequestLog(username,
                request.getRequestURL().toString(), request.getMethod(), handlerMethod.getMethod().getName(),
                    user.get().getIsAdmin()));
        }
    }
}
