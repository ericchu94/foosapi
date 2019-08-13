package net.ericchu.foosapi.graph.match;

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

public class MatchService {
    private final MatchRepository matchRepository;
    private final EventBus eventBus;
    private final Flux<Match> matches;

    @Inject
    public MatchService(MatchRepository matchRepository, EventBus eventBus) {
        this.matchRepository = matchRepository;
        this.eventBus = eventBus;

        this.matches = Flux.create(sink -> {
            eventBus.register(new Object() {
                @Subscribe
                public void subscribe(Match match) {
                    sink.next(match);
                }
            });
        });
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

    public Publisher<? extends Collection<Match>> getMatches() {
        return toMono(matchRepository.findAll().fetchAll());
    }

    public Publisher<Match> getMatch(String id) {
        return toMono(matchRepository.findById(id).fetchFirst().transform(Optional::orNull));
    }

    public Publisher<? extends Match> createMatch(String name) {
        ImmutableMatch match = ImmutableMatch.builder().name(name).build();
        return toMono(matchRepository.insert(match).transform(x -> match)).doOnSuccess(eventBus::post);
    }

    public Publisher<Match> deleteMatch(String id) {
        return toMono(matchRepository.findById(id).deleteFirst().transform(Optional::orNull)).single();
    }

    public Publisher<Integer> deleteMatches() {
        return toMono(matchRepository.findAll().deleteAll());
    }

    public Mono<Match> updateMatch(String id, Map<String, Object> fields) {
        MatchRepository.Modifier modifier = matchRepository.findById(id).andModifyFirst().returningNew();

        // Work around no-ops
        if (fields.isEmpty())
            modifier.initName("");

        if (fields.containsKey("name"))
            modifier.setName((String) fields.get("name"));

        return toMono(modifier.update().transform(Optional::orNull)).single().doOnSuccess(eventBus::post);
    }

    public Publisher<Match> subscribeMatch() {
        return matches;
    }
}
