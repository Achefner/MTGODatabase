package be.dontletit.application;

import be.dontletit.api.CollectionEntryRequest;
import be.dontletit.api.CollectionResponse;
import be.dontletit.api.UnknownCardsException;
import be.dontletit.domain.CollectionType;
import be.dontletit.spi.CardData;
import be.dontletit.stubs.CardSpiStub;
import be.dontletit.stubs.CollectionSpiStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionApiAdapterTest {

    static final CardData BRAZEN_BORROWER = new CardData(
            "brazen", "Brazen Borrower // Petty Theft", "{1}{U}{U}", List.of("CREATURE"), "Flash",
            3.0, 1.0, List.of(), List.of("Faerie", "Rogue"), "U", List.of("ELD"), "ELD", "Throne of Eldraine", "expansion",
            LocalDate.of(2019, 10, 4), "MYTHIC", null);

    CollectionSpiStub collections;
    CollectionApiAdapter adapter;

    @BeforeEach
    void setUp() {
        collections = new CollectionSpiStub();
        List<CardData> cards = new java.util.ArrayList<>(CardSpiStub.ALL_CARDS);
        cards.add(BRAZEN_BORROWER);
        adapter = new CollectionApiAdapter(collections, () -> cards);
    }

    private static CollectionEntryRequest entry(String name, Integer quantity) {
        return new CollectionEntryRequest(name, quantity);
    }

    @Test
    void create_storesTheCollectionWithAnIdAndCanonicalNames() {
        CollectionResponse created = adapter.create("Power LSV Cube", CollectionType.CUBE,
                List.of(entry("lightning bolt", 1), entry("Llanowar Elves", null)));

        assertNotNull(created.id());
        assertEquals("Power LSV Cube", created.name());
        assertEquals(CollectionType.CUBE, created.type());
        assertEquals(2, created.entries().size());
        assertEquals("Lightning Bolt", created.entries().get(0).cardName());
        assertEquals(1, created.entries().get(0).quantity());
        assertEquals(1, created.entries().get(1).quantity());
        assertEquals(2, created.cardCount());
        assertTrue(collections.stored.containsKey(created.id()));
    }

    @Test
    void create_resolvesFrontFaceNames() {
        CollectionResponse created = adapter.create("Faeries", CollectionType.DECK, List.of(entry("Brazen Borrower", 4)));

        assertEquals("Brazen Borrower // Petty Theft", created.entries().get(0).cardName());
        assertEquals(4, created.cardCount());
    }

    @Test
    void create_mergesRepeatedCards() {
        CollectionResponse created = adapter.create("Burn", CollectionType.DECK,
                List.of(entry("Lightning Bolt", 2), entry("lightning bolt", 2)));

        assertEquals(1, created.entries().size());
        assertEquals(4, created.entries().get(0).quantity());
    }

    @Test
    void create_unknownCards_rejectedWithTheirNames() {
        UnknownCardsException e = assertThrows(UnknownCardsException.class, () -> adapter.create("Bad", CollectionType.MISC,
                List.of(entry("Lightning Bolt", 1), entry("Black Lotuss", 1), entry("Mox Pearl", 1))));

        assertEquals(List.of("Black Lotuss", "Mox Pearl"), e.unknownNames());
        assertTrue(collections.stored.isEmpty());
    }

    @Test
    void create_blankNameOrNonPositiveQuantity_rejected() {
        assertThrows(IllegalArgumentException.class, () -> adapter.create("  ", CollectionType.CUBE, List.of(entry("Lightning Bolt", 1))));
        assertThrows(IllegalArgumentException.class, () -> adapter.create("Cube", CollectionType.CUBE, List.of(entry("Lightning Bolt", 0))));
        assertThrows(IllegalArgumentException.class, () -> adapter.create("Cube", null, List.of(entry("Lightning Bolt", 1))));
    }

    @Test
    void create_emptyEntries_allowed() {
        CollectionResponse created = adapter.create("Empty", CollectionType.MISC, List.of());
        assertEquals(0, created.cardCount());
    }

    @Test
    void list_get_delete_roundTrip() {
        CollectionResponse a = adapter.create("A", CollectionType.CUBE, List.of(entry("Lightning Bolt", 1)));
        CollectionResponse b = adapter.create("B", CollectionType.DECK, List.of(entry("Serra Angel", 4)));

        assertEquals(List.of(a.id(), b.id()), adapter.list().stream().map(be.dontletit.api.CollectionSummaryResponse::id).toList());
        assertEquals(4, adapter.get(b.id()).orElseThrow().cardCount());
        assertTrue(adapter.get("nope").isEmpty());
        assertTrue(adapter.delete(a.id()));
        assertFalse(adapter.delete(a.id()));
        assertEquals(1, adapter.list().size());
    }

    @Test
    void replace_overwritesEntriesNameAndType() {
        CollectionResponse a = adapter.create("A", CollectionType.CUBE, List.of(entry("Lightning Bolt", 1)));

        CollectionResponse replaced = adapter.replace(a.id(), "A2", CollectionType.DECK, List.of(entry("Serra Angel", 2))).orElseThrow();

        assertEquals(a.id(), replaced.id());
        assertEquals("A2", replaced.name());
        assertEquals(CollectionType.DECK, replaced.type());
        assertEquals("Serra Angel", replaced.entries().get(0).cardName());
        assertTrue(adapter.replace("nope", "x", CollectionType.MISC, List.of()).isEmpty());
    }

    @Test
    void cards_expandsEntriesIntoFullCards() {
        CollectionResponse a = adapter.create("A", CollectionType.DECK, List.of(entry("Lightning Bolt", 2), entry("Serra Angel", 1)));

        var cards = adapter.cards(a.id()).orElseThrow();

        assertEquals(List.of("Lightning Bolt", "Lightning Bolt", "Serra Angel"), cards.stream().map(c -> c.name()).toList());
        assertEquals("{R}", cards.get(0).manaCost());
    }
}
