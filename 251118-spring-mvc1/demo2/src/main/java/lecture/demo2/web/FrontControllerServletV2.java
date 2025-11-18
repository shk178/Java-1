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

@WebServlet("/v2/*")
public class FrontControllerServletV2 extends HttpServlet {
    private Map<String, ControllerV2> controllerV2Map = new HashMap<>();
    public FrontControllerServletV2() {
        controllerV2Map.put("/v2/add", new MemberAddControllerV2());
        controllerV2Map.put("/v2/save", new MemberSaveControllerV2());
        controllerV2Map.put("/v2/list", new MemberListControllerV2());
    }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reqURI = req.getRequestURI();
        ControllerV2 controllerV2 = controllerV2Map.get(reqURI);
        if (controllerV2 == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Map<String, String> paramMap = createParamMap(req);
        ModelView modelView = controllerV2.process(paramMap);
        String viewName = modelView.getViewName();
        MyView view = viewResolver(viewName);
        view.render(modelView.getModel(), req, resp);
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
