package net.ericchu.foosapi;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.List;
import java.util.stream.Collectors;

class FoosApi {
    public static void main(String[] args) throws ServletException {
        List<Class<? extends Servlet>> servletClasses = List.of(GraphQLServlet.class);
        List<ServletInfo> servletInfos = servletClasses.stream()
                .map(servletClass -> {
                    WebServlet webServlet = servletClass.getAnnotation(WebServlet.class);
                    String name = webServlet.name();
                    String[] urlPatterns = webServlet.urlPatterns();
                    int loadOnStartup = webServlet.loadOnStartup();
                    return Servlets.servlet(name, servletClass)
                            .addMappings(urlPatterns)
                            .setLoadOnStartup(loadOnStartup);
                }).collect(Collectors.toList());

        DeploymentInfo servletBuilder = Servlets.deployment()
                .setDeploymentName("FoosApi")
                .setContextPath("/")
                .setClassLoader(FoosApi.class.getClassLoader())
                .addServlets(servletInfos);

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(manager.start())
                .build();
        server.start();
    }
}
