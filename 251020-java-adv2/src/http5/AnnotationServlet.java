package http5;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationServlet implements HttpServlet {
    private final Map<String, ControllerMethod> pathMap;
    private static class ControllerMethod {
        private final Object controller;
        private final Method method;
        public ControllerMethod(Object controller, Method method) {
            this.controller = controller;
            this.method = method;
        }
        public void invoke(HttpRequest request, HttpResponse response) {
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i] == HttpRequest.class) {
                    args[i] = request;
                } else if (paramTypes[i] == HttpResponse.class) {
                    args[i] = response;
                } else {
                    throw new IllegalArgumentException("Unsupported param type");
                }
            }
            try {
                method.invoke(controller, args);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public AnnotationServlet(List<Object> controllers) {
        this.pathMap = new HashMap<>();
        initPathMap(controllers);
    }
    private void initPathMap(List<Object> controllers) {
        for (Object controller : controllers) {
            Method[] methods = controller.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Mapping.class)) {
                    String path = method.getAnnotation(Mapping.class).value();
                    if (pathMap.containsKey(path)) {
                        throw new IllegalArgumentException("이미 등록된 메서드");
                    } else {
                        pathMap.put(path, new ControllerMethod(controller, method));
                    }
                }
            }
        }
    }
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();
        ControllerMethod controllerMethod = pathMap.get(path);
        if (controllerMethod == null) {
            throw new PageNotFoundException("Not Found");
        } else {
            controllerMethod.invoke(request, response);
        }
    }
}
