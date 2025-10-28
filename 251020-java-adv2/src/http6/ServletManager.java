package http6;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServletManager {
    private final Map<String, HttpServlet> servletMap = new HashMap<>();
    private HttpServlet defaultServlet;
    private HttpServlet notFoundErrorServlet = new NotFoundServlet();
    private HttpServlet internalErrorServlet = new InternalErrorServlet();
    public void add(String path, HttpServlet servlet) {
        servletMap.put(path, servlet);
    }
    public void setDefaultServlet(HttpServlet defaultServlet) {
        this.defaultServlet = defaultServlet;
    }

    public void setNotFoundErrorServlet(HttpServlet notFoundErrorServlet) {
        this.notFoundErrorServlet = notFoundErrorServlet;
    }
    public void setInternalErrorServlet(HttpServlet internalErrorServlet) {
        this.internalErrorServlet = internalErrorServlet;
    }
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        try {
            HttpServlet servlet = servletMap.getOrDefault(request.getPath(), defaultServlet);
            if (servlet == null) {
                throw new PageNotFoundException("없는 페이지"); // defaultServlet도 null일 때
            } else {
                servlet.service(request, response);
            }
        } catch (PageNotFoundException e) {
            notFoundErrorServlet.service(request, response);
        } catch (Exception e) {
            internalErrorServlet.service(request, response);
        }
    }
}
