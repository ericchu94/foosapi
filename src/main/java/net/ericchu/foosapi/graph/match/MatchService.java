package net.ericchu.foosapi.graph.match;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import org.immutables.mongo.concurrent.FluentFuture;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class MatchService {
    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
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

    public Publisher<Match> createMatch(String name) {
        ImmutableMatch match = ImmutableMatch.builder().name(name).build();
        return toMono(matchRepository.insert(match).transform(x -> match));
    }

    public Publisher<Match> deleteMatch(String id) {
        return toMono(matchRepository.findById(id).deleteFirst().transform(Optional::orNull)).single();
    }
}
