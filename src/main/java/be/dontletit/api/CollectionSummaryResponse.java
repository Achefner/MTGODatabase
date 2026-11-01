package be.dontletit.api;


public record CollectionSummaryResponse(String id, String name, CollectionKind type, int cardCount) {}
