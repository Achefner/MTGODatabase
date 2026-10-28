package be.dontletit.application;

import be.dontletit.api.CardApiPort;
import be.dontletit.api.CardResponse;
import be.dontletit.api.SetResponse;
import be.dontletit.domain.Card;
import be.dontletit.spi.CardData;
import be.dontletit.spi.CardSpiPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
    public List<String> getAllCardNames() {
        return cardSpi.getAllCards().stream()
                .map(CardData::name)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public List<SetResponse> getFirstPrintingSets() {
        return cardSpi.getAllCards().stream()
                .filter(card -> card.originalPrintedSet() != null)
                .collect(Collectors.groupingBy(CardData::originalPrintedSet))
                .values().stream()
                .map(CardApiAdapter::toSet)
                .sorted(Comparator.comparing(SetResponse::releasedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(SetResponse::code))
                .toList();
    }

    private static SetResponse toSet(List<CardData> cards) {
        CardData any = cards.get(0);
        LocalDate releasedAt = cards.stream()
                .map(CardData::firstPrintedAt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        return new SetResponse(any.originalPrintedSet(), any.originalPrintedSetName(), any.originalPrintedSetType(),
                releasedAt, cards.size());
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

    private Card toDomain(CardData data) {
        return CardMapping.toDomain(data);
    }

    private CardResponse toResponse(Card card) {
        return CardMapping.toResponse(card);
    }
}
