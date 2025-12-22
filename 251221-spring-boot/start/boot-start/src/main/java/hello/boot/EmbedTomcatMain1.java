package hello.boot;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

public class EmbedTomcatMain1 {
    public static void main(String[] args) throws LifecycleException {
        System.out.println("EmbedTomcatMain1");

        Tomcat tomcat = new Tomcat();
        Connector connector = new Connector();
        connector.setPort(8080);
        tomcat.setConnector(connector);

        Context context = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
        tomcat.addServlet("", "servlet1", new Servlet1());
        context.addServletMappingDecoded("/tc", "servlet1");
        tomcat.start();
        tomcat.getServer().await();
    }
}
