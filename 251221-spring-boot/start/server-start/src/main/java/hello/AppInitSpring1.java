package hello;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class AppInitSpring1 implements AppInit {
    @Override
    public void onStartup(ServletContext servletContext) {
        System.out.println(this.getClass().getSimpleName() + ".onStartup: servletContext = " + servletContext);
        addSpring(servletContext);
    }
    private void addSpring(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(SpringConfig1.class);
        DispatcherServlet dispatcher1 = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher1", dispatcher1);
        registration.addMapping("/sp/*");
    }
}