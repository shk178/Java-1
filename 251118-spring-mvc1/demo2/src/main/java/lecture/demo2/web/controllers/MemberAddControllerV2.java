package lecture.demo2.web.controllers;

import lecture.demo2.web.ModelView;

import java.util.Map;

public class MemberAddControllerV2 implements ControllerV2 {
    @Override
    public ModelView process(Map<String, String> paramMap) {
        return new ModelView("add");
    }
}
