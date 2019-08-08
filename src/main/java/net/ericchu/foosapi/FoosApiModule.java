package net.ericchu.foosapi;

import com.google.common.net.HttpHeaders;
import dagger.Module;
import dagger.Provides;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.servlet.GraphQLWebsocketServlet;
import graphql.servlet.config.GraphQLConfiguration;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.SetHeaderHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import net.ericchu.foosapi.graph.GraphQLModule;
import net.ericchu.foosapi.graph.game.GameModule;
import net.ericchu.foosapi.graph.match.MatchModule;
import net.ericchu.foosapi.graph.player.PlayerModule;
import net.ericchu.foosapi.graph.side.SideModule;

import javax.inject.Singleton;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

@Module(includes = { MatchModule.class, GameModule.class, SideModule.class, PlayerModule.class })
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
    static GraphQLWebsocketServlet graphQLWebsocketServlet(GraphQLConfiguration graphQLConfiguration) {
        return new GraphQLWebsocketServlet(graphQLConfiguration.getQueryInvoker(),
                graphQLConfiguration.getInvocationInputFactory(), graphQLConfiguration.getObjectMapper());
    }

    @Provides
    ServerEndpointConfig serverEndpointConfig(GraphQLWebsocketServlet graphQLWebsocketServlet) {
        return ServerEndpointConfig.Builder.create(GraphQLWebsocketServlet.class, "/graphql")
                .configurator(new ServerEndpointConfig.Configurator() {
                    @Override
                    public <T> T getEndpointInstance(Class<T> endpointClass) {
                        return (T) graphQLWebsocketServlet;
                    }

                    @Override
                    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request,
                            HandshakeResponse response) {
                        super.modifyHandshake(sec, request, response);
                        graphQLWebsocketServlet.modifyHandshake(sec, request, response);
                    }
                }).build();
    }

    @Provides
    static WebSocketDeploymentInfo webSocketDeploymentInfo(ServerEndpointConfig serverEndpointConfig) {
        return new WebSocketDeploymentInfo().addEndpoint(serverEndpointConfig);

    }

    @Provides
    static DeploymentInfo deploymentInfo(Collection<ServletInfo> servletInfos,
            GraphQLConfiguration graphQLConfiguration, WebSocketDeploymentInfo webSocketDeploymentInfo) {
        DeploymentInfo deploymentInfo = Servlets.deployment().setDeploymentName("FoosApi").setContextPath("/")
                .setClassLoader(FoosApiModule.class.getClassLoader()).addServlets(servletInfos)
                .addServletContextAttribute(GraphQLServlet.GRAPHQL_CONFIGURATION_ATTRIBUTE, graphQLConfiguration)
                .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSocketDeploymentInfo);
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
            return new SetHeaderHandler(
                    new SetHeaderHandler(manager.start(), HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"),
                    HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "content-type");
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    static Undertow undertow(HttpHandler httpHandler) {
        Undertow server = Undertow.builder().addHttpListener(8080, "localhost").setHandler(httpHandler).build();
        return server;
    }

    @Provides
    static GraphQLSchema graphQLSchema(Set<GraphQLModule> graphQLModules) {
        String schema = "type Query{} type Mutation {} type Subscription {}";

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
        for (GraphQLModule module : graphQLModules)
            typeDefinitionRegistry = typeDefinitionRegistry.merge(module.getTypeDefinitionRegistry());

        RuntimeWiring.Builder builder = newRuntimeWiring();

        graphQLModules.stream().flatMap(x -> x.getTypeRuntimeWirings().stream()).forEach(x -> builder.type(x));

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, builder.build());
    }

    @Provides
    @Singleton
    static GraphQLConfiguration graphQLConfiguration(GraphQLSchema graphQLSchema) {
        return GraphQLConfiguration.with(graphQLSchema).build();
    }
}
