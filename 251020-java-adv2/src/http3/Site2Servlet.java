package http3;

import java.io.IOException;

public class Site2Servlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>site2</h1>");
        String body = sb.toString();
        response.writeBody(body);
    }
}
