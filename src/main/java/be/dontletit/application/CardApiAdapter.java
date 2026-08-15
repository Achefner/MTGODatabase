package be.dontletit.application;

import be.dontletit.api.CardApiPort;
import be.dontletit.api.CardResponse;
import be.dontletit.domain.Card;
import be.dontletit.domain.CardType;
import be.dontletit.spi.CardData;
import be.dontletit.spi.CardSpiPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@ApplicationScoped
public class CardApiAdapter implements CardApiPort {

    private final CardSpiPort cardSpi;

    @Inject
    public CardApiAdapter(CardSpiPort cardSpi) {
        this.cardSpi = cardSpi;
    }

    @Override
    public Optional<CardResponse> getCardByName(String name) {
        return cardSpi.getAllCards().stream()
                .map(this::toDomain)
                .filter(card -> card.name().equalsIgnoreCase(name))
                .map(this::toResponse)
                .findFirst();
    }

    @Override
    public CardResponse getRandomCard() {
        List<CardData> cards = cardSpi.getAllCards();
        if (cards.isEmpty()) {
            throw new IllegalStateException("No cards in database");
        }
        return toResponse(toDomain(cards.get(ThreadLocalRandom.current().nextInt(cards.size()))));
    }

    @Override
    public List<CardResponse> getRandomCards(int count) {
        List<CardData> cards = cardSpi.getAllCards();
        if (cards.isEmpty()) {
            throw new IllegalStateException("No cards in database");
        }
        return IntStream.range(0, count)
                .mapToObj(i -> toResponse(toDomain(cards.get(ThreadLocalRandom.current().nextInt(cards.size())))))
                .toList();
    }

    private static boolean isKnownType(String type) {
        try {
            CardType.valueOf(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Card toDomain(CardData data) {
        return new Card(
                data.id(),
                data.name(),
                data.manaCost(),
                data.types().stream().filter(CardApiAdapter::isKnownType).map(CardType::valueOf).toList(),
                data.text(),
                data.power(),
                data.toughness(),
                data.surtypes(),
                data.subtypes(),
                data.colorIdentity(),
                data.sets(),
                data.originalPrintedSet(),
                data.imgUrl()
        );
    }

    private CardResponse toResponse(Card card) {
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
                card.imgUrl()
        );
    }
}
