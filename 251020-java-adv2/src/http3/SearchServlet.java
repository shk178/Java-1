package http3;

import java.io.IOException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SearchServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
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
