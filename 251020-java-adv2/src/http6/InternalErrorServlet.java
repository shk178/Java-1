package http6;

public class InternalErrorServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>500 Internal Error</h1>");
        String body = sb.toString();
        response.writeBody(body);
        response.setStatusCode(500);
    }
}
