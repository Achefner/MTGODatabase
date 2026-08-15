package be.dontletit.infrastructure.data;

import be.dontletit.spi.CardData;
import be.dontletit.spi.CardSpiPort;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@DefaultBean
@ApplicationScoped
public class InMemoryCardRepository implements CardSpiPort {

    private static final List<CardData> CARDS = List.of(
            new CardData("black-lotus", "Black Lotus", "{0}",
                    List.of("ARTIFACT"),
                    "{T}, Sacrifice Black Lotus: Add three mana of any one color.",
                    null, null, List.of(), List.of(), "", List.of("LEA"), "LEA", null),

            new CardData("lightning-bolt", "Lightning Bolt", "{R}",
                    List.of("INSTANT"),
                    "Lightning Bolt deals 3 damage to any target.",
                    null, null, List.of(), List.of(), "R", List.of("LEA", "M11"), "LEA", null),

            new CardData("counterspell", "Counterspell", "{U}{U}",
                    List.of("INSTANT"),
                    "Counter target spell.",
                    null, null, List.of(), List.of(), "U", List.of("LEA", "MMQ"), "LEA", null),

            new CardData("llanowar-elves", "Llanowar Elves", "{G}",
                    List.of("CREATURE"),
                    "{T}: Add {G}.",
                    1.0, 1.0, List.of(), List.of("Elf", "Druid"), "G", List.of("LEA", "M19"), "LEA", null),

            new CardData("serra-angel", "Serra Angel", "{3}{W}{W}",
                    List.of("CREATURE"),
                    "Flying, vigilance.",
                    4.0, 4.0, List.of(), List.of("Angel"), "W", List.of("LEA", "M15"), "LEA", null)
    );

    @Override
    public List<CardData> getAllCards() {
        return CARDS;
    }
}
