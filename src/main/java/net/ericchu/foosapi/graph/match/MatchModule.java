package net.ericchu.foosapi.graph.match;

import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.Success;
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
    private final MongoCollection<Document> collection;

    public MatchModule(MongoCollection<Document> collection) {
        this.collection = collection;

        if (true) {
            Document d = new Document();
            d.put("_id", "custom id?");
            d.put("name", "d1");
            collection.insertOne(d).subscribe(new Subscriber<Success>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Success success) {

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onComplete() {

                }
            });
        }
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


                    CompletableFuture<Collection<Document>> future = new CompletableFuture<>();

                    collection.find().subscribe(new Subscriber<>() {
                        private List<Document> documents = new ArrayList<>();

                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(Document document) {
                           document.put("_id", document.get("_id").toString());
                            documents.add(document);
                        }

                        @Override
                        public void onError(Throwable t) {
                            future.completeExceptionally(t);
                        }

                        @Override
                        public void onComplete() {
                            future.complete(documents);
                        }
                    });

                    return future;
                }
        ))
                //        ,TypeRuntimeWiring.newTypeWiring("Match", builder -> builder.dataFetcher("_id", x -> ((Document)x.getSource()).get("_id").toString()))
        );
    }
}
