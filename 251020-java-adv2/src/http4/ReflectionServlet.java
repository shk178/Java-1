package http4;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectionServlet implements HttpServlet {
    private final List<Object> controllers;
    public ReflectionServlet(List<Object> controllers) {
        this.controllers = controllers;
    }
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        for (Object controller : controllers) {
            Class<?> aClass = controller.getClass();
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                if (path.equals("/" + method.getName())) {
                    invoke(controller, method, request, response);
                    return;
                }
            }
        }
        throw new PageNotFoundException("Not Found");
    }
    private static void invoke(Object controller, Method method, HttpRequest request, HttpResponse response) {
        try {
            method.invoke(controller, request, response);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
