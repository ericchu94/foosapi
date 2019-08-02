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
import net.ericchu.foosapi.graph.match.MatchModule;

import javax.servlet.annotation.WebServlet;

import java.util.Collection;
import java.util.List;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

@WebServlet(name = "GraphQLServlet", urlPatterns = {"graphql/*"}, loadOnStartup = 1)
public class GraphQLServlet extends GraphQLHttpServlet {
    @Override
    protected GraphQLConfiguration getConfiguration() {
        return GraphQLConfiguration.with(createSchema()).build();
    }

    private GraphQLSchema createSchema() {
        String schema = "type Query{hello: String}";

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
        for (GraphQLModule module : getModules())
            typeDefinitionRegistry = typeDefinitionRegistry.merge(module.getTypeDefinitionRegistry());

        RuntimeWiring.Builder builder = newRuntimeWiring();

        getModules().stream().flatMap(x -> x.getTypeRuntimeWirings().stream()).forEach(x -> builder.type(x));

        builder.type("Query", x -> x.dataFetcher("hello", new StaticDataFetcher("world")))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, builder.build());
    }

    private Collection<GraphQLModule> getModules() {
        return List.of(new MatchModule());
    }
}
