package net.ericchu.foosapi.graph.match;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.GraphQLModule;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

public class MatchModule implements GraphQLModule {
    private final MatchTypeRuntimeWirings matchTypeRuntimeWirings;

    @Inject
    public MatchModule(MatchTypeRuntimeWirings matchTypeRuntimeWirings) {
        this.matchTypeRuntimeWirings = matchTypeRuntimeWirings;
    }

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
        return matchTypeRuntimeWirings.getTypeRuntimeWirings();
    }
}
