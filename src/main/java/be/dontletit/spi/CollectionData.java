package be.dontletit.spi;

import be.dontletit.domain.CollectionType;

import java.util.List;

public record CollectionData(String id, String name, CollectionType type, List<CollectionEntryData> entries) {}
