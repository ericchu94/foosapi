package net.ericchu.foosapi;

import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.servlet.GraphQLHttpServlet;
import graphql.servlet.config.GraphQLConfiguration;
import net.ericchu.foosapi.graph.GraphQLModule;

import javax.servlet.annotation.WebServlet;
import java.util.Collection;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

@WebServlet(name = "GraphQLServlet", urlPatterns = { "graphql/*" }, loadOnStartup = 1)
public class GraphQLServlet extends GraphQLHttpServlet {
    private final Collection<GraphQLModule> graphQLModules = DaggerFoosApi.create().graphQLModules();

    @Override
    protected GraphQLConfiguration getConfiguration() {
        return GraphQLConfiguration.with(createSchema()).build();
    }

    private GraphQLSchema createSchema() {
        String schema = "type Query{} type Mutation {}";

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
        for (GraphQLModule module : graphQLModules)
            typeDefinitionRegistry = typeDefinitionRegistry.merge(module.getTypeDefinitionRegistry());

        RuntimeWiring.Builder builder = newRuntimeWiring();

        graphQLModules.stream().flatMap(x -> x.getTypeRuntimeWirings().stream()).forEach(x -> builder.type(x));

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, builder.build());
    }
}
