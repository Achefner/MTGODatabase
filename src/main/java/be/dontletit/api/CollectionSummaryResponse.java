package be.dontletit.api;

import be.dontletit.domain.CollectionType;

public record CollectionSummaryResponse(String id, String name, CollectionType type, int cardCount) {}
