package hello.boot;

import hello.SpringConfig1;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class EmbedTomcatMain2 {
    public static void main(String[] args) throws LifecycleException {
        System.out.println("EmbedTomcatMain2");

        Tomcat tomcat = new Tomcat();
        Connector connector = new Connector();
        connector.setPort(8080);
        tomcat.setConnector(connector);

        AnnotationConfigWebApplicationContext contextA = new AnnotationConfigWebApplicationContext();
        contextA.register(SpringConfig1.class);
        DispatcherServlet dispatcher = new DispatcherServlet(contextA);
        Context context = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
        tomcat.addServlet("", "dispatcher", dispatcher);
        context.addServletMappingDecoded("/tc/*", "dispatcher");
        tomcat.start();
        tomcat.getServer().await();
    }
}
