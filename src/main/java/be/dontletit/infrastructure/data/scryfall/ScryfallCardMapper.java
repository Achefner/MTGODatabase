package be.dontletit.infrastructure.data.scryfall;

import be.dontletit.infrastructure.data.entity.CardEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ScryfallCardMapper {

    private static final Set<String> SUPERTYPES = Set.of("Basic", "Legendary", "Snow", "Ongoing", "World");

    /** Scryfall objects that are not playable cards: Art Series, tokens, emblems. */
    private static final Set<String> NON_CARD_LAYOUTS = Set.of("art_series", "token", "double_faced_token", "emblem");
    private static final String FACE_SEPARATOR = " // ";

    /** Layouts whose card has the combined characteristics of both halves (rule 709.3). */
    private static final Set<String> COMBINED_LAYOUTS = Set.of("split", "aftermath");

    public boolean isImportable(ScryfallCardJson card) {
        return card.layout == null || !NON_CARD_LAYOUTS.contains(card.layout);
    }

    public CardEntity toEntity(ScryfallCardJson card) {
        ScryfallCardFaceJson front = frontFace(card);
        ParsedTypeLine parsed = parseTypeLine(front != null ? front.typeLine : card.typeLine);

        CardEntity entity = new CardEntity();
        entity.id = card.id;
        entity.name = cardName(card.name);
        entity.manaCost = front != null && front.manaCost != null ? front.manaCost : card.manaCost;
        entity.types = parsed.types();
        entity.surtypes = parsed.surtypes();
        entity.subtypes = parsed.subtypes();
        entity.text = front != null ? front.oracleText : card.oracleText;
        entity.power = parsePowerToughness(front != null ? front.power : card.power);
        entity.toughness = parsePowerToughness(front != null ? front.toughness : card.toughness);
        entity.colorIdentity = card.colorIdentity != null ? String.join("", card.colorIdentity) : "";
        entity.sets = card.set != null ? List.of(card.set) : List.of();
        entity.originalPrintedSet = card.set;
        entity.imgUrl = front != null && front.imageUris != null ? front.imageUris.large
                : card.imageUris != null ? card.imageUris.large : null;
        entity.setName = card.setName;
        entity.setType = card.setType;
        entity.promo = Boolean.TRUE.equals(card.promo);
        entity.rarity = card.rarity != null ? card.rarity.toUpperCase() : null;
        entity.releasedAt = parseDate(card.releasedAt);
        return entity;
    }

    /**
     * The face whose characteristics the card has outside the stack: the front face of
     * transform / modal / adventure / flip cards. Split cards combine both halves, so they
     * (and single-face cards) return null and the top-level fields are used instead.
     */
    static ScryfallCardFaceJson frontFace(ScryfallCardJson card) {
        if (card.cardFaces == null || card.cardFaces.isEmpty()) return null;
        if (card.layout != null && COMBINED_LAYOUTS.contains(card.layout)) return null;
        return card.cardFaces.get(0);
    }

    /**
     * Parses a Scryfall type line. Multi-face cards (split, adventure, transform...) have one
     * type line per face joined by " // "; the faces are merged, keeping order and dropping
     * duplicates, so "Sorcery // Sorcery" is just a sorcery.
     */
    ParsedTypeLine parseTypeLine(String typeLine) {
        if (typeLine == null) return new ParsedTypeLine(List.of(), List.of(), List.of());

        Set<String> surtypes = new LinkedHashSet<>();
        Set<String> types = new LinkedHashSet<>();
        Set<String> subtypes = new LinkedHashSet<>();

        for (String face : typeLine.split(FACE_SEPARATOR)) {
            String[] halves = face.trim().split(" — ");
            for (String word : halves[0].trim().split(" ")) {
                if (word.isBlank()) continue;
                if (SUPERTYPES.contains(word)) surtypes.add(word.toUpperCase());
                else types.add(word.toUpperCase());
            }
            if (halves.length > 1) {
                for (String word : halves[1].trim().split(" ")) {
                    if (!word.isBlank()) subtypes.add(word);
                }
            }
        }

        return new ParsedTypeLine(List.copyOf(types), List.copyOf(surtypes), List.copyOf(subtypes));
    }

    /** Reversible cards are named "X // X" by Scryfall; a single face name is what players know. */
    static String cardName(String name) {
        if (name == null) return null;
        int separator = name.indexOf(FACE_SEPARATOR);
        if (separator < 0) return name;
        String front = name.substring(0, separator);
        String back = name.substring(separator + FACE_SEPARATOR.length());
        return front.equals(back) ? front : name;
    }

    LocalDate parseDate(String value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
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
