package be.dontletit.spi;

import java.util.List;

public interface CardSpiPort {

    /** One entry per card name, folded from its printings. */
    List<CardData> getAllCards();

    /** Stores printings, replacing the ones with the same id. */
    void savePrintings(List<CardPrintingData> printings);
}
