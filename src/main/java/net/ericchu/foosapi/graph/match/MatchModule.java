package net.ericchu.foosapi.graph.match;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.GraphQLModule;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

public class MatchModule implements GraphQLModule {
    @Override
    public TypeDefinitionRegistry getTypeDefinitionRegistry() {
        try (InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream("match.graphql"))) {
            return new SchemaParser().parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<TypeRuntimeWiring> getTypeRuntimeWirings() {
        return List.of(TypeRuntimeWiring.newTypeWiring("Query", builder -> builder.dataFetcher("matches", x -> new Match[] {new Match("id1", "name1")})));
    }
}
