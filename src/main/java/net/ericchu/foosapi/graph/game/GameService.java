package net.ericchu.foosapi.graph.game;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import org.immutables.mongo.concurrent.FluentFuture;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

public class GameService {
    private final GameRepository gameRepository;
    private final EventBus eventBus;

    @Inject
    public GameService(GameRepository gameRepository, EventBus eventBus) {
        this.gameRepository = gameRepository;
        this.eventBus = eventBus;
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

    public Publisher<Game> createGame(String matchId, String name, boolean swapped) {
        Game game = ImmutableGame.builder().matchId(matchId).name(name).swapped(swapped).build();
        return toMono(gameRepository.insert(game).transform(x -> game)).doOnSuccess(eventBus::post);
    }

    public Mono<Game> updateGame(String id, Map<String, Object> fields) {
        GameRepository.Modifier modifier = gameRepository.findById(id).andModifyFirst().returningNew();

        // Work around no-ops
        if (fields.isEmpty())
            modifier.initName("");

        if (fields.containsKey("name"))
            modifier.setName((String) fields.get("name"));

        if (fields.containsKey("swapped"))
            modifier.setSwapped((boolean) fields.get("swapped"));

        return toMono(modifier.update().transform(com.google.common.base.Optional::orNull)).single()
                .doOnSuccess(eventBus::post);
    }

    public Publisher<? extends Game> subscribeMatchGames(String matchId) {
        return Flux.<Game>create(sink -> {
            Object object = new Object() {
                @Subscribe
                public void Subscribe(Game game) {
                    sink.next(game);
                }
            };
            sink.onDispose(() -> eventBus.unregister(object));
            eventBus.register(object);
        }).filter(x -> x.matchId().equals(matchId));
    }

    public Publisher<? extends Game> deleteGame(String id) {
        return toMono(gameRepository.findById(id).deleteFirst().transform(Optional::orNull)).single();
    }
}
