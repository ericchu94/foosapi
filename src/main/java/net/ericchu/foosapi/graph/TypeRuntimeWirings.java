package net.ericchu.foosapi.graph;

import graphql.schema.idl.TypeRuntimeWiring;

import java.util.Collection;

public interface TypeRuntimeWirings {
    Collection<TypeRuntimeWiring> getTypeRuntimeWirings();
}
