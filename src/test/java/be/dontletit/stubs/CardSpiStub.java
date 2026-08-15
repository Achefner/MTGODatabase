package be.dontletit.stubs;

import be.dontletit.spi.CardData;
import be.dontletit.spi.CardSpiPort;

import java.util.List;

public class CardSpiStub implements CardSpiPort {

    public static final CardData LIGHTNING_BOLT = new CardData(
            "lightning-bolt", "Lightning Bolt", "{R}",
            List.of("INSTANT"),
            "Lightning Bolt deals 3 damage to any target.",
            null, null, List.of(), List.of(), "R", List.of("LEA"), "LEA", null);

    public static final CardData LLANOWAR_ELVES = new CardData(
            "llanowar-elves", "Llanowar Elves", "{G}",
            List.of("CREATURE"),
            "{T}: Add {G}.",
            1.0, 1.0, List.of(), List.of("Elf", "Druid"), "G", List.of("LEA"), "LEA", null);

    public static final List<CardData> ALL_CARDS = List.of(LIGHTNING_BOLT, LLANOWAR_ELVES);

    @Override
    public List<CardData> getAllCards() {
        return ALL_CARDS;
    }
}
