package net.ericchu.foosapi.graph.player;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.match.Match;
import net.ericchu.foosapi.graph.side.Side;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlayerTypeRuntimeWirings {
    private final PlayerService playerService;

    @Inject
    public PlayerTypeRuntimeWirings(PlayerService playerService) {
        this.playerService = playerService;
    }

    public Collection<TypeRuntimeWiring> getTypeRuntimeWirings() {
        return List.of(typeRuntimeWiring("Query", "players", this::getPlayers),
                typeRuntimeWiring("Side", "players", this::getSidePlayers),
                typeRuntimeWiring("Match", "top", this::getTopPlayers),
                typeRuntimeWiring("Match", "bottom", this::getBottomPlayers),
                typeRuntimeWiring("Mutation", "createPlayer", this::createPlayer),
                typeRuntimeWiring("Mutation", "addPlayer", this::addPlayer),
                typeRuntimeWiring("Mutation", "addPlayerMatch", this::addPlayerMatch),
                typeRuntimeWiring("Mutation", "deletePlayer", this::deletePlayer));
    }

    private TypeRuntimeWiring typeRuntimeWiring(String type, String field, DataFetcher dataFetcher) {
        return TypeRuntimeWiring.newTypeWiring(type).dataFetcher(field, dataFetcher).build();
    }

    private <T> CompletableFuture<T> toFuture(Publisher<T> publisher) {
        return Mono.from(publisher).toFuture();
    }

    private CompletableFuture<? extends Collection<Player>> getPlayers(DataFetchingEnvironment env) {
        return toFuture(playerService.getPlayers());
    }

    private CompletableFuture<PlayerPayload> createPlayer(DataFetchingEnvironment env) {
        Map<String, Object> input = env.getArgument("input");
        String name = (String) input.get("name");
        return toFuture(playerService.createPlayer(name)).handle((count, ex) -> {
            Optional<PlayerError> error = Optional.ofNullable(ex).map(PlayerError::of);
            return ImmutablePlayerPayload.builder().result(Optional.ofNullable(count)).error(error).build();
        });
    }

    private CompletableFuture<PlayerPayload> addPlayer(DataFetchingEnvironment env) {
        Map<String, Object> input = env.getArgument("input");
        String playerId = (String) input.get("playerId");
        String sideId = (String) input.get("sideId");
        return toFuture(playerService.addPlayer(sideId, playerId)).handle((count, ex) -> {
            Optional<PlayerError> error = Optional.ofNullable(ex).map(PlayerError::of);
            return ImmutablePlayerPayload.builder().result(Optional.ofNullable(count)).error(error).build();
        });
    }

    private CompletableFuture<? extends Collection<Player>> getSidePlayers(DataFetchingEnvironment env) {
        Side side = env.getSource();
        return toFuture(playerService.getSidePlayers(side.id()));
    }

    private CompletableFuture<? extends Collection<? extends Player>> getTopPlayers(DataFetchingEnvironment env) {
        Match match = env.getSource();
        return toFuture(playerService.getMatchPlayers(match.id(), Spot.TOP));
    }

    private CompletableFuture<? extends Collection<? extends Player>> getBottomPlayers(DataFetchingEnvironment env) {
        Match match = env.getSource();
        return toFuture(playerService.getMatchPlayers(match.id(), Spot.BOTTOM));
    }

    private CompletableFuture<? extends PlayerPayload> addPlayerMatch(DataFetchingEnvironment env) {
        Map<String, Object> input = env.getArgument("input");
        String playerId = (String) input.get("playerId");
        String matchId = (String) input.get("matchId");
        Spot spot = Spot.valueOf((String) input.get("spot"));
        return toFuture(playerService.addPlayerMatch(matchId, playerId, spot)).handle((count, ex) -> {
            Optional<PlayerError> error = Optional.ofNullable(ex).map(PlayerError::of);
            return ImmutablePlayerPayload.builder().result(Optional.ofNullable(count)).error(error).build();
        });
    }

    private CompletableFuture<? extends PlayerPayload> deletePlayer(DataFetchingEnvironment env) {
        Map<String, Object> input = env.getArgument("input");
        String playerId = (String) input.get("playerId");
        return toFuture(playerService.deletePlayer(playerId)).handle((player, ex) -> {
            Optional<PlayerError> error = Optional.ofNullable(ex).map(PlayerError::of);
            return ImmutablePlayerPayload.builder().result(Optional.ofNullable(player)).error(error).build();
        });
    }
}
