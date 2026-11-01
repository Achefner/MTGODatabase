package be.dontletit.spi;

import java.util.function.Consumer;

/** Where card printings come from (Scryfall's bulk data). Too many to hold in memory: they are streamed. */
public interface CardCatalogSpiPort {

    /** Hands every importable printing to {@code sink}, one at a time. */
    void readPrintings(Consumer<CardPrintingData> sink);
}
