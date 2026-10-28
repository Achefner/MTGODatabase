package be.dontletit.api;

import be.dontletit.domain.CollectionType;

import java.util.List;

public record CollectionResponse(String id, String name, CollectionType type, int cardCount, List<CollectionEntryResponse> entries) {}
