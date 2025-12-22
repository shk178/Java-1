package hello;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class WebApplicationInitializer1 implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        System.out.println(this.getClass().getSimpleName() + ".onStartup: servletContext = " + servletContext);
        addSpring(servletContext);
    }
    private void addSpring(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(SpringConfig1.class);
        DispatcherServlet dispatcher2 = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher2", dispatcher2);
        registration.addMapping("/sp2/*");
    }
}
/*
WebApplicationInitializer1.onStartup: servletContext = org.apache.catalina.core.ApplicationContextFacade@1fb2eec
 */