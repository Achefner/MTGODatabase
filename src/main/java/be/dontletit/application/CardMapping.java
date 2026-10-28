package be.dontletit.application;

import be.dontletit.api.CardResponse;
import be.dontletit.domain.Card;
import be.dontletit.domain.CardType;
import be.dontletit.spi.CardData;

/** Shared CardData -> Card -> CardResponse conversions. */
final class CardMapping {

    private CardMapping() {}

    static boolean isKnownType(String type) {
        try {
            CardType.valueOf(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static Card toDomain(CardData data) {
        return new Card(
                data.id(),
                data.name(),
                data.manaCost(),
                data.types().stream().filter(CardMapping::isKnownType).map(CardType::valueOf).toList(),
                data.text(),
                data.power(),
                data.toughness(),
                data.surtypes(),
                data.subtypes(),
                data.colorIdentity(),
                data.sets(),
                data.originalPrintedSet(),
                data.originalPrintedSetName(),
                data.originalPrintedSetType(),
                data.firstPrintedAt(),
                data.rarity(),
                data.imgUrl()
        );
    }

    static CardResponse toResponse(Card card) {
        return new CardResponse(
                card.id(),
                card.name(),
                card.manaCost(),
                card.types().stream().map(Enum::name).toList(),
                card.text(),
                card.power(),
                card.toughness(),
                card.surtypes(),
                card.subtypes(),
                card.colorIdentity(),
                card.sets(),
                card.originalPrintedSet(),
                card.originalPrintedSetName(),
                card.originalPrintedSetType(),
                card.firstPrintedAt(),
                card.rarity(),
                card.imgUrl()
        );
    }

    static CardResponse toResponse(CardData data) {
        return toResponse(toDomain(data));
    }
}
