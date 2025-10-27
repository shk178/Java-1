package http4;

import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReflectorController {
    public void site1(HttpRequest request, HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>site1</h1>");
        String body = sb.toString();
        response.writeBody(body);
    }
    public void site2(HttpRequest request, HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>site2</h1>");
        String body = sb.toString();
        response.writeBody(body);
    }
    public void search(HttpRequest request, HttpResponse response) {
        String query = request.getQueryParam("q"); // private void parseQueryParams(String query)에서 디코드되어 있다.
        String decode = URLDecoder.decode(query, UTF_8);
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>search</h1>").append("\n");
        sb.append("<ul>").append("\n");
        sb.append("<li>query: ").append(query).append("</li>").append("\n");
        sb.append("<li>decode: ").append(decode).append("</li>").append("\n");
        sb.append("</ul>");
        String body = sb.toString();
        response.writeBody(body);
    }
}
