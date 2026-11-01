package be.dontletit.spi;

import java.time.LocalDate;
import java.util.List;

/**
 * One printing of a card, as the catalog describes it. A card (by name) has many printings;
 * {@link CardSpiPort#getAllCards()} folds them into one {@link CardData} per name.
 */
public record CardPrintingData(
        String id,
        String name,
        String manaCost,
        List<String> types,
        List<String> surtypes,
        List<String> subtypes,
        String text,
        Double power,
        Double toughness,
        String colorIdentity,
        String set,
        String setName,
        String setType,
        boolean promo,
        LocalDate releasedAt,
        String rarity,
        String imgUrl
) {}
