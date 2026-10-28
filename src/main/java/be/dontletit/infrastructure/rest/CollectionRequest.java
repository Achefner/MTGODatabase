package be.dontletit.infrastructure.rest;

import be.dontletit.api.CollectionEntryRequest;

import java.util.List;

/** JSON body of POST /collections and PUT /collections/{id}; `type` is validated by the controller. */
public record CollectionRequest(String name, String type, List<CollectionEntryRequest> entries) {}
