package be.dontletit.infrastructure.data;

import be.dontletit.infrastructure.data.entity.CardEntity;
import be.dontletit.spi.CardData;
import be.dontletit.spi.CardSpiPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class PostgresCardRepository implements CardSpiPort {

    @Inject
    EntityManager em;

    @Override
    public List<CardData> getAllCards() {
        return em.createQuery("FROM CardEntity", CardEntity.class)
                .getResultList()
                .stream()
                .collect(Collectors.groupingBy(e -> e.name))
                .values()
                .stream()
                .map(this::toData)
                .toList();
    }

    private CardData toData(List<CardEntity> printings) {
        // First printing = earliest release among regular printings; promo printings
        // (prerelease, judge, buy-a-box...) are only used when nothing else exists.
        CardEntity canonical = printings.stream()
                .min(Comparator.comparing((CardEntity e) -> e.promo)
                        .thenComparing(e -> e.releasedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(e -> e.id))
                .orElseThrow();
        List<String> sets = printings.stream()
                .map(e -> e.originalPrintedSet)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        return new CardData(
                canonical.id,
                canonical.name,
                canonical.manaCost,
                canonical.types,
                canonical.text,
                canonical.power,
                canonical.toughness,
                canonical.surtypes,
                canonical.subtypes,
                canonical.colorIdentity,
                sets,
                canonical.originalPrintedSet,
                canonical.setName,
                canonical.setType,
                canonical.releasedAt,
                canonical.rarity,
                canonical.imgUrl
        );
    }
}
