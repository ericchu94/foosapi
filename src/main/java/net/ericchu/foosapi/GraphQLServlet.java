package net.ericchu.foosapi;

import graphql.servlet.GraphQLHttpServlet;
import graphql.servlet.config.GraphQLConfiguration;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "GraphQLServlet", urlPatterns = { "graphql/*" }, loadOnStartup = 1)
public class GraphQLServlet extends GraphQLHttpServlet {
    @Override
    protected GraphQLConfiguration getConfiguration() {
        return (GraphQLConfiguration) getServletContext().getAttribute("graphQLConfiguration");
    }
}
