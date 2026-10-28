package be.dontletit.spi;

import java.util.List;
import java.util.Optional;

public interface CollectionSpiPort {
    /** Inserts, or replaces the collection with the same id. */
    CollectionData save(CollectionData collection);
    List<CollectionData> findAll();
    Optional<CollectionData> findById(String id);
    boolean delete(String id);
}
