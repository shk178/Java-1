package hello;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;

import java.util.Set;

@HandlesTypes(AppInit.class)
public class ServletContainerInitializer1 implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        System.out.println(this.getClass().getSimpleName() + ".onStartup: set = " + set);
        System.out.println(this.getClass().getSimpleName() + ".onStartup: servletContext = " + servletContext);
        for (Class<?> aClass : set) {
            try {
                AppInit appInit = (AppInit) aClass.getDeclaredConstructor().newInstance();
                System.out.println("appInit.getClass().getSimpleName() = " + appInit.getClass().getSimpleName());
                appInit.onStartup(servletContext);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
/*
ServletContainerInitializer1.onStartup: set = [class hello.AppInitServlet1, class hello.AppInitSpring1]
ServletContainerInitializer1.onStartup: servletContext = org.apache.catalina.core.ApplicationContextFacade@7aa01bd9
appInit.getClass().getSimpleName() = AppInitServlet1
AppInitServlet1.onStartup: servletContext = org.apache.catalina.core.ApplicationContextFacade@7aa01bd9
appInit.getClass().getSimpleName() = AppInitSpring1
AppInitSpring1.onStartup: servletContext = org.apache.catalina.core.ApplicationContextFacade@7aa01bd9
 */