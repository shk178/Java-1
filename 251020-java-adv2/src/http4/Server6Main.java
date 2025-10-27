package http4;

import java.io.IOException;
import java.util.List;

public class Server6Main {
    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        List<Object> controllers = List.of(new ReflectorController());
        HttpServlet reflectionServlet = new ReflectionServlet(controllers);
        ServletManager servletManager = new ServletManager();
        servletManager.setDefaultServlet(reflectionServlet);
        servletManager.add("/", new HomeServlet());
        servletManager.add("/favicon.ico", new DiscardServlet());
        HttpServer6 server = new HttpServer6(PORT, servletManager);
        server.start();
    }
}