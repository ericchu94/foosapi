package net.ericchu.foosapi.graph.side;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.game.Game;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SideTypeRuntimeWirings {
    private final SideService sideService;

    @Inject
    public SideTypeRuntimeWirings(SideService sideService) {
        this.sideService = sideService;
    }

    public Collection<TypeRuntimeWiring> getTypeRuntimeWirings() {
        return List.of(typeRuntimeWiring("Game", "yellow", this::getYellowSide),
                typeRuntimeWiring("Game", "black", this::getBlackSide),
                typeRuntimeWiring("Mutation", "updateSide", this::updateSide));
    }

    private TypeRuntimeWiring typeRuntimeWiring(String type, String field, DataFetcher dataFetcher) {
        return TypeRuntimeWiring.newTypeWiring(type).dataFetcher(field, dataFetcher).build();
    }

    private <T> CompletableFuture<T> toFuture(Publisher<T> publisher) {
        return Mono.from(publisher).toFuture();
    }

    private CompletableFuture<? extends Side> getYellowSide(DataFetchingEnvironment environment) {
        return getSide(environment, Color.YELLOW);
    }

    private CompletableFuture<? extends Side> getBlackSide(DataFetchingEnvironment environment) {
        return getSide(environment, Color.BLACK);
    }

    private CompletableFuture<? extends Side> getSide(DataFetchingEnvironment environment, Color color) {
        Game game = environment.getSource();
        return toFuture(sideService.getOrCreateSide(game.id(), color));
    }

    public CompletableFuture<SidePayload> updateSide(DataFetchingEnvironment env) {
        Map<String, Object> input = env.getArgument("input");
        String id = (String) input.get("id");
        Map<String, Object> fields = (Map<String, Object>) input.get("fields");
        return toFuture(sideService.updateSide(id, fields)).handle((match, ex) -> {
            Optional<SideError> error = Optional.ofNullable(ex).map(SideError::of);
            return ImmutableSidePayload.builder().result(Optional.ofNullable(match)).error(error).build();
        });
    }
}
