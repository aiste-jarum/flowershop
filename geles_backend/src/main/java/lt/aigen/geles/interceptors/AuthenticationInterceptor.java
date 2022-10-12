package lt.aigen.geles.interceptors;

import lt.aigen.geles.annotations.Authorized;
import lt.aigen.geles.components.CurrentUser;
import lt.aigen.geles.models.User;
import lt.aigen.geles.repositories.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    UserRepository userRepository;
    CurrentUser currentUser;

    public AuthenticationInterceptor(UserRepository userRepository, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean needToCheckAuth = false;
        boolean isAuthRequired = false;
        boolean needsAdmin = false;
        boolean isAuthorized;
        String username = null;
        User user = null;
        if(!(handler instanceof HandlerMethod)) return true;
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        try {
            Authorized authAnnotation = handlerMethod.getMethod().getAnnotation(Authorized.class);

            needsAdmin = authAnnotation.admin();
            isAuthRequired = !authAnnotation.optional();
            needToCheckAuth = true;

        } catch (NullPointerException ignored) { }

        if (!needToCheckAuth) {
            return true;
        }
        try {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("user")) {
                    username = cookie.getValue();
                    break;
                }
            }
        } catch (NullPointerException ignored) {
        }

        if (username == null) {
            isAuthorized = !isAuthRequired;
        } else {
            var optUser = userRepository.findByUsername(username);
            if (optUser.isEmpty()) {
                isAuthorized = false;
            } else {
                isAuthorized = !needsAdmin || optUser.get().getIsAdmin();
                user = optUser.get();
            }
        }
        if (isAuthorized) {
            currentUser.setCurrentUser(user);
            return true;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized user");
            return false;
        }
    }
}
