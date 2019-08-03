package net.ericchu.foosapi.graph.game;

import com.google.common.util.concurrent.FutureCallback;
import org.immutables.mongo.concurrent.FluentFuture;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.Collection;

public class GameService {
    private final GameRepository gameRepository;

    @Inject
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
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

    public Publisher<? extends Collection<Game>> getGames(String matchId) {
        return toMono(gameRepository.find(gameRepository.criteria().matchId(matchId)).fetchAll());
    }
}
