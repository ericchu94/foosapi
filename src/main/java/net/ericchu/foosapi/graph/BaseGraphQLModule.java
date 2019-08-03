package net.ericchu.foosapi.graph;

import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;

import javax.inject.Inject;
import java.util.Collection;

public class BaseGraphQLModule implements GraphQLModule {
    private final TypeDefinitionRegistry typeDefinitionRegistry;
    private final Collection<TypeRuntimeWiring> typeRuntimeWirings;

    public BaseGraphQLModule(TypeDefinitionRegistry typeDefinitionRegistry,
            Collection<TypeRuntimeWiring> typeRuntimeWirings) {
        this.typeDefinitionRegistry = typeDefinitionRegistry;
        this.typeRuntimeWirings = typeRuntimeWirings;
    }

    @Override
    public TypeDefinitionRegistry getTypeDefinitionRegistry() {
        return typeDefinitionRegistry;
    }

    @Override
    public Collection<TypeRuntimeWiring> getTypeRuntimeWirings() {
        return typeRuntimeWirings;
    }
}
