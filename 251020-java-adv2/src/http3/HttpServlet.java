package http3;

import java.io.IOException;

public interface HttpServlet {
    void service(HttpRequest request, HttpResponse response) throws IOException;
}
