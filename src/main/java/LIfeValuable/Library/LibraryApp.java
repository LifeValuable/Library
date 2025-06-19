package LifeValuable.Library;

import LifeValuable.Library.config.WebConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;


public class LibraryApp {
    private static final int PORT = 8888;

    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.getConnector().setPort(PORT);

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(WebConfig.class);

        Context tomcatContext = tomcat.addContext("", new File(".").getAbsolutePath());
        context.setServletContext(tomcatContext.getServletContext());

        FilterDef securityFilterDef = new FilterDef();
        securityFilterDef.setFilterName("springSecurityFilterChain");
        securityFilterDef.setFilterClass("org.springframework.web.filter.DelegatingFilterProxy");
        tomcatContext.addFilterDef(securityFilterDef);

        FilterMap securityFilterMap = new FilterMap();
        securityFilterMap.setFilterName("springSecurityFilterChain");
        securityFilterMap.addURLPattern("/*");
        tomcatContext.addFilterMap(securityFilterMap);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
        Tomcat.addServlet(tomcatContext, "dispatcher", dispatcherServlet).setLoadOnStartup(1);
        tomcatContext.addServletMappingDecoded("/*", "dispatcher");

        tomcat.start();
        tomcat.getServer().await();
    }
}
