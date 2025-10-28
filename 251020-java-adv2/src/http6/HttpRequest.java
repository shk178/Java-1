package http6;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpRequest {
    private String method;
    private String path;
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    public HttpRequest(BufferedReader reader) throws IOException {
        parseReqLine(reader);
        parseHeaders(reader);
        parseBody(reader);
    }
    private void parseReqLine(BufferedReader reader) throws IOException {
        String reqLine = reader.readLine(); // GET /search?keyword=c&lang=k HTTP/1.1
        if (reqLine == null) {
            throw new IOException("EOF: No request line received");
        }
        String[] splits = reqLine.split(" ");
        if (splits.length != 3) {
            throw new IOException("Invalid request line");
        }
        method = splits[0]; // GET
        String[] pathQuery = splits[1].split("\\?"); // /search (?가 구분자) keyword=c&lang=k
        // 정규식(Regular Expression)에서 ?는 특별한 의미(“앞의 문자가 0개나 1개 있을 때”)를 가지기 때문
        // 문자 ?를 기준으로 자르려면 백슬래시 2개로 escape 필요
        path = pathQuery[0];
        if (pathQuery.length > 1) {
            parseQueryParams(pathQuery[1]);
        }
    }
    private void parseQueryParams(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            String key = URLDecoder.decode(keyValue[0], UTF_8);
            String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], UTF_8) : "";
            queryParams.put(key, value);
        }
    }
    private void parseHeaders(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) { // 헤더 끝 빈 라인
                break;
            }
            String[] splits = line.split(":");
            headers.put(splits[0].trim(), splits[1].trim());
        }
    }
    private void parseBody(BufferedReader reader) throws IOException {
        if (!headers.containsKey("Content-Length")) {
            return;
        }
        int contentLength = Integer.parseInt(headers.get("Content-Length"));
        char[] bodyChars = new char[contentLength];
        int readLength = reader.read(bodyChars); // 바디 읽는다.
        if (readLength != contentLength) {
            throw new IOException("contentLength만큼 읽지 못함");
        } else {
            String body = new String(bodyChars);
            String contentType = headers.get("Content-Type");
            if ("application/x-www-form-urlencoded".equals(contentType)) {
                parseQueryParams(body);
            }
        }
    }
    public Map<String, String> getHeaders() {
        return headers;
    }
    public String getHeader(String key) {
        return headers.get(key);
    }
    public String getMethod() {
        return method;
    }
    public String getPath() {
        return path;
    }
    public Map<String, String> getQueryParams() {
        return queryParams;
    }
    public String getQueryParam(String key) {
        return queryParams.get(key);
    }
    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", queryParams=" + queryParams +
                ", headers=" + headers +
                '}';
    }
}
