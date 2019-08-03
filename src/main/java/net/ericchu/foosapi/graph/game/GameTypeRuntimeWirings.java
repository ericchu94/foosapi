package net.ericchu.foosapi.graph.game;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.match.Match;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameTypeRuntimeWirings {
    private final GameService gameService;

    @Inject
    public GameTypeRuntimeWirings(GameService gameService) {
        this.gameService = gameService;
    }

    public Collection<TypeRuntimeWiring> getTypeRuntimeWirings() {
        return List.of(typeRuntimeWiring("Match", "games", this::getGames));
    }

    private TypeRuntimeWiring typeRuntimeWiring(String type, String field, DataFetcher dataFetcher) {
        return TypeRuntimeWiring.newTypeWiring(type).dataFetcher(field, dataFetcher).build();
    }

    private <T> CompletableFuture<T> toFuture(Publisher<T> publisher) {
        return Mono.from(publisher).toFuture();
    }

    private CompletableFuture<? extends Collection<Game>> getGames(DataFetchingEnvironment environment) {
        Match match = environment.getSource();
        return toFuture(gameService.getGames(match.id()));
    }
}
