package be.dontletit.infrastructure.data;

import be.dontletit.infrastructure.data.entity.CardEntity;
import be.dontletit.spi.CardData;
import be.dontletit.spi.CardSpiPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

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
        CardEntity canonical = printings.get(0);
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
                canonical.imgUrl
        );
    }
}
