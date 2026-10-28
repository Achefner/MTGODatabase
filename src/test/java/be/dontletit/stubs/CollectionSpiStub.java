package be.dontletit.stubs;

import be.dontletit.spi.CollectionData;
import be.dontletit.spi.CollectionSpiPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CollectionSpiStub implements CollectionSpiPort {

    public final Map<String, CollectionData> stored = new LinkedHashMap<>();

    @Override
    public CollectionData save(CollectionData collection) {
        stored.put(collection.id(), collection);
        return collection;
    }

    @Override
    public List<CollectionData> findAll() {
        return new ArrayList<>(stored.values());
    }

    @Override
    public Optional<CollectionData> findById(String id) {
        return Optional.ofNullable(stored.get(id));
    }

    @Override
    public boolean delete(String id) {
        return stored.remove(id) != null;
    }
}
