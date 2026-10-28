package be.dontletit.api;

import be.dontletit.domain.CollectionType;

import java.util.List;
import java.util.Optional;

public interface CollectionApiPort {

    /** @throws UnknownCardsException when an entry names a card that is not in the database
     *  @throws IllegalArgumentException on a blank name, missing type or non-positive quantity */
    CollectionResponse create(String name, CollectionType type, List<CollectionEntryRequest> entries);

    Optional<CollectionResponse> replace(String id, String name, CollectionType type, List<CollectionEntryRequest> entries);

    List<CollectionSummaryResponse> list();

    Optional<CollectionResponse> get(String id);

    /** The collection's cards expanded by quantity, in collection order. */
    Optional<List<CardResponse>> cards(String id);

    boolean delete(String id);
}
