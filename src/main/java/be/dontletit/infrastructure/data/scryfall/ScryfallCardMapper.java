package be.dontletit.infrastructure.data.scryfall;

import be.dontletit.infrastructure.data.entity.CardEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ScryfallCardMapper {

    private static final Set<String> SUPERTYPES = Set.of("Basic", "Legendary", "Snow", "Ongoing", "World");

    public CardEntity toEntity(ScryfallCardJson card) {
        ParsedTypeLine parsed = parseTypeLine(card.typeLine);

        CardEntity entity = new CardEntity();
        entity.id = card.id;
        entity.name = card.name;
        entity.manaCost = card.manaCost;
        entity.types = parsed.types();
        entity.surtypes = parsed.surtypes();
        entity.subtypes = parsed.subtypes();
        entity.text = card.oracleText;
        entity.power = parsePowerToughness(card.power);
        entity.toughness = parsePowerToughness(card.toughness);
        entity.colorIdentity = card.colorIdentity != null ? String.join("", card.colorIdentity) : "";
        entity.sets = card.set != null ? List.of(card.set) : List.of();
        entity.originalPrintedSet = card.set;
        entity.imgUrl = card.imageUris != null ? card.imageUris.large : null;
        return entity;
    }

    ParsedTypeLine parseTypeLine(String typeLine) {
        if (typeLine == null) return new ParsedTypeLine(List.of(), List.of(), List.of());

        String[] halves = typeLine.split(" — ");
        List<String> surtypes = new ArrayList<>();
        List<String> types = new ArrayList<>();

        for (String word : halves[0].trim().split(" ")) {
            if (SUPERTYPES.contains(word)) surtypes.add(word.toUpperCase());
            else types.add(word.toUpperCase());
        }

        List<String> subtypes = halves.length > 1
                ? List.of(halves[1].trim().split(" "))
                : List.of();

        return new ParsedTypeLine(types, surtypes, subtypes);
    }

    Double parsePowerToughness(String value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    record ParsedTypeLine(List<String> types, List<String> surtypes, List<String> subtypes) {}
}
