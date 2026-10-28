package be.dontletit.api;

/** A card to put in a collection: any known name (front face names work too); quantity defaults to 1. */
public record CollectionEntryRequest(String name, Integer quantity) {}
