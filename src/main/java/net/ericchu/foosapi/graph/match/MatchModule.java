package net.ericchu.foosapi.graph.match;

import com.mongodb.reactivestreams.client.MongoCollection;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.GraphQLModule;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MatchModule implements GraphQLModule {
    private final MongoCollection<Match> collection;

    public MatchModule(MongoCollection<Match> collection) {
        this.collection = collection;
    }

    @Override
    public TypeDefinitionRegistry getTypeDefinitionRegistry() {
        try (InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream("match.graphql"))) {
            return new SchemaParser().parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<TypeRuntimeWiring> getTypeRuntimeWirings() {
        return List.of(TypeRuntimeWiring.newTypeWiring("Query", builder -> builder.dataFetcher("matches", x -> {
            CompletableFuture<Collection<Match>> future = new CompletableFuture<>();

            collection.find().subscribe(new Subscriber<>() {
                private List<Match> matches = new ArrayList<>();

                @Override
                public void onSubscribe(Subscription s) {
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Match match) {
                    matches.add(match);
                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    future.complete(matches);
                }
            });

            return future;
        })), TypeRuntimeWiring.newTypeWiring("Match",
                builder -> builder.dataFetcher("id", x -> ((Match) x.getSource()).getId().toString())));
    }
}
