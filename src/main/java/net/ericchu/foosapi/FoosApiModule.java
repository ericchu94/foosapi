package net.ericchu.foosapi;

import dagger.Module;
import dagger.Provides;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import net.ericchu.foosapi.graph.match.MatchDaggerModule;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Module(includes = MatchDaggerModule.class)
public class FoosApiModule {
    @Provides
    static Collection<Class<? extends Servlet>> servletClasses() {
        List<Class<? extends Servlet>> servletClasses = List.of(GraphQLServlet.class);

        return servletClasses;
    }

    @Provides
    static Collection<ServletInfo> servletInfos(Collection<Class<? extends Servlet>> servletClasses) {
        List<ServletInfo> servletInfos = servletClasses.stream().map(servletClass -> {
            WebServlet webServlet = servletClass.getAnnotation(WebServlet.class);
            String name = webServlet.name();
            String[] urlPatterns = webServlet.urlPatterns();
            int loadOnStartup = webServlet.loadOnStartup();
            return Servlets.servlet(name, servletClass).addMappings(urlPatterns).setLoadOnStartup(loadOnStartup);
        }).collect(Collectors.toList());
        return servletInfos;
    }

    @Provides
    static DeploymentInfo deploymentInfo(Collection<ServletInfo> servletInfos) {
        DeploymentInfo deploymentInfo = Servlets.deployment().setDeploymentName("FoosApi").setContextPath("/")
                .setClassLoader(FoosApiModule.class.getClassLoader()).addServlets(servletInfos);
        return deploymentInfo;
    }

    @Provides
    static DeploymentManager deploymentManager(DeploymentInfo deploymentInfo) {
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();
        return manager;
    }

    @Provides
    static HttpHandler httpHandler(DeploymentManager manager) {
        try {
            return manager.start();
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    static Undertow undertow(HttpHandler httpHandler) {
        Undertow server = Undertow.builder().addHttpListener(8080, "localhost").setHandler(httpHandler).build();
        return server;
    }

}
