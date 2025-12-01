package hello.exception.errorpage;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorPageController {
    @RequestMapping("/error-page/Re")
    public String secondRe(HttpServletRequest request) {
        System.out.println(this.getClass() + ".secondRe");
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        System.out.println("exception = " + exception);
        System.out.println("message = " + message);
        System.out.println("uri = " + uri);
        System.out.println("status = " + status);
        return "Re";
        /*
java.lang.RuntimeException: 런타임 에러 발생
	at hello.exception.errorpage.ServletExController.firstRe(ServletExController.java:14) ~[main/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-6.2.14.jar:6.2.14]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:191) ~[spring-web-6.2.14.jar:6.2.14]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.2.14.jar:6.2.14]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:991) ~[spring-webmvc-6.2.14.jar:6.2.14]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:896) ~[spring-webmvc-6.2.14.jar:6.2.14]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.2.14.jar:6.2.14]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089) ~[spring-webmvc-6.2.14.jar:6.2.14]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979) ~[spring-webmvc-6.2.14.jar:6.2.14]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014) ~[spring-webmvc-6.2.14.jar:6.2.14]
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903) ~[spring-webmvc-6.2.14.jar:6.2.14]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564) ~[tomcat-embed-core-10.1.49.jar:6.0]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.2.14.jar:6.2.14]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.49.jar:6.0]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51) ~[tomcat-embed-websocket-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-6.2.14.jar:6.2.14]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.14.jar:6.2.14]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-6.2.14.jar:6.2.14]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.14.jar:6.2.14]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-6.2.14.jar:6.2.14]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.14.jar:6.2.14]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:88) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:342) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:399) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1774) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:973) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:491) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63) ~[tomcat-embed-core-10.1.49.jar:10.1.49]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

class hello.exception.errorpage.ErrorPageController.secondRe
exception = java.lang.RuntimeException: ��Ÿ�� ���� �߻�
message = Request processing failed: java.lang.RuntimeException: ��Ÿ�� ���� �߻�
uri = /error-re
status = 500
class hello.exception.errorpage.ErrorPageController.second404
         */
    }
    @RequestMapping("/error-page/404")
    public String second404() {
        System.out.println(this.getClass() + ".second404");
        return "404";
        /*
2025-12-01T15:51:31.862+09:00  INFO 6880 --- [exception] [nio-8080-exec-1] h.exception.errorpage.LogInterceptor     : LogInterceptor.postHandle: [ModelAndView [view="Re"; model={}]]
2025-12-01T15:51:32.052+09:00  INFO 6880 --- [exception] [nio-8080-exec-1] h.exception.errorpage.LogInterceptor     : LogInterceptor.afterCompletion: [/error-page/Re][ERROR][a55100ca-84be-4ea9-b29e-5e0ca7addb62][hello.exception.errorpage.ErrorPageController#secondRe(HttpServletRequest)]
2025-12-01T15:51:32.052+09:00  INFO 6880 --- [exception] [nio-8080-exec-1] hello.exception.errorpage.LogFilter      : LogFilter.doFilter AFTER: [/error-page/Re][ERROR]
2025-12-01T15:51:49.313+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] hello.exception.errorpage.LogFilter      : LogFilter.doFilter BEFORE: [/error-404][REQUEST]
2025-12-01T15:51:49.314+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.preHandle: [/error-404][REQUEST][a70463c5-2f4a-49ad-aaf1-8e3c1521c5e9][hello.exception.errorpage.ServletExController#first404(HttpServletResponse)]
class hello.exception.errorpage.ServletExController.first404
2025-12-01T15:51:49.315+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.postHandle: [null]
2025-12-01T15:51:49.315+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.afterCompletion: [/error-404][REQUEST][a70463c5-2f4a-49ad-aaf1-8e3c1521c5e9][hello.exception.errorpage.ServletExController#first404(HttpServletResponse)]
2025-12-01T15:51:49.315+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] hello.exception.errorpage.LogFilter      : LogFilter.doFilter AFTER: [/error-404][REQUEST]
2025-12-01T15:51:49.317+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] hello.exception.errorpage.LogFilter      : LogFilter.doFilter BEFORE: [/error-page/404][ERROR]
2025-12-01T15:51:49.317+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.preHandle: [/error-page/404][ERROR][527a5994-8355-4b20-8411-20fa6fd61c1c][hello.exception.errorpage.ErrorPageController#second404()]
class hello.exception.errorpage.ErrorPageController.second404
2025-12-01T15:51:49.318+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.postHandle: [ModelAndView [view="404"; model={}]]
2025-12-01T15:51:49.321+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.afterCompletion: [/error-page/404][ERROR][527a5994-8355-4b20-8411-20fa6fd61c1c][hello.exception.errorpage.ErrorPageController#second404()]
2025-12-01T15:51:49.321+09:00  INFO 6880 --- [exception] [nio-8080-exec-2] hello.exception.errorpage.LogFilter      : LogFilter.doFilter AFTER: [/error-page/404][ERROR]
         */
    }
}
