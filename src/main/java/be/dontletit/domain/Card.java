package be.dontletit.domain;

import java.time.LocalDate;
import java.util.List;

public record Card(
        String id,
        String name,
        String manaCost,
        List<CardType> types,
        String text,
        Double power,
        Double toughness,
        List<String> surtypes,
        List<String> subtypes,
        String colorIdentity,
        List<String> sets,
        String originalPrintedSet,
        String originalPrintedSetName,
        String originalPrintedSetType,
        LocalDate firstPrintedAt,
        String rarity,
        String imgUrl
) {}
