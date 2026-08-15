package be.dontletit.infrastructure.data;

import be.dontletit.infrastructure.data.entity.CardEntity;
import be.dontletit.spi.CardData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PostgresCardRepositoryTest {

    @Inject
    PostgresCardRepository repository;

    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    void cleanUp() {
        em.createQuery("DELETE FROM CardEntity").executeUpdate();
    }

    @Test
    @Transactional
    void getAllCards_returnsPersistedCard() {
        em.persist(buildEntity("storm-crow", "Storm Crow", List.of("CREATURE"), List.of("Bird"), List.of(), 1.0, 2.0));

        List<CardData> result = repository.getAllCards();

        assertEquals(1, result.size());
        CardData card = result.get(0);
        assertEquals("storm-crow", card.id());
        assertEquals("Storm Crow", card.name());
        assertEquals(List.of("CREATURE"), card.types());
        assertEquals(List.of("Bird"), card.subtypes());
        assertEquals(1.0, card.power());
        assertEquals(2.0, card.toughness());
    }

    @Test
    @Transactional
    void getAllCards_multipleCards_allReturned() {
        em.persist(buildEntity("bolt", "Lightning Bolt", List.of("INSTANT"), List.of(), List.of(), null, null));
        em.persist(buildEntity("counterspell", "Counterspell", List.of("INSTANT"), List.of(), List.of(), null, null));

        assertEquals(2, repository.getAllCards().size());
    }

    @Test
    @Transactional
    void getAllCards_listsRoundtrip_converterWorks() {
        CardEntity entity = buildEntity("nissa", "Nissa", List.of("CREATURE"), List.of("Elf", "Druid"), List.of("LEGENDARY"), 3.0, 3.0);
        entity.colorIdentity = "G";
        entity.sets = List.of("drc");
        entity.originalPrintedSet = "drc";
        em.persist(entity);

        CardData result = repository.getAllCards().get(0);

        assertEquals(List.of("CREATURE"), result.types());
        assertEquals(List.of("Elf", "Druid"), result.subtypes());
        assertEquals(List.of("LEGENDARY"), result.surtypes());
        assertEquals("G", result.colorIdentity());
        assertEquals(List.of("drc"), result.sets());
    }

    @Test
    @Transactional
    void getAllCards_nullPowerToughness_handledCorrectly() {
        em.persist(buildEntity("counterspell", "Counterspell", List.of("INSTANT"), List.of(), List.of(), null, null));

        CardData result = repository.getAllCards().get(0);

        assertNull(result.power());
        assertNull(result.toughness());
    }

    @Test
    @Transactional
    void getAllCards_empty_returnsEmptyList() {
        assertTrue(repository.getAllCards().isEmpty());
    }

    private CardEntity buildEntity(String id, String name, List<String> types,
                                   List<String> subtypes, List<String> surtypes,
                                   Double power, Double toughness) {
        CardEntity e = new CardEntity();
        e.id = id;
        e.name = name;
        e.types = types;
        e.subtypes = subtypes;
        e.surtypes = surtypes;
        e.power = power;
        e.toughness = toughness;
        e.colorIdentity = "";
        e.sets = List.of();
        e.manaCost = "";
        return e;
    }
}
