package net.ericchu.foosapi.graph.side;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import org.immutables.mongo.concurrent.FluentFuture;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.Map;

public class SideService {
    private final SideRepository sideRepository;

    @Inject
    public SideService(SideRepository sideRepository) {
        this.sideRepository = sideRepository;
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

    public Publisher<? extends Side> getOrCreateSide(String gameId, Color color) {
        return toMono(sideRepository.find(sideRepository.criteria().gameId(gameId).color(color)).fetchFirst()
                .transform(Optional::orNull)).switchIfEmpty(Mono.from(createSide(gameId, color)));
    }

    public Publisher<? extends Side> createSide(String gameId, Color color) {
        Side side = ImmutableSide.builder().gameId(gameId).color(color).build();
        return toMono(sideRepository.insert(side).transform(x -> side));
    }

    public Mono<Side> updateSide(String id, Map<String, Object> fields) {
        SideRepository.Modifier modifier = sideRepository.findById(id).andModifyFirst().returningNew();

        // Work around no-ops
        if (fields.isEmpty())
            modifier.initPoints(0);

        if (fields.containsKey("points"))
            modifier.setPoints((int) fields.get("points"));

        return toMono(modifier.update().transform(Optional::orNull)).single();
    }
}
