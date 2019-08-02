package net.ericchu.foosapi.graph.match;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.GraphQLModule;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

public class MatchModule implements GraphQLModule {
    private final MatchService matchService;

    public MatchModule(MatchService matchService) {
        this.matchService = matchService;
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
        return List.of(TypeRuntimeWiring.newTypeWiring("Query",
                builder -> builder.dataFetcher("matches", env -> Mono.from(matchService.getMatches()).toFuture())
                        .dataFetcher("match",
                                env -> Mono.from(matchService.getMatch(env.getArgument("id"))).toFuture())));
    }
}
