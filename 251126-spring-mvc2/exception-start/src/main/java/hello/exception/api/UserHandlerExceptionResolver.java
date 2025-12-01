package hello.exception.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

public class UserHandlerExceptionResolver implements HandlerExceptionResolver {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            if (ex instanceof UserException) {
                String accept = request.getHeader("accept");
                response.setStatus(501);
                if(accept.contains("json")) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("ex-class", ex.getClass());
                    result.put("ex-message", ex.getMessage());
                    String resultJson = objectMapper.writeValueAsString(result);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(resultJson);
                    return new ModelAndView();
                } else {
                    return new ModelAndView("error/501");
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
/*
{
    "ex-message": "유저 오류",
    "ex-class": "hello.exception.api.UserException"
}
501
extendHandlerExceptionResolvers로 등록해야 적용된다.
 */