package net.ericchu.foosapi.graph.match;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.TypeRuntimeWiring;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MatchTypeRuntimeWirings {
    private final MatchService matchService;

    @Inject
    public MatchTypeRuntimeWirings(MatchService matchService) {
        this.matchService = matchService;
    }

    public Collection<TypeRuntimeWiring> getTypeRuntimeWirings() {
        return List.of(typeRuntimeWiring("Query", "matches", this::getMatches),
                typeRuntimeWiring("Query", "match", this::getMatch),
                typeRuntimeWiring("Mutation", "createMatch", this::createMatch),
                typeRuntimeWiring("Mutation", "deleteMatch", this::deleteMatch),
                typeRuntimeWiring("Mutation", "deleteMatches", this::deleteMatches),
                typeRuntimeWiring("Mutation", "updateMatch", this::updateMatch));
    }

    private TypeRuntimeWiring typeRuntimeWiring(String type, String field, DataFetcher dataFetcher) {
        return TypeRuntimeWiring.newTypeWiring(type).dataFetcher(field, dataFetcher).build();
    }

    private <T> CompletableFuture<T> toFuture(Publisher<T> publisher) {
        return Mono.from(publisher).toFuture();
    }

    public CompletableFuture<? extends Collection<Match>> getMatches(DataFetchingEnvironment env) {
        return toFuture(matchService.getMatches());
    }

    public CompletableFuture<Match> getMatch(DataFetchingEnvironment env) {
        return toFuture(matchService.getMatch(env.getArgument("id")));
    }

    public CompletableFuture<ImmutableMatchPayload> createMatch(DataFetchingEnvironment env) {
        Map<String, String> input = env.getArgument("input");
        return toFuture(matchService.createMatch(input.get("name"))).handle((match, ex) -> {
            Optional<MatchError> error = Optional.ofNullable(ex).map(MatchError::of);
            return ImmutableMatchPayload.builder().result(Optional.ofNullable(match)).error(error).build();
        });
    }

    public CompletableFuture<ImmutableMatchPayload> deleteMatch(DataFetchingEnvironment env) {
        Map<String, String> input = env.getArgument("input");
        return toFuture(matchService.deleteMatch(input.get("id"))).handle((match, ex) -> {
            Optional<MatchError> error = Optional.ofNullable(ex).map(MatchError::of);
            return ImmutableMatchPayload.builder().result(Optional.ofNullable(match)).error(error).build();
        });
    }

    public CompletableFuture<ImmutableMatchPayload> deleteMatches(DataFetchingEnvironment env) {
        return toFuture(matchService.deleteMatches()).handle((count, ex) -> {
            Optional<MatchError> error = Optional.ofNullable(ex).map(MatchError::of);
            return ImmutableMatchPayload.builder().result(Optional.ofNullable(count)).error(error).build();
        });
    }

    public CompletableFuture<ImmutableMatchPayload> updateMatch(DataFetchingEnvironment env) {
        Map<String, Object> input = env.getArgument("input");
        String id = (String) input.get("id");
        Map<String, Object> fields = (Map<String, Object>) input.get("fields");
        return toFuture(matchService.updateMatch(id, fields)).handle((match, ex) -> {
            Optional<MatchError> error = Optional.ofNullable(ex).map(MatchError::of);
            return ImmutableMatchPayload.builder().result(Optional.ofNullable(match)).error(error).build();
        });
    }
}
