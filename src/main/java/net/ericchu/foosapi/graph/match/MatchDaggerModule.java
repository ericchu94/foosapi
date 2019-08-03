package net.ericchu.foosapi.graph.match;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.BaseGraphQLModule;
import net.ericchu.foosapi.graph.GraphQLModule;
import org.immutables.mongo.repository.RepositorySetup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collection;

@Module
public class MatchDaggerModule {
    @Provides
    static RepositorySetup repositorySetup() {
        return RepositorySetup.forUri("mongodb://localhost/foosapi");
    }

    @Provides
    static MatchRepository matchRepository(RepositorySetup repositorySetup) {
        return new MatchRepository(repositorySetup);
    }

    @Provides
    static TypeDefinitionRegistry typeDefinitionRegistry() {
        try (InputStreamReader reader = new InputStreamReader(
                MatchDaggerModule.class.getResourceAsStream("match.graphql"))) {
            return new SchemaParser().parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    static Collection<TypeRuntimeWiring> typeRuntimeWirings(MatchTypeRuntimeWirings matchTypeRuntimeWirings) {
        return matchTypeRuntimeWirings.getTypeRuntimeWirings();
    }

    @Provides
    @IntoSet
    static GraphQLModule matchModule(TypeDefinitionRegistry typeDefinitionRegistry,
            Collection<TypeRuntimeWiring> typeRuntimeWirings) {
        return new BaseGraphQLModule(typeDefinitionRegistry, typeRuntimeWirings);
    }
}
