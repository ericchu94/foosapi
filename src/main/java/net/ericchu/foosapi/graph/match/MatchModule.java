package net.ericchu.foosapi.graph.match;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.BaseGraphQLModule;
import net.ericchu.foosapi.graph.GraphQLModule;
import net.ericchu.foosapi.mongo.MongoModule;
import org.immutables.mongo.repository.RepositorySetup;

import javax.inject.Named;
import javax.inject.Qualifier;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

@Module(includes = MongoModule.class)
public class MatchModule {
    @Provides
    static MatchRepository matchRepository(RepositorySetup repositorySetup) {
        return new MatchRepository(repositorySetup);
    }

    @Provides
    @Named("matchTypeDefinitionRegistry")
    static TypeDefinitionRegistry matchTypeDefinitionRegistry() {
        try (InputStreamReader reader = new InputStreamReader(MatchModule.class.getResourceAsStream("match.graphql"))) {
            return new SchemaParser().parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Named("matchTypeRuntimeWirings")
    static Collection<TypeRuntimeWiring> matchTypeRuntimeWirings(MatchTypeRuntimeWirings matchTypeRuntimeWirings) {
        return matchTypeRuntimeWirings.getTypeRuntimeWirings();
    }

    @Provides
    @IntoSet
    static GraphQLModule matchModule(
            @Named("matchTypeDefinitionRegistry") TypeDefinitionRegistry matchTypeDefinitionRegistry,
            @Named("matchTypeRuntimeWirings") Collection<TypeRuntimeWiring> matchTypeRuntimeWirings) {
        return new BaseGraphQLModule(matchTypeDefinitionRegistry, matchTypeRuntimeWirings);
    }
}
