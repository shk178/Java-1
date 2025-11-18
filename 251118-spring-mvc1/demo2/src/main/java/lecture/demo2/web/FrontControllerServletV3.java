package lecture.demo2.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.web.controllers.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/v3/*")
public class FrontControllerServletV3 extends HttpServlet {
    private Map<String, ControllerV3> controllerV3Map = new HashMap<>();
    public FrontControllerServletV3() {
        controllerV3Map.put("/v3/add", new MemberAddControllerV3());
        controllerV3Map.put("/v3/save", new MemberSaveControllerV3());
        controllerV3Map.put("/v3/list", new MemberListControllerV3());
    }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reqURI = req.getRequestURI();
        ControllerV3 controllerV3 = controllerV3Map.get(reqURI);
        if (controllerV3 == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Map<String, String> paramMap = createParamMap(req);
        Map<String, Object> model = new HashMap<>();
        String viewName = controllerV3.process(paramMap, model);
        MyView view = viewResolver(viewName);
        view.render(model, req, resp);
    }
    private Map<String, String> createParamMap(HttpServletRequest req) {
        Map<String, String> paramMap = new HashMap<>();
        req.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, req.getParameter(paramName)));
        return paramMap;
    }
    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }
}
