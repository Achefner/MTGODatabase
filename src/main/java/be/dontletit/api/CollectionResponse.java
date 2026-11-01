package be.dontletit.api;


import java.util.List;

public record CollectionResponse(String id, String name, CollectionKind type, int cardCount, List<CollectionEntryResponse> entries) {}
