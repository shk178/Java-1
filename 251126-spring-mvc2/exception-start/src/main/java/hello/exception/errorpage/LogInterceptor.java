package hello.exception.errorpage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {
    public static final String LOG_ID = "logId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String reqURI = request.getRequestURI();
        String uuid = UUID.randomUUID().toString();
        request.setAttribute(LOG_ID, uuid);
        log.info("LogInterceptor.preHandle: [{}][{}][{}][{}]",
                reqURI, request.getDispatcherType(), uuid, handler);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("LogInterceptor.postHandle: [{}]", modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String reqURI = request.getRequestURI();
        String uuid = (String) request.getAttribute(LOG_ID);
        log.info("LogInterceptor.afterCompletion: [{}][{}][{}][{}]",
                reqURI, request.getDispatcherType(), uuid, handler);
        if (ex != null) {
            log.error("LogInterceptor.afterCompletion: ", ex);
        }
    }
}
