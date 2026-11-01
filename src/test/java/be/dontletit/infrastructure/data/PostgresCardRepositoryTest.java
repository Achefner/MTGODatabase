package be.dontletit.infrastructure.data;

import be.dontletit.infrastructure.data.entity.CardEntity;
import be.dontletit.spi.CardData;
import be.dontletit.spi.CardPrintingData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
    void getAllCards_groupsPrintingsByName_unionsSets() {
        CardEntity first = buildEntity("bolt-lea", "Lightning Bolt", List.of("INSTANT"), List.of(), List.of(), null, null);
        first.sets = List.of("lea"); first.originalPrintedSet = "lea";
        CardEntity second = buildEntity("bolt-m11", "Lightning Bolt", List.of("INSTANT"), List.of(), List.of(), null, null);
        second.sets = List.of("m11"); second.originalPrintedSet = "m11";
        em.persist(first);
        em.persist(second);

        List<CardData> result = repository.getAllCards();

        assertEquals(1, result.size());
        assertEquals(List.of("lea", "m11"), result.get(0).sets());
    }

    @Test
    @Transactional
    void getAllCards_originalPrintingIsTheEarliestReleased() {
        CardEntity reprint = buildEntity("bolt-m11", "Lightning Bolt", List.of("INSTANT"), List.of(), List.of(), null, null);
        reprint.sets = List.of("m11"); reprint.originalPrintedSet = "m11";
        reprint.releasedAt = LocalDate.of(2010, 7, 16); reprint.rarity = "COMMON";
        reprint.imgUrl = "https://img/m11.jpg";
        CardEntity original = buildEntity("bolt-lea", "Lightning Bolt", List.of("INSTANT"), List.of(), List.of(), null, null);
        original.sets = List.of("lea"); original.originalPrintedSet = "lea";
        original.releasedAt = LocalDate.of(1993, 8, 5); original.rarity = "UNCOMMON";
        original.imgUrl = "https://img/lea.jpg";
        CardEntity undated = buildEntity("bolt-undated", "Lightning Bolt", List.of("INSTANT"), List.of(), List.of(), null, null);
        undated.sets = List.of("prm"); undated.originalPrintedSet = "prm";
        em.persist(reprint);
        em.persist(undated);
        em.persist(original);

        CardData card = repository.getAllCards().get(0);

        assertEquals("bolt-lea", card.id());
        assertEquals("lea", card.originalPrintedSet());
        assertEquals(LocalDate.of(1993, 8, 5), card.firstPrintedAt());
        assertEquals("UNCOMMON", card.rarity());
        assertEquals("https://img/lea.jpg", card.imgUrl());
        assertEquals(List.of("lea", "m11", "prm"), card.sets());
    }

    @Test
    @Transactional
    void getAllCards_prefersNonPromoPrintingOverEarlierPromo() {
        CardEntity prerelease = buildEntity("omnath-pmom", "Omnath", List.of("CREATURE"), List.of(), List.of(), null, null);
        prerelease.sets = List.of("pmom"); prerelease.originalPrintedSet = "pmom";
        prerelease.setName = "March of the Machine Promos"; prerelease.setType = "promo"; prerelease.promo = true;
        prerelease.releasedAt = LocalDate.of(2023, 4, 14);
        CardEntity main = buildEntity("omnath-mom", "Omnath", List.of("CREATURE"), List.of(), List.of(), null, null);
        main.sets = List.of("mom"); main.originalPrintedSet = "mom";
        main.setName = "March of the Machine"; main.setType = "expansion"; main.promo = false;
        main.releasedAt = LocalDate.of(2023, 4, 21);
        em.persist(prerelease);
        em.persist(main);

        CardData card = repository.getAllCards().get(0);

        assertEquals("mom", card.originalPrintedSet());
        assertEquals("March of the Machine", card.originalPrintedSetName());
        assertEquals("expansion", card.originalPrintedSetType());
        assertEquals(LocalDate.of(2023, 4, 21), card.firstPrintedAt());
    }

    @Test
    @Transactional
    void getAllCards_promoOnlyCard_fallsBackToThePromoPrinting() {
        CardEntity promo = buildEntity("judge", "Judge Only", List.of("CREATURE"), List.of(), List.of(), null, null);
        promo.sets = List.of("pjgp"); promo.originalPrintedSet = "pjgp"; promo.promo = true;
        promo.releasedAt = LocalDate.of(2010, 1, 1);
        em.persist(promo);

        assertEquals("pjgp", repository.getAllCards().get(0).originalPrintedSet());
    }

    @Test
    @Transactional
    void getAllCards_noReleaseDates_stillReturnsCard() {
        em.persist(buildEntity("x", "Undated", List.of("INSTANT"), List.of(), List.of(), null, null));

        CardData card = repository.getAllCards().get(0);

        assertNull(card.firstPrintedAt());
        assertNull(card.rarity());
    }

    @Test
    @Transactional
    void getAllCards_empty_returnsEmptyList() {
        assertTrue(repository.getAllCards().isEmpty());
    }

    static CardPrintingData printing(String id, String name, String set, boolean promo, LocalDate releasedAt) {
        return new CardPrintingData(id, name, "{R}", List.of("INSTANT"), List.of(), List.of(), "Deal 3 damage.", null, null,
                "R", set, set.toUpperCase() + " set", "expansion", promo, releasedAt, "COMMON", "https://img/" + id + ".jpg");
    }

    @Test
    void savePrintings_storesThem_andTheyReadBackAsOneCardPerName() {
        repository.savePrintings(List.of(
                printing("p1", "Lightning Bolt", "m11", false, LocalDate.of(2010, 7, 16)),
                printing("p2", "Lightning Bolt", "lea", false, LocalDate.of(1993, 8, 5)),
                printing("p3", "Lightning Bolt", "pbook", true, LocalDate.of(1990, 1, 1))));

        List<CardData> cards = repository.getAllCards();

        assertEquals(1, cards.size());
        CardData bolt = cards.get(0);
        assertEquals("p2", bolt.id(), "the earliest regular printing is the card");
        assertEquals("lea", bolt.originalPrintedSet());
        assertEquals("LEA set", bolt.originalPrintedSetName());
        assertEquals(List.of("lea", "m11", "pbook"), bolt.sets());
        assertEquals(List.of("INSTANT"), bolt.types());
        assertEquals("https://img/p2.jpg", bolt.imgUrl());
    }

    @Test
    void savePrintings_again_replacesThePrintingWithTheSameId() {
        repository.savePrintings(List.of(printing("p1", "Lightning Bolt", "m11", false, LocalDate.of(2010, 7, 16))));
        repository.savePrintings(List.of(printing("p1", "Lightning Bolt", "m10", false, LocalDate.of(2009, 7, 17))));

        List<CardData> cards = repository.getAllCards();
        assertEquals(1, cards.size());
        assertEquals("m10", cards.get(0).originalPrintedSet());
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
