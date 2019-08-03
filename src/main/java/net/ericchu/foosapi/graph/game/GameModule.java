package net.ericchu.foosapi.graph.game;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

@Module(includes = MongoModule.class)
public class GameModule {
    @Provides
    static GameRepository GameRepository(RepositorySetup repositorySetup) {
        return new GameRepository(repositorySetup);
    }

    @Provides
    @Named("gameTypeDefinitionRegistry")
    static TypeDefinitionRegistry gameTypeDefinitionRegistry() {
        try (InputStreamReader reader = new InputStreamReader(GameModule.class.getResourceAsStream("game.graphql"))) {
            return new SchemaParser().parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Named("gameTypeRuntimeWirings")
    static Collection<TypeRuntimeWiring> gameTypeRuntimeWirings(GameTypeRuntimeWirings gameTypeRuntimeWirings) {
        return gameTypeRuntimeWirings.getTypeRuntimeWirings();
    }

    @Provides
    @IntoSet
    static GraphQLModule gameModule(
            @Named("gameTypeDefinitionRegistry") TypeDefinitionRegistry gameTypeDefinitionRegistry,
            @Named("gameTypeRuntimeWirings") Collection<TypeRuntimeWiring> gameTypeRuntimeWirings) {
        return new BaseGraphQLModule(gameTypeDefinitionRegistry, gameTypeRuntimeWirings);
    }
}
