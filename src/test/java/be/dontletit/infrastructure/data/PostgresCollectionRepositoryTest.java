package be.dontletit.infrastructure.data;

import be.dontletit.domain.CollectionType;
import be.dontletit.spi.CollectionData;
import be.dontletit.spi.CollectionEntryData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PostgresCollectionRepositoryTest {

    @Inject
    PostgresCollectionRepository repository;

    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    void cleanUp() {
        em.createQuery("DELETE FROM CollectionEntity").executeUpdate();
    }

    private static CollectionData cube(String id, String name) {
        return new CollectionData(id, name, CollectionType.CUBE, List.of(
                new CollectionEntryData("Black Lotus", 1),
                new CollectionEntryData("Lightning Bolt", 4),
                new CollectionEntryData("Ancestral Recall", 1)));
    }

    @Test
    @Transactional
    void save_thenFindById_keepsEntriesInOrderWithQuantities() {
        repository.save(cube("c1", "Power LSV Cube"));

        Optional<CollectionData> found = repository.findById("c1");

        assertTrue(found.isPresent());
        assertEquals("Power LSV Cube", found.get().name());
        assertEquals(CollectionType.CUBE, found.get().type());
        assertEquals(List.of("Black Lotus", "Lightning Bolt", "Ancestral Recall"),
                found.get().entries().stream().map(CollectionEntryData::cardName).toList());
        assertEquals(4, found.get().entries().get(1).quantity());
    }

    @Test
    @Transactional
    void save_existingId_replacesTheCollection() {
        repository.save(cube("c1", "Old"));
        repository.save(new CollectionData("c1", "New", CollectionType.DECK, List.of(new CollectionEntryData("Serra Angel", 2))));

        CollectionData found = repository.findById("c1").orElseThrow();

        assertEquals("New", found.name());
        assertEquals(CollectionType.DECK, found.type());
        assertEquals(1, found.entries().size());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    @Transactional
    void findAll_listsEveryCollection() {
        repository.save(cube("c1", "A"));
        repository.save(cube("c2", "B"));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    @Transactional
    void delete_removesTheCollectionAndReportsWhetherItExisted() {
        repository.save(cube("c1", "A"));

        assertTrue(repository.delete("c1"));
        assertFalse(repository.delete("c1"));
        assertTrue(repository.findById("c1").isEmpty());
    }

    @Test
    @Transactional
    void findById_unknown_isEmpty() {
        assertTrue(repository.findById("nope").isEmpty());
    }
}
