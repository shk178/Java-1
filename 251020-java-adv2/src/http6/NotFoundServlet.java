package http6;

public class NotFoundServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>404 페이지를 찾을 수 없습니다.</h1>");
        String body = sb.toString();
        response.writeBody(body);
        response.setStatusCode(404);
    }
}
