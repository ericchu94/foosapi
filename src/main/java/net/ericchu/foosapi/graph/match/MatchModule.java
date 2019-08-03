package net.ericchu.foosapi.graph.match;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.GraphQLModule;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

    private <T> CompletableFuture<T> toFuture(Publisher<T> publisher) {
        return Mono.from(publisher).toFuture();
    }

    @Override
    public Collection<TypeRuntimeWiring> getTypeRuntimeWirings() {
        return List.of(
                TypeRuntimeWiring.newTypeWiring("Query",
                        builder -> builder.dataFetcher("matches", env -> toFuture(matchService.getMatches()))
                                .dataFetcher("match", env -> toFuture(matchService.getMatch(env.getArgument("id"))))),
                TypeRuntimeWiring.newTypeWiring("Mutation", builder -> builder.dataFetcher("createMatch", env -> {
                    Map<String, String> input = env.getArgument("input");
                    return toFuture(matchService.createMatch(input.get("name"))).handle((match, ex) -> {
                        Optional<MatchError> error = Optional.ofNullable(ex).map(MatchError::of);
                        return ImmutableMatchPayload.builder().result(Optional.ofNullable(match)).error(error).build();
                    });
                }).dataFetcher("deleteMatch", env -> {
                    Map<String, String> input = env.getArgument("input");
                    return toFuture(matchService.deleteMatch(input.get("id"))).handle((match, ex) -> {
                        Optional<MatchError> error = Optional.ofNullable(ex).map(MatchError::of);
                        return ImmutableMatchPayload.builder().result(Optional.ofNullable(match)).error(error).build();
                    });
                }).dataFetcher("deleteMatches", env -> toFuture(matchService.deleteMatches()).handle((count, ex) -> {
                    Optional<MatchError> error = Optional.ofNullable(ex).map(MatchError::of);
                    return ImmutableMatchPayload.builder().result(Optional.ofNullable(count)).error(error).build();
                })).dataFetcher("updateMatch", env -> {
                    Map<String, Object> input = env.getArgument("input");
                    String id = (String) input.get("id");
                    Map<String, Object> fields = (Map<String, Object>) input.get("fields");
                    return toFuture(matchService.updateMatch(id, fields)).handle((match, ex) -> {
                        Optional<MatchError> error = Optional.ofNullable(ex).map(MatchError::of);
                        return ImmutableMatchPayload.builder().result(Optional.ofNullable(match)).error(error).build();
                    });
                })));
    }
}
