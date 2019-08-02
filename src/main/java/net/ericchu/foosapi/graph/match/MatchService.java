package net.ericchu.foosapi.graph.match;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public class MatchService {
    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public Publisher<Collection<Match>> getMatches() {
        return Mono.create(sink -> matchRepository.findAll().fetchAll().addCallback(new FutureCallback<>() {
            @Override
            public void onSuccess(List<Match> result) {
                sink.success(result);
            }

            @Override
            public void onFailure(Throwable t) {
                sink.error(t);
            }
        }));
    }

    public Publisher<Match> getMatch(String id) {
        return Mono.create(sink -> matchRepository.findById(id).fetchFirst().addCallback(new FutureCallback<>() {
            @Override
            public void onSuccess(Optional<Match> result) {
                if (result.isPresent())
                    sink.success(result.get());
                else
                    sink.success();
            }

            @Override
            public void onFailure(Throwable t) {
                sink.error(t);
            }
        }));
    }
}
