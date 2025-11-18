package lecture.demo2.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.web.controllers.ControllerV1;
import lecture.demo2.web.controllers.MemberAddControllerV1;
import lecture.demo2.web.controllers.MemberListControllerV1;
import lecture.demo2.web.controllers.MemberSaveControllerV1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/v1/*")
public class FrontControllerServletV1 extends HttpServlet {
    private Map<String, ControllerV1> controllerV1Map = new HashMap<>();
    public FrontControllerServletV1() {
        controllerV1Map.put("/v1/add", new MemberAddControllerV1());
        controllerV1Map.put("/v1/save", new MemberSaveControllerV1());
        controllerV1Map.put("/v1/list", new MemberListControllerV1());
    }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reqURI = req.getRequestURI();
        ControllerV1 controllerV1 = controllerV1Map.get(reqURI);
        if (controllerV1 == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        MyView view = controllerV1.process(req, resp);
        view.render(req, resp);
    }
}
