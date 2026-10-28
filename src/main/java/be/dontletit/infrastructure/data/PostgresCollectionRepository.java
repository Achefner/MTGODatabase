package be.dontletit.infrastructure.data;

import be.dontletit.infrastructure.data.entity.CollectionEntity;
import be.dontletit.infrastructure.data.entity.CollectionEntryEmbeddable;
import be.dontletit.spi.CollectionData;
import be.dontletit.spi.CollectionEntryData;
import be.dontletit.spi.CollectionSpiPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PostgresCollectionRepository implements CollectionSpiPort {

    @Inject
    EntityManager em;

    @Override
    @Transactional
    public CollectionData save(CollectionData collection) {
        CollectionEntity entity = em.find(CollectionEntity.class, collection.id());
        boolean isNew = entity == null;
        if (isNew) {
            entity = new CollectionEntity();
            entity.id = collection.id();
        }
        entity.name = collection.name();
        entity.type = collection.type();
        entity.entries.clear();
        for (CollectionEntryData entry : collection.entries()) {
            entity.entries.add(new CollectionEntryEmbeddable(entry.cardName(), entry.quantity()));
        }
        if (isNew) em.persist(entity);
        em.flush();
        return toData(entity);
    }

    @Override
    public List<CollectionData> findAll() {
        return em.createQuery("FROM CollectionEntity ORDER BY name", CollectionEntity.class)
                .getResultList().stream().map(PostgresCollectionRepository::toData).toList();
    }

    @Override
    public Optional<CollectionData> findById(String id) {
        return Optional.ofNullable(em.find(CollectionEntity.class, id)).map(PostgresCollectionRepository::toData);
    }

    @Override
    @Transactional
    public boolean delete(String id) {
        CollectionEntity entity = em.find(CollectionEntity.class, id);
        if (entity == null) return false;
        em.remove(entity);
        return true;
    }

    private static CollectionData toData(CollectionEntity entity) {
        List<CollectionEntryData> entries = new ArrayList<>();
        for (CollectionEntryEmbeddable entry : entity.entries) {
            entries.add(new CollectionEntryData(entry.cardName, entry.quantity));
        }
        return new CollectionData(entity.id, entity.name, entity.type, entries);
    }
}
