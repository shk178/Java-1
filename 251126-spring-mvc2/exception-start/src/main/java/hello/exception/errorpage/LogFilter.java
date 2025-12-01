package hello.exception.errorpage;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LogFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("LogFilter.init");
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String reqURI = request.getRequestURI();
        try {
            log.info("LogFilter.doFilter BEFORE: [{}][{}]", reqURI, request.getDispatcherType());
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            throw e;
        } finally {
            log.info("LogFilter.doFilter AFTER: [{}][{}]", reqURI, request.getDispatcherType());
        }
    }
    @Override
    public void destroy() {
        log.info("LogFilter.destroy");
    }
}
