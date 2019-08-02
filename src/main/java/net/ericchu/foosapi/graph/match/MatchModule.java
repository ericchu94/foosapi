package net.ericchu.foosapi.graph.match;

import com.google.common.util.concurrent.FutureCallback;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.GraphQLModule;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MatchModule implements GraphQLModule {
    private final MatchRepository matchRepository;

    public MatchModule(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
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
            CompletableFuture<Collection<Match>> completableFuture = new CompletableFuture<>();

            matchRepository.findAll().fetchAll().addCallback(new FutureCallback<>() {
                @Override
                public void onSuccess(List<Match> result) {
                    completableFuture.complete(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    completableFuture.completeExceptionally(t);
                }
            });

            return completableFuture;
        })), TypeRuntimeWiring.newTypeWiring("Match",
                builder -> builder.dataFetcher("id", x -> String.valueOf(((Match) x.getSource()).id()))));
    }
}
