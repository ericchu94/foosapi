package net.ericchu.foosapi.graph.player;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import org.immutables.mongo.concurrent.FluentFuture;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerSideRepository playerSideRepository;

    @Inject
    public PlayerService(PlayerRepository playerRepository, PlayerSideRepository playerSideRepository) {
        this.playerRepository = playerRepository;
        this.playerSideRepository = playerSideRepository;
    }

    private <T> Mono<T> toMono(FluentFuture<T> future) {
        return Mono.create(sink -> future.addCallback(new FutureCallback<>() {
            @Override
            public void onSuccess(T result) {
                sink.success(result);
            }

            @Override
            public void onFailure(Throwable t) {
                sink.error(t);
            }
        }));
    }

    public Publisher<? extends Collection<Player>> getPlayers() {
        return toMono(playerRepository.findAll().fetchAll());
    }

    public Publisher<Player> createPlayer(String name) {
        Player player = ImmutablePlayer.builder().name(name).build();
        return toMono(playerRepository.insert(player).transform(x -> player));
    }

    public Publisher<Boolean> addPlayer(String sideId, String playerId) {
        PlayerSide playerSide = ImmutablePlayerSide.builder().sideId(sideId).playerId(playerId).build();
        return toMono(playerSideRepository.insert(playerSide).transform(x -> true));
    }

    public Publisher<? extends Collection<Player>> getSidePlayers(String sideId) {
        return toMono(playerSideRepository.find(playerSideRepository.criteria().sideId(sideId)).fetchAll())
                .flatMapIterable(Function.identity())
                .flatMap(x -> toMono(playerRepository.findById(x.playerId()).fetchFirst().transform(Optional::orNull)))
                .collect(Collectors.toList());
    }
}
