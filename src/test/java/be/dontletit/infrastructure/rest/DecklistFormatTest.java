package be.dontletit.infrastructure.rest;

import be.dontletit.api.CollectionEntryRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DecklistFormatTest {

    @Test
    void parse_quantityPrefixedLines() {
        List<CollectionEntryRequest> entries = DecklistFormat.parse("4 Lightning Bolt\n2x Counterspell\n1 Black Lotus");
        assertEquals(List.of(
                new CollectionEntryRequest("Lightning Bolt", 4),
                new CollectionEntryRequest("Counterspell", 2),
                new CollectionEntryRequest("Black Lotus", 1)), entries);
    }

    @Test
    void parse_bareNamesCountAsOne() {
        List<CollectionEntryRequest> entries = DecklistFormat.parse("Black Lotus\nMox Pearl");
        assertEquals(List.of(new CollectionEntryRequest("Black Lotus", 1), new CollectionEntryRequest("Mox Pearl", 1)), entries);
    }

    @Test
    void parse_skipsBlankLinesCommentsAndSectionHeaders() {
        List<CollectionEntryRequest> entries = DecklistFormat.parse("Deck\n\n# my burn list\n// another comment\n4 Lightning Bolt\r\n\nSideboard\n2 Red Elemental Blast\n");
        assertEquals(List.of(new CollectionEntryRequest("Lightning Bolt", 4), new CollectionEntryRequest("Red Elemental Blast", 2)), entries);
    }

    @Test
    void parse_stripsArenaSetAndCollectorNumberSuffix() {
        List<CollectionEntryRequest> entries = DecklistFormat.parse("4 Lightning Bolt (M11) 149\n1 Jace, Vryn's Prodigy (ORI) 60");
        assertEquals(List.of(new CollectionEntryRequest("Lightning Bolt", 4), new CollectionEntryRequest("Jace, Vryn's Prodigy", 1)), entries);
    }

    @Test
    void parse_keepsNamesThatStartWithADigit() {
        List<CollectionEntryRequest> entries = DecklistFormat.parse("1 1996 World Champion\n2x 2 Face Cat");
        assertEquals(List.of(new CollectionEntryRequest("1996 World Champion", 1), new CollectionEntryRequest("2 Face Cat", 2)), entries);
    }

    @Test
    void parse_nullOrEmpty_returnsNoEntries() {
        assertTrue(DecklistFormat.parse(null).isEmpty());
        assertTrue(DecklistFormat.parse("   \n").isEmpty());
    }

    @Test
    void format_oneLinePerEntryWithQuantity() {
        String text = DecklistFormat.format(List.of(
                new be.dontletit.api.CollectionEntryResponse("Lightning Bolt", 4),
                new be.dontletit.api.CollectionEntryResponse("Black Lotus", 1)));
        assertEquals("4 Lightning Bolt\n1 Black Lotus\n", text);
    }

    @Test
    void parseThenFormat_roundTrips() {
        String original = "4 Lightning Bolt\n1 Black Lotus\n";
        var entries = DecklistFormat.parse(original).stream()
                .map(e -> new be.dontletit.api.CollectionEntryResponse(e.name(), e.quantity()))
                .toList();
        assertEquals(original, DecklistFormat.format(entries));
    }
}
