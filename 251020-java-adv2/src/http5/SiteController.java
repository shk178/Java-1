package http5;

import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SiteController {
    @Mapping("/")
    public void home(HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>home</h1>").append("\n");
        sb.append("<ul>").append("\n");
        sb.append("<li><a href='/site1'>site1</a></li>").append("\n");
        sb.append("<li><a href='/site2'>site2</a></li>").append("\n");
        sb.append("<li><form action='/search' method='GET'>");
        sb.append("<input type='text' name='q' placeholder='검색어'>");
        sb.append("<button type='submit'>검색</button>");
        sb.append("</form></li>").append("\n");
        sb.append("</ul>");
        String body = sb.toString();
        response.writeBody(body);
    }
    @Mapping("/site1")
    public void site1(HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>site1</h1>");
        String body = sb.toString();
        response.writeBody(body);
    }
    @Mapping("/site2")
    public void site2(HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>site2</h1>");
        String body = sb.toString();
        response.writeBody(body);
    }
    @Mapping("/search")
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
