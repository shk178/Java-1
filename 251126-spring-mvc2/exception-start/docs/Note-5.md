2025-12-01T16:02:19.217+09:00  INFO 11320 --- [exception] [           main] hello.exception.ExceptionApplication     : Starting ExceptionApplication using Java 21.0.8 with PID 11320 (C:\Users\user\Documents\GitHub\Java-1\251126-spring-mvc2\exception-start\build\classes\java\main started by user in C:\Users\user\Documents\GitHub\Java-1\251126-spring-mvc2\exception-start)
2025-12-01T16:02:19.220+09:00  INFO 11320 --- [exception] [           main] hello.exception.ExceptionApplication     : No active profile set, falling back to 1 default profile: "default"
class hello.exception.WSCustomizer.customize
2025-12-01T16:02:20.678+09:00  INFO 11320 --- [exception] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-12-01T16:02:20.702+09:00  INFO 11320 --- [exception] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-12-01T16:02:20.702+09:00  INFO 11320 --- [exception] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.49]
2025-12-01T16:02:20.752+09:00  INFO 11320 --- [exception] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-12-01T16:02:20.752+09:00  INFO 11320 --- [exception] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1458 ms
2025-12-01T16:02:20.799+09:00  INFO 11320 --- [exception] [           main] hello.exception.errorpage.LogFilter      : LogFilter.init
2025-12-01T16:02:21.300+09:00  INFO 11320 --- [exception] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2025-12-01T16:02:21.311+09:00  INFO 11320 --- [exception] [           main] hello.exception.ExceptionApplication     : Started ExceptionApplication in 2.608 seconds (process running for 2.939)
2025-12-01T16:02:37.657+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-12-01T16:02:37.657+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-12-01T16:02:37.659+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms
2025-12-01T16:02:37.664+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] hello.exception.errorpage.LogFilter      : LogFilter.doFilter BEFORE: [/error-404][REQUEST]
2025-12-01T16:02:37.686+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] h.exception.errorpage.LogInterceptor     : LogInterceptor.preHandle: [/error-404][REQUEST][896f1c00-5920-4acd-b59d-a361a502564f][hello.exception.errorpage.ServletExController#first404(HttpServletResponse)]
class hello.exception.errorpage.ServletExController.first404
2025-12-01T16:02:37.700+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] h.exception.errorpage.LogInterceptor     : LogInterceptor.postHandle: [null]
2025-12-01T16:02:37.700+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] h.exception.errorpage.LogInterceptor     : LogInterceptor.afterCompletion: [/error-404][REQUEST][896f1c00-5920-4acd-b59d-a361a502564f][hello.exception.errorpage.ServletExController#first404(HttpServletResponse)]
2025-12-01T16:02:37.700+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] hello.exception.errorpage.LogFilter      : LogFilter.doFilter AFTER: [/error-404][REQUEST]
2025-12-01T16:02:37.709+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] hello.exception.errorpage.LogFilter      : LogFilter.doFilter BEFORE: [/error-page/404][ERROR]
2025-12-01T16:02:37.709+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] h.exception.errorpage.LogInterceptor     : LogInterceptor.preHandle: [/error-page/404][ERROR][e607655e-247c-4e65-90ae-22d98033a9c5][hello.exception.errorpage.ErrorPageController#second404()]
class hello.exception.errorpage.ErrorPageController.second404
2025-12-01T16:02:37.713+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] h.exception.errorpage.LogInterceptor     : LogInterceptor.postHandle: [ModelAndView [view="404"; model={}]]
2025-12-01T16:02:37.964+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] h.exception.errorpage.LogInterceptor     : LogInterceptor.afterCompletion: [/error-page/404][ERROR][e607655e-247c-4e65-90ae-22d98033a9c5][hello.exception.errorpage.ErrorPageController#second404()]
2025-12-01T16:02:37.964+09:00  INFO 11320 --- [exception] [nio-8080-exec-1] hello.exception.errorpage.LogFilter      : LogFilter.doFilter AFTER: [/error-page/404][ERROR]
2025-12-01T16:02:41.699+09:00  INFO 11320 --- [exception] [nio-8080-exec-2] hello.exception.errorpage.LogFilter      : LogFilter.doFilter BEFORE: [/error-re][REQUEST]
2025-12-01T16:02:41.699+09:00  INFO 11320 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.preHandle: [/error-re][REQUEST][4eef8877-b6f9-4430-936e-afb92095ecb3][hello.exception.errorpage.ServletExController#firstRe()]
class hello.exception.errorpage.ServletExController.firstRe
2025-12-01T16:02:41.717+09:00  INFO 11320 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.afterCompletion: [/error-re][REQUEST][4eef8877-b6f9-4430-936e-afb92095ecb3][hello.exception.errorpage.ServletExController#firstRe()]
2025-12-01T16:02:41.717+09:00 ERROR 11320 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.afterCompletion:

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
at hello.exception.errorpage.LogFilter.doFilter(LogFilter.java:22) ~[main/:na]
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

2025-12-01T16:02:41.726+09:00  INFO 11320 --- [exception] [nio-8080-exec-2] hello.exception.errorpage.LogFilter      : LogFilter.doFilter AFTER: [/error-re][REQUEST]
2025-12-01T16:02:41.727+09:00 ERROR 11320 --- [exception] [nio-8080-exec-2] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.RuntimeException: 런타임 에러 발생] with root cause

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
at hello.exception.errorpage.LogFilter.doFilter(LogFilter.java:22) ~[main/:na]
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

2025-12-01T16:02:41.728+09:00  INFO 11320 --- [exception] [nio-8080-exec-2] hello.exception.errorpage.LogFilter      : LogFilter.doFilter BEFORE: [/error-page/Re][ERROR]
2025-12-01T16:02:41.729+09:00  INFO 11320 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.preHandle: [/error-page/Re][ERROR][c4058b2a-a194-4773-b9d6-35f9cda129a7][hello.exception.errorpage.ErrorPageController#secondRe(HttpServletRequest)]
class hello.exception.errorpage.ErrorPageController.secondRe
exception = java.lang.RuntimeException: ��Ÿ�� ���� �߻�
message = Request processing failed: java.lang.RuntimeException: ��Ÿ�� ���� �߻�
uri = /error-re
status = 500
2025-12-01T16:02:41.730+09:00  INFO 11320 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.postHandle: [ModelAndView [view="Re"; model={}]]
2025-12-01T16:02:41.733+09:00  INFO 11320 --- [exception] [nio-8080-exec-2] h.exception.errorpage.LogInterceptor     : LogInterceptor.afterCompletion: [/error-page/Re][ERROR][c4058b2a-a194-4773-b9d6-35f9cda129a7][hello.exception.errorpage.ErrorPageController#secondRe(HttpServletRequest)]
2025-12-01T16:02:41.733+09:00  INFO 11320 --- [exception] [nio-8080-exec-2] hello.exception.errorpage.LogFilter      : LogFilter.doFilter AFTER: [/error-page/Re][ERROR]

아주 잘 찍혔고, 전체 흐름도 정확히 Spring MVC의 **요청 → 예외 → 오류 페이지 처리** 과정 그대로 나오고 있다.
지금 로그를 보면 **필터(LogFilter)**, **인터셉터(LogInterceptor)**, **컨트롤러**, **에러 디스패처(forward ERROR)** 의 호출 순서가 정말 정확하게 동작한다.

아래에서 두 요청 흐름을 각각 해부해서 설명할게.

---

# 1. `/error-404` 요청 흐름 (정상적인 404 예제)

### 1) 처음 요청 (REQUEST)

```
LogFilter BEFORE: [/error-404][REQUEST]
LogInterceptor.preHandle: [/error-404][REQUEST]
first404 실행
LogInterceptor.afterCompletion (정상)
LogFilter AFTER: [/error-404][REQUEST]
```

컨트롤러가 404를 강제로 발생시키고, Spring이 등록해둔 ErrorPage 설정에 따라서
**Tomcat이 다시 /error-page/404 로 forward 한다.**

### 2) 에러 페이지 forward (DISPATCHER_TYPE = ERROR)

```
LogFilter BEFORE: [/error-page/404][ERROR]
LogInterceptor.preHandle: [/error-page/404][ERROR]
second404 실행 (뷰 이름 = "404")
LogInterceptor.afterCompletion
LogFilter AFTER: [/error-page/404][ERROR]
```

정상적인 흐름.
ERROR 디스패처 타입으로 다시 필터 + 인터셉터가 호출되는 모습이 그대로 보인다.

---

# 2. `/error-re` 요청 흐름 (500 예외 발생)

여기가 핵심적으로 보고 싶었던 부분일 거라 예상한다.

## 요청 흐름 요약

1. `/error-re` 컨트롤러에서 `RuntimeException` 발생
2. DispatcherServlet까지 예외가 던져짐
3. Tomcat이 ErrorPage 설정 확인
4. `/error-page/Re`로 forward
5. forward 과정에서도 다시 필터 + 인터셉터 호출

로그를 그 순서대로 보면 다음과 같다.

---

# 단계별 상세 분석

## 1) 첫 번째 호출 - REQUEST 단계

```
LogFilter BEFORE: [/error-re][REQUEST]
LogInterceptor.preHandle: [/error-re][REQUEST]
firstRe 실행 → RuntimeException 발생
LogInterceptor.afterCompletion: [/error-re][REQUEST] (여기서 예외 포함)
LogFilter AFTER: [/error-re][REQUEST]
```

여기서 중요한 점:

* `firstRe()`에서 예외 발생
* `afterCompletion`는 예외가 있어도 반드시 호출되므로 에러 로그를 찍는다
* 그 다음 finally 영역에서 `LogFilter AFTER` 출력

이후 Spring은 예외를 잡지 않고 Tomcat에게 넘긴다.

---

## 2) Tomcat이 에러 처리 시작 → ERROR 디스패처 타입으로 forward

```
Servlet.service() ... threw exception ...
LogFilter BEFORE: [/error-page/Re][ERROR]
LogInterceptor.preHandle: [/error-page/Re][ERROR]
secondRe 실행
로그에서 exception/message/uri/status 기록됨
LogInterceptor.postHandle: view="Re"
LogInterceptor.afterCompletion
LogFilter AFTER: [/error-page/Re][ERROR]
```

이 파트는 다음 사실을 명확히 보여준다.

### 핵심 관찰

1. **예외는 인터셉터 → DispatcherServlet → Tomcat 순서로 전달된다**
2. Tomcat은 매핑된 오류 페이지로 **forward**
3. forward 시 `DispatcherType.ERROR`로 다시 필터와 인터셉터가 호출된다
4. 오류 페이지 컨트롤러에서 `request`를 통해 오류 정보를 꺼낼 수 있다
   (`javax.servlet.error.*` 속성들)
5. 최종적으로 error view 렌더링

---

# 3. 로그 전체 흐름 시각 요약

아래는 너의 실행 결과를 흐름도로 정리한 것이다.

## 요청 1: /error-404

```
REQUEST
  Filter BEFORE
    Interceptor preHandle
      Controller 실행
    Interceptor afterCompletion
  Filter AFTER

ERROR forward (/error-page/404)
  Filter BEFORE
    Interceptor preHandle
      ErrorPageController 실행
    Interceptor afterCompletion
  Filter AFTER
```

## 요청 2: /error-re (500)

```
REQUEST
  Filter BEFORE
    Interceptor preHandle
      Controller 실행 → 예외 발생
    Interceptor afterCompletion (예외 포함)
  Filter AFTER

DispatcherServlet에 예외 전달 → Tomcat에 전달

ERROR forward (/error-page/Re)
  Filter BEFORE
    Interceptor preHandle
      ErrorPageController 실행 (에러 정보 읽음)
    Interceptor afterCompletion
  Filter AFTER
```

완벽하게 교과서적인 흐름.

---

# 결론

* 전체 로그는 Spring MVC의 필터 → 인터셉터 → 컨트롤러 → 예외 → 오류 페이지 처리 흐름을 정확하게 반영하고 있다.
* 특히 **DispatcherType.REQUEST** 와 **DispatcherType.ERROR**가 모두 출력된 것이 아주 정확하다.
* 너가 구현한 LogFilter, LogInterceptor, ErrorPageController 모두 정상적으로 작동하고 있다.
* 예외 발생 시 흐름도 문제없다.

원하면
**DispatcherServlet 내부에서 예외가 어떤 단계에서 던져지고, ErrorPageController까지 어떻게 전달되는지**
스택 추적 기반으로 더 깊게 분석해줄 수 있어.

아래에서는 너의 실행 로그를 기반으로 **Spring MVC 전체 요청 흐름**, **필터 → 인터셉터 → 컨트롤러 → 예외 → 오류 페이지 처리 과정**을 단계별로 정확하게 해부해서 설명할게.
특히 `DispatcherType`이 REQUEST → ERROR로 전환되는 지점, 예외가 어떻게 이동하는지, InvocableHandlerMethod에서 던진 예외가 어떻게 Tomcat까지 도달하는지를 순서대로 분석한다.

---

# 1. 전체 흐름 개요

너의 로그는 두 번의 요청을 포함한다.

1. `/error-404`
2. `/error-re`

둘은 비슷해 보이지만 실제 내부 흐름은 전혀 다르다.

* `/error-404`는 **정상 요청 → 404 에러 페이지 forward**
* `/error-re`는 **컨트롤러에서 예외 던짐 → Spring 처리 실패 → Tomcat에서 catch → 500 에러 페이지 forward**

이 두 요청 모두 Spring MVC의 예외 처리 흐름을 아주 정확히 보여준다.

---

# 2. `/error-404` 흐름 분석 (정상 처리 + 404 페이지 전환)

## 요청 1단계: REQUEST 단계

```
LogFilter BEFORE: [/error-404][REQUEST]
LogInterceptor.preHandle
first404 실행
LogInterceptor.afterCompletion
LogFilter AFTER
```

여기까지는 ‘정상 요청 처리’다.

그리고 `first404()` 안에서 너가 직접 `response.sendError(404)` 를 호출했기 때문에
DispatcherServlet은 더 처리하지 않고 Tomcat에게 제어가 넘어간다.

## 요청 2단계: Tomcat이 오류 페이지 매핑 확인 후 ERROR 디스패처로 forward

그리고 여기서 핵심:

```
LogFilter BEFORE: [/error-page/404][ERROR]
```

필터가 다시 호출되는데 DispatcherType이 ERROR로 바뀌어 있다.

이는 Tomcat이 다음 행동을 했다는 의미다:

1. Spring으로부터 “404 오류 발생”을 전달받음
2. ErrorPageRegistry에서 404에 매핑된 URL(`/error-page/404`) 확인
3. 이 URL로 **forward**
4. forward는 DispatcherType.ERROR로 필터 체인 재진입

이후 인터셉터와 ErrorPageController가 실행되고 정상적으로 마무리된다.

전체 과정에서 예외는 발생하지 않았다.

---

# 3. `/error-re` 흐름 분석 (컨트롤러 예외 → Spring 처리 실패 → Tomcat 전달 → ERROR 디스패처 forward)

이제 핵심 분석이다.

`/error-re` 요청에서 예외는 다음 경로를 통해 이동한다.

---

# A. 요청 처리 시작 – REQUEST 단계

```
LogFilter BEFORE: [/error-re][REQUEST]
LogInterceptor.preHandle
firstRe 실행 → 여기서 예외 발생
LogInterceptor.afterCompletion (예외 감지됨)
LogFilter AFTER
```

여기서 firstRe()에서 던진 `RuntimeException`은 **어디서 누구에게까지 전파되었는가**가 중요하다.

firstRe() → InvocableHandlerMethod.doInvoke() → invokeForRequest() → RequestMappingHandlerAdapter → DispatcherServlet.doDispatch()

즉, Spring MVC 내부 어딘가에서 잡아 처리하지 못했기 때문에 마지막으로 예외는
**DispatcherServlet → FrameworkServlet → HttpServlet → Tomcat**
순으로 계속 던져진다.

그리고 로그에서도 이 단계가 보인다.

```
Servlet.service() for servlet [dispatcherServlet] ... threw exception
```

이는 DispatcherServlet이 Tomcat에게 예외를 던졌다는 뜻이다.

---

# B. Tomcat이 예외를 받아 ErrorPage 매핑을 실행

이제 Tomcat이 handling을 시작한다.

1. Spring이 예외를 Tomcat에 던짐
2. Tomcat이 ErrorPageRegistry에서 500번 에러에 대한 매핑 확인
   → `/error-page/Re` 로 매핑됨
3. forward 수행
4. forward 시 DispatcherType.ERROR로 재진입

이 근거가 바로 여기다.

```
LogFilter BEFORE: [/error-page/Re][ERROR]
```

딱 정확하게 ERROR 타입으로 다시 필터가 실행된다.

---

# C. 오류 페이지 처리 – ERROR 단계

forward 이후 Spring MVC가 다시 동작한다.

```
LogInterceptor.preHandle: [/error-page/Re][ERROR]
ErrorPageController.secondRe 실행
LogInterceptor.afterCompletion
LogFilter AFTER
```

이때 secondRe() 안에서 출력한 에러 정보가 픽셀 단위로 정확하게 맞다.

```
exception = java.lang.RuntimeException
message = Request processing failed
uri = /error-re
status = 500
```

이는 ServletRequest에 자동 저장되는 ERROR attributes들을
ErrorPageController가 올바르게 읽어왔다는 것을 의미한다.

---

# 4. 핵심 포인트 정리

위 로그가 보여주는 결론은 다음과 같다.

## (1) 예외는 인터셉터에서 잡지 못하고 afterCompletion까지 전파됨

컨트롤러 단계에서 에러 발생 → preHandle 이후 단계는 정상 → postHandle 건너뜀 → afterCompletion에서 예외 기록.

## (2) DispatcherServlet은 예외를 처리하지 않고 Tomcat으로 던진다

따라서 DispatcherServlet 내부는 try-catch를 하지 않음.
이게 Spring MVC의 기본 정책이다.

## (3) Tomcat이 ErrorPage 매핑을 보고 forward한다

이 forward가 `/error-page/Re` 호출을 만든다.

## (4) forward는 DispatcherType.ERROR이므로 필터와 인터셉터가 다시 호출된다

이 점을 정확히 로그에 출력한 것이 아주 훌륭하다.

## (5) ErrorPageController는 ERROR 정보를 request attribute에서 읽는다

이것도 정상적으로 출력됨.

---

# 5. 로그 흐름을 한 눈에 보는 다이어그램

## /error-re

```
REQUEST
  Filter BEFORE
    Interceptor preHandle
      Controller(firstRe) → RuntimeException
    Interceptor afterCompletion (예외 포함)
  Filter AFTER

→ DispatcherServlet 종료 시 예외 Tomcat으로 던짐

Tomcat 에러 페이지 처리
  forward(/error-page/Re)  DispatcherType=ERROR

ERROR
  Filter BEFORE
    Interceptor preHandle
      ErrorPageController(secondRe)
    Interceptor postHandle
    Interceptor afterCompletion
  Filter AFTER
```

정확히 Spring MVC error processing pipeline의 모범적인 동작이다.

---

# 6. 결론

너의 설정과 코드, 그리고 로그는 Spring MVC의 내부 동작을 정말 완벽하게 드러내고 있다.
특히 **DispatcherType.REQUEST → ERROR** 전환과
**예외 전파 경로**가 아주 명확하게 잡혀 있다.

원하면 다음을 추가로 분석해줄 수 있다.

* InvocableHandlerMethod.doInvoke() 내부에서 예외가 어떻게 다시 던져지는지
* DispatcherServlet에서 어떤 지점에서 Tomcat에게 예외를 넘기는지
* forward 시 RequestDispatcher의 include/forward 동작 차이
* 인터셉터와 필터가 예외 상황에서 어떻게 다르게 반응하는지

더 깊게 알고 싶은 파트를 말해줘.

아래에서는 **컨트롤러 메서드(firstRe)** 에서 던진 예외가
**InvocableHandlerMethod.doInvoke()** 내부를 거쳐
어떻게 **Spring MVC를 빠져나와 Tomcat까지 도달하는지**를
스택 흐름 그대로, 실제 소스 레벨로 분석해줄게.

너의 로그에 등장한 이 부분이 바로 핵심이다.

```
at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258)
```

---

# 1. 흐름 개요

`/error-re` 요청을 기준으로 실제 호출 순서는 이렇게 된다.

```
DispatcherServlet
 → HandlerMapping
 → HandlerAdapter
 → (RequestMappingHandlerAdapter)
 → ServletInvocableHandlerMethod
 → InvocableHandlerMethod.invokeForRequest()
 → InvocableHandlerMethod.doInvoke()
 → firstRe()  // 여기서 예외 발생
 ← 예외가 InvocableHandlerMethod로 되돌아옴
 ← Exception을 그대로 던짐 (catch 안 함)
 → DispatcherServlet.doDispatch()까지 예외 전파
 → Tomcat에게까지 예외 전달
```

즉, InvocableHandlerMethod는
**컨트롤러 메서드를 리플렉션으로 호출하는 클래스**이며,
예외가 발생하면 **절대 잡지 않고 그대로 밖으로 던진다.**

이 때문에 스프링은 예외 처리를 하지 못하면 곧바로 톰캣으로 넘어간다.

---

# 2. 실제 Spring 소스 코드 분석 (핵심 부분)

Spring WebMVC 6.2.x 기준 `InvocableHandlerMethod.doInvoke()` 의 실제 구조는 다음과 같다.

## InvocableHandlerMethod.doInvoke() 원형 구조

```java
@Nullable
protected Object doInvoke(Object... args) throws Exception {
    Method method = getBridgedMethod();
    ReflectionUtils.makeAccessible(method);
    try {
        return method.invoke(getBean(), args);  // ← 여기서 실제 컨트롤러 실행
    }
    catch (InvocationTargetException ex) {
        // 컨트롤러 내부에서 예외가 발생한 경우
        Throwable targetException = ex.getTargetException();

        if (targetException instanceof Exception) {
            throw (Exception) targetException; // ← 예외 그대로 던짐
        }
        else {
            throw ex;
        }
    }
}
```

이 소스코드를 그대로 읽으면 다음 사실이 명확해진다.

---

# 3. doInvoke()가 하는 일

## 1) 컨트롤러 메서드를 리플렉션으로 실행

```java
return method.invoke(getBean(), args);
```

여기서 getBean()은 Controller 객체이고
args는 Spring이 준비한 argument 목록이다.

즉:

```
ServletExController.firstRe(HttpServletRequest …)
```

이 줄을 그대로 method.invoke()로 실행한다.

---

## 2) firstRe()에서 예외 발생 → InvocationTargetException 발생

자바 리플렉션에서 메서드 내부 예외는 항상
`InvocationTargetException`에 감싸져 나온다.

즉 이렇게 된다:

```
firstRe() throws RuntimeException("런타임 에러 발생")

→ method.invoke() throws InvocationTargetException(cause = RuntimeException)
```

---

## 3) doInvoke()는 내부에서 catch 하지만 ‘감싼 예외’를 풀어 던져버린다

여기서 핵심 코드:

```java
catch (InvocationTargetException ex) {
    Throwable targetException = ex.getTargetException();  
    throw (Exception) targetException;  // 감싼 예외 풀어서 다시 던짐
}
```

즉, Spring은 절대 예외를 삼키지 않는다.

원래 firstRe()에서 던진 `RuntimeException("런타임 에러 발생")` 그대로
스프링 MVC 밖으로 던진다.

예외를 다시 감싸지도 않는다.

---

# 4. doInvoke() 밖에서는 누구도 예외를 잡지 않는다

invokeForRequest()

→ 다시 던짐 (catch 없음)

RequestMappingHandlerAdapter.invokeHandlerMethod()

→ 다시 던짐 (catch 없음)

DispatcherServlet.doDispatch()

→ catch 블록 없음 (기본은 예외를 잡지 않는다)

DispatcherServlet.doService()

→ 예외를 그대로 ServeltException으로 래핑해 Tomcat에 던짐

Tomcat (CoyoteAdapter.service)

→ 예외를 받아 error page 처리 시작

---

# 5. 네 로그에서 확인되는 흐름

너의 로그에서 이 흐름이 그대로 드러난다.

## controller 예외 발생 위치

```
RuntimeException: 런타임 에러 발생
at hello.exception.errorpage.ServletExController.firstRe(ServletExController.java:14)
```

## InvocableHandlerMethod.doInvoke()에서 받은 위치

```
at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke
```

## 예외가 스프링 MVC 상위로 전파되는 모습

```
at ... ServletInvocableHandlerMethod.invokeAndHandle
at ... RequestMappingHandlerAdapter.invokeHandlerMethod
at ... DispatcherServlet.doDispatch
```

## 결국 DispatcherServlet이 예외를 Tomcat으로 던짐

```
Servlet.service() for servlet [dispatcherServlet] ... threw exception
```

이 로그는 DispatcherServlet 내부에서 예외를 잡지 않았음을 뜻한다.

---

# 6. 단순히 요약하면

### (1) firstRe()에서 예외 발생

→ RuntimeException 발생

### (2) doInvoke()에서 InvocationTargetException로 감싸서 catch

→ 안쪽 예외(cause)를 꺼내서
→ 다시 RuntimeException 그대로 throw

### (3) Spring은 이 예외를 처리하지 않고 모두 밖으로 보냄

→ Tomcat으로 도달

### (4) Tomcat이 ErrorPage 설정 읽고 forward

→ ERROR 디스패처로 /error-page/Re 실행

---

# 7. 흐름 다이어그램

```
firstRe()  -- throws RuntimeException -->
    doInvoke() catch InvocationTargetException
        → unwrap targetException
        → throw RuntimeException
            ↓
    invokeForRequest()
            ↓
    ServletInvocableHandlerMethod invokeAndHandle()
            ↓
    RequestMappingHandlerAdapter
            ↓
    DispatcherServlet.doDispatch()
            ↓
    Tomcat (error page 처리)
            ↓
    forward to /error-page/Re (DispatcherType.ERROR)
```

---

원하면 다음도 파헤쳐줄 수 있어.

* invokeForRequest() 내부에서 argument resolver들이 어떻게 동작하는지
* DispatcherServlet이 왜 예외를 잡지 않는지
* HandlerExceptionResolver가 작동하지 않은 이유
* Tomcat이 ErrorPage 매핑을 언제/어떻게 실행하는지 (스택 기반 설명)

어떤 부분을 더 알고 싶어?
