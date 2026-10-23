package be.dontletit.stubs;

import be.dontletit.spi.CardData;
import be.dontletit.spi.CardSpiPort;

import java.time.LocalDate;
import java.util.List;

public class CardSpiStub implements CardSpiPort {

    public static final CardData LIGHTNING_BOLT = new CardData(
            "lightning-bolt", "Lightning Bolt", "{R}",
            List.of("INSTANT"),
            "Lightning Bolt deals 3 damage to any target.",
            null, null, List.of(), List.of(), "R", List.of("LEA"), "LEA", "Limited Edition Alpha", "core", LocalDate.of(1993, 8, 5), "COMMON", null);

    public static final CardData LLANOWAR_ELVES = new CardData(
            "llanowar-elves", "Llanowar Elves", "{G}",
            List.of("CREATURE"),
            "{T}: Add {G}.",
            1.0, 1.0, List.of(), List.of("Elf", "Druid"), "G", List.of("LEA"), "LEA", "Limited Edition Alpha", "core", LocalDate.of(1993, 8, 5), "COMMON", null);

    public static final CardData SERRA_ANGEL = new CardData(
            "serra-angel", "Serra Angel", "{3}{W}{W}",
            List.of("CREATURE"),
            "Flying, vigilance.",
            4.0, 4.0, List.of(), List.of("Angel"), "W", List.of("LEA", "M15"), "LEA", "Limited Edition Alpha", "core", LocalDate.of(1993, 8, 5), "UNCOMMON", null);

    public static final CardData PACK_RAT = new CardData(
            "pack-rat", "Pack Rat", "{1}{B}",
            List.of("CREATURE"),
            "...",
            2.0, 2.0, List.of(), List.of("Rat"), "B", List.of("RTR"), "RTR", "Return to Ravnica", "expansion", LocalDate.of(2012, 10, 5), "RARE", null);

    public static final CardData UNDATED = new CardData(
            "undated", "Undated", "{1}",
            List.of("ARTIFACT"),
            "...",
            null, null, List.of(), List.of(), "", List.of(), null, null, null, null, null, null);

    public static final List<CardData> ALL_CARDS = List.of(LIGHTNING_BOLT, LLANOWAR_ELVES, SERRA_ANGEL, PACK_RAT, UNDATED);

    @Override
    public List<CardData> getAllCards() {
        return ALL_CARDS;
    }
}
