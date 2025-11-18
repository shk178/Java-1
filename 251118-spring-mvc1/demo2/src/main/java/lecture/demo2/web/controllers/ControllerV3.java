package lecture.demo2.web.controllers;

import java.util.Map;

public interface ControllerV3 {
    String process(Map<String, String> paramMap, Map<String, Object> model);
}
