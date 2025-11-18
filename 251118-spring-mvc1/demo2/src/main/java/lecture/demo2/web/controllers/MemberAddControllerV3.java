package lecture.demo2.web.controllers;

import java.util.Map;

public class MemberAddControllerV3 implements ControllerV3 {
    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        return "add";
    }
}
