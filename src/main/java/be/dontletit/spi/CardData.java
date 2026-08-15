package be.dontletit.spi;

import java.util.List;

public record CardData(
        String id,
        String name,
        String manaCost,
        List<String> types,
        String text,
        Double power,
        Double toughness,
        List<String> surtypes,
        List<String> subtypes,
        String colorIdentity,
        List<String> sets,
        String originalPrintedSet,
        String imgUrl
) {}
