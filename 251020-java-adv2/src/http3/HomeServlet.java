package http3;

import java.io.IOException;

public class HomeServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
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
}
