package net.ericchu.foosapi;

import graphql.servlet.GraphQLHttpServlet;
import graphql.servlet.config.GraphQLConfiguration;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "GraphQLServlet", urlPatterns = { "graphql/*" }, loadOnStartup = 1)
public class GraphQLServlet extends GraphQLHttpServlet {
    public static final String GRAPHQL_CONFIGURATION_ATTRIBUTE = "graphQLConfiguration";

    @Override
    protected GraphQLConfiguration getConfiguration() {
        return (GraphQLConfiguration) getServletContext().getAttribute(GRAPHQL_CONFIGURATION_ATTRIBUTE);
    }
}
