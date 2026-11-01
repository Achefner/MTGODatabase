package be.dontletit.application;

import be.dontletit.api.CardResponse;
import be.dontletit.api.SetResponse;
import be.dontletit.stubs.CardSpiStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static be.dontletit.stubs.CardSpiStub.*;
import static org.junit.jupiter.api.Assertions.*;

class CardApiAdapterTest {

    CardSpiStub spiStub;
    CardApiAdapter adapter;

    @BeforeEach
    void setUp() {
        spiStub = new CardSpiStub();
        adapter = new CardApiAdapter(spiStub);
    }

    @Test
    void getAllCards_returnsAllStubCards() {
        assertEquals(ALL_CARDS, spiStub.getAllCards());
        assertEquals(5, spiStub.getAllCards().size());
    }

    @Test
    void getCardByName_found_returnsCard() {
        Optional<CardResponse> result = adapter.getCardByName("Lightning Bolt");
        assertTrue(result.isPresent());
        assertEquals(LIGHTNING_BOLT.name(), result.get().name());
        assertEquals(LIGHTNING_BOLT.id(), result.get().id());
    }

    @Test
    void getCardByName_caseInsensitive_returnsCard() {
        Optional<CardResponse> result = adapter.getCardByName("lightning bolt");
        assertTrue(result.isPresent());
        assertEquals(LIGHTNING_BOLT.name(), result.get().name());
    }

    @Test
    void getCardByName_unknown_returnsEmpty() {
        assertTrue(adapter.getCardByName("Black Lotus").isEmpty());
    }

    @Test
    void getRandomCard_returnsCardFromList() {
        CardResponse card = adapter.getRandomCard();
        List<String> knownNames = ALL_CARDS.stream().map(c -> c.name()).toList();
        assertTrue(knownNames.contains(card.name()));
    }

    @Test
    void getRandomCards_returnsRequestedCount() {
        List<CardResponse> cards = adapter.getRandomCards(3);
        assertEquals(3, cards.size());
        List<String> knownNames = ALL_CARDS.stream().map(c -> c.name()).toList();
        cards.forEach(card -> assertTrue(knownNames.contains(card.name())));
    }

    @Test
    void getCardByName_exposesRarityAndFirstPrintedAt() {
        CardResponse card = adapter.getCardByName("Lightning Bolt").orElseThrow();
        assertEquals("COMMON", card.rarity());
        assertEquals(LIGHTNING_BOLT.firstPrintedAt(), card.firstPrintedAt());
    }

    @Test
    void getAllCardNames_returnsDistinctSortedNames() {
        List<String> names = adapter.getAllCardNames();
        assertEquals(List.of("Lightning Bolt", "Llanowar Elves", "Pack Rat", "Serra Angel", "Undated"), names);
    }

    @Test
    void getCardByName_exposesOriginalSetNameAndType() {
        CardResponse card = adapter.getCardByName("Pack Rat").orElseThrow();
        assertEquals("Return to Ravnica", card.originalPrintedSetName());
        assertEquals("expansion", card.originalPrintedSetType());
    }

    @Test
    void getFirstPrintingSets_groupsCardsByOriginalSet_chronologically() {
        List<SetResponse> sets = adapter.getFirstPrintingSets();

        assertEquals(2, sets.size());
        SetResponse lea = sets.get(0);
        assertEquals("LEA", lea.code());
        assertEquals("Limited Edition Alpha", lea.name());
        assertEquals("core", lea.type());
        assertEquals(LocalDate.of(1993, 8, 5), lea.releasedAt());
        assertEquals(3, lea.cardCount());
        SetResponse rtr = sets.get(1);
        assertEquals("RTR", rtr.code());
        assertEquals(LocalDate.of(2012, 10, 5), rtr.releasedAt());
        assertEquals(1, rtr.cardCount());
    }

    @Test
    void getFirstPrintingSets_emptyRepository_returnsEmptyList() {
        assertTrue(new CardApiAdapter(new CardSpiStub(List.of())).getFirstPrintingSets().isEmpty());
    }

    @Test
    void getAllCardNames_emptyRepository_returnsEmptyList() {
        assertTrue(new CardApiAdapter(new CardSpiStub(List.of())).getAllCardNames().isEmpty());
    }

    @Test
    void getRandomCard_emptyRepository_throwsIllegalState() {
        CardApiAdapter emptyAdapter = new CardApiAdapter(new CardSpiStub(List.of()));
        assertThrows(IllegalStateException.class, emptyAdapter::getRandomCard);
    }
}
