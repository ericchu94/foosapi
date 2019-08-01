package net.ericchu.foosapi.graph;

import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;

import java.util.Collection;

public interface GraphQLModule {
    TypeDefinitionRegistry getTypeDefinitionRegistry();

    Collection<TypeRuntimeWiring> getTypeRuntimeWirings();
}
