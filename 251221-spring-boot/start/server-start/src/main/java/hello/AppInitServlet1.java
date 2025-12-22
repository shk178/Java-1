package hello;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

public class AppInitServlet1 implements AppInit {
    @Override
    public void onStartup(ServletContext servletContext) {
        System.out.println(this.getClass().getSimpleName() + ".onStartup: servletContext = " + servletContext);
        addServlet(servletContext);
    }
    private void addServlet(ServletContext servletContext) {
        ServletRegistration.Dynamic registration = servletContext.addServlet("servlet1", new Servlet1());
        registration.addMapping("/sv");
    }
}
