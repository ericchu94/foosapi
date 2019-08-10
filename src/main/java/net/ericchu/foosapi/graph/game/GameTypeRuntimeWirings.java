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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GameTypeRuntimeWirings {
    private final GameService gameService;

    @Inject
    public GameTypeRuntimeWirings(GameService gameService) {
        this.gameService = gameService;
    }

    public Collection<TypeRuntimeWiring> getTypeRuntimeWirings() {
        return List.of(typeRuntimeWiring("Match", "games", this::getGames),
                typeRuntimeWiring("Mutation", "createGame", this::createGame),
                typeRuntimeWiring("Mutation", "updateGame", this::updateGame),
                typeRuntimeWiring("Mutation", "deleteGame", this::deleteGame),
                typeRuntimeWiring("Subscription", "matchGames", this::subscribeMatchGames));
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

    private CompletableFuture<GamePayload> createGame(DataFetchingEnvironment env) {
        Map<String, Object> input = env.getArgument("input");
        String matchId = (String) input.get("matchId");
        String name = (String) input.get("name");
        boolean swapped = (boolean) input.get("swapped");
        return toFuture(gameService.createGame(matchId, name, swapped)).handle((count, ex) -> {
            Optional<GameError> error = Optional.ofNullable(ex).map(GameError::of);
            return ImmutableGamePayload.builder().result(Optional.ofNullable(count)).error(error).build();
        });
    }

    public CompletableFuture<? extends GamePayload> updateGame(DataFetchingEnvironment env) {
        Map<String, Object> input = env.getArgument("input");
        String id = (String) input.get("id");
        Map<String, Object> fields = (Map<String, Object>) input.get("fields");
        return toFuture(gameService.updateGame(id, fields)).handle((match, ex) -> {
            Optional<GameError> error = Optional.ofNullable(ex).map(GameError::of);
            return ImmutableGamePayload.builder().result(Optional.ofNullable(match)).error(error).build();
        });
    }

    private Publisher<? extends Game> subscribeMatchGames(DataFetchingEnvironment env) {
        String matchId = env.getArgument("matchId");
        return gameService.subscribeMatchGames(matchId);
    }

    private CompletableFuture<? extends GamePayload> deleteGame(DataFetchingEnvironment env) {
        Map<String, Object> input = env.getArgument("input");
        String id = (String) input.get("id");
        return toFuture(gameService.deleteGame(id)).handle((game, ex) -> {
            Optional<GameError> error = Optional.ofNullable(ex).map(GameError::of);
            return ImmutableGamePayload.builder().result(Optional.ofNullable(game)).error(error).build();
        });
    }
}
