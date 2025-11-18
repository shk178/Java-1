package lecture.demo2.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.web.controllers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/v23/*")
public class FrontControllerServletV23 extends HttpServlet {
    private final Map<String, Object> handlerMappingMap = new HashMap<>();
    private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>();
    public FrontControllerServletV23() {
        initHandlerMappingMap();
        initHandlerAdapters();
    }
    private void initHandlerMappingMap() {
        handlerMappingMap.put("/v23/add", new MemberAddControllerV2());
        handlerMappingMap.put("/v23/save", new MemberSaveControllerV2());
        handlerMappingMap.put("/v23/list", new MemberListControllerV3());
    }
    private void initHandlerAdapters() {
        handlerAdapters.add(new ControllerV2HandlerAdapter());
        handlerAdapters.add(new ControllerV3HandlerAdapter());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object handler = getHandler(req);
        if (handler == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        MyHandlerAdapter adapter = getHandlerAdapter(handler);
        if (adapter instanceof ControllerV2HandlerAdapter) {
            ModelView modelView = adapter.handle(req, resp, handler);
            MyView view = viewResolver(modelView.getViewName());
            view.render(modelView.getModel(), req, resp);
        } else if (adapter instanceof ControllerV3HandlerAdapter) {
            ModelView modelView = adapter.handle(req, resp, handler);
            MyView view = viewResolver(modelView.getViewName());
            view.render(modelView.getModel(), req, resp);
        }
    }
    private Object getHandler(HttpServletRequest req) {
        String reqURI = req.getRequestURI();
        return handlerMappingMap.get(reqURI);
    }
    private MyHandlerAdapter getHandlerAdapter(Object handler) {
        for (MyHandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler))
                return adapter;
        }
        throw new IllegalArgumentException();
    }
    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }
}
