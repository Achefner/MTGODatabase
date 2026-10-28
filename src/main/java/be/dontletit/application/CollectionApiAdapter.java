package be.dontletit.application;

import be.dontletit.api.CardResponse;
import be.dontletit.api.CollectionApiPort;
import be.dontletit.api.CollectionEntryRequest;
import be.dontletit.api.CollectionEntryResponse;
import be.dontletit.api.CollectionResponse;
import be.dontletit.api.CollectionSummaryResponse;
import be.dontletit.api.UnknownCardsException;
import be.dontletit.domain.CardCollection;
import be.dontletit.domain.CollectionEntry;
import be.dontletit.domain.CollectionType;
import be.dontletit.spi.CardData;
import be.dontletit.spi.CardSpiPort;
import be.dontletit.spi.CollectionData;
import be.dontletit.spi.CollectionEntryData;
import be.dontletit.spi.CollectionSpiPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CollectionApiAdapter implements CollectionApiPort {

    private static final String FACE_SEPARATOR = " // ";

    private final CollectionSpiPort collections;
    private final CardSpiPort cards;

    @Inject
    public CollectionApiAdapter(CollectionSpiPort collections, CardSpiPort cards) {
        this.collections = collections;
        this.cards = cards;
    }

    @Override
    public CollectionResponse create(String name, CollectionType type, List<CollectionEntryRequest> entries) {
        CardCollection collection = validate(UUID.randomUUID().toString(), name, type, entries);
        return toResponse(collections.save(toData(collection)));
    }

    @Override
    public Optional<CollectionResponse> replace(String id, String name, CollectionType type, List<CollectionEntryRequest> entries) {
        if (collections.findById(id).isEmpty()) return Optional.empty();
        CardCollection collection = validate(id, name, type, entries);
        return Optional.of(toResponse(collections.save(toData(collection))));
    }

    @Override
    public List<CollectionSummaryResponse> list() {
        return collections.findAll().stream()
                .map(CollectionApiAdapter::toDomain)
                .map(c -> new CollectionSummaryResponse(c.id(), c.name(), c.type(), c.cardCount()))
                .toList();
    }

    @Override
    public Optional<CollectionResponse> get(String id) {
        return collections.findById(id).map(CollectionApiAdapter::toResponse);
    }

    @Override
    public Optional<List<CardResponse>> cards(String id) {
        return collections.findById(id).map(data -> {
            Map<String, CardData> byName = new HashMap<>();
            for (CardData card : cards.getAllCards()) byName.put(card.name().toLowerCase(), card);
            List<CardResponse> expanded = new ArrayList<>();
            for (CollectionEntryData entry : data.entries()) {
                CardData card = byName.get(entry.cardName().toLowerCase());
                if (card == null) continue; // card removed from the database since the import
                for (int i = 0; i < entry.quantity(); i++) expanded.add(CardMapping.toResponse(card));
            }
            return expanded;
        });
    }

    @Override
    public boolean delete(String id) {
        return collections.delete(id);
    }

    /** Checks the inputs, resolves every entry to a canonical card name and merges duplicates. */
    private CardCollection validate(String id, String name, CollectionType type, List<CollectionEntryRequest> entries) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Collection name must not be blank");
        if (type == null) throw new IllegalArgumentException("Collection type must be one of DECK, CUBE, MISC");

        Map<String, String> canonicalNames = canonicalNamesIndex();
        Map<String, Integer> merged = new LinkedHashMap<>();
        List<String> unknown = new ArrayList<>();
        for (CollectionEntryRequest entry : entries == null ? List.<CollectionEntryRequest>of() : entries) {
            int quantity = entry.quantity() == null ? 1 : entry.quantity();
            if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive for " + entry.name());
            String canonical = entry.name() == null ? null : canonicalNames.get(entry.name().trim().toLowerCase());
            if (canonical == null) {
                unknown.add(entry.name());
                continue;
            }
            merged.merge(canonical, quantity, Integer::sum);
        }
        if (!unknown.isEmpty()) throw new UnknownCardsException(unknown);

        List<CollectionEntry> resolved = merged.entrySet().stream()
                .map(e -> new CollectionEntry(e.getKey(), e.getValue()))
                .toList();
        return new CardCollection(id, name.trim(), type, resolved);
    }

    /** lower-cased full name, and lower-cased front-face name, -> canonical full name. */
    private Map<String, String> canonicalNamesIndex() {
        Map<String, String> index = new HashMap<>();
        for (CardData card : cards.getAllCards()) {
            String fullName = card.name();
            int separator = fullName.indexOf(FACE_SEPARATOR);
            if (separator > 0) index.putIfAbsent(fullName.substring(0, separator).toLowerCase(), fullName);
        }
        for (CardData card : cards.getAllCards()) {
            index.put(card.name().toLowerCase(), card.name()); // full names win over front-face aliases
        }
        return index;
    }

    private static CollectionData toData(CardCollection collection) {
        return new CollectionData(collection.id(), collection.name(), collection.type(),
                collection.entries().stream().map(e -> new CollectionEntryData(e.cardName(), e.quantity())).toList());
    }

    private static CardCollection toDomain(CollectionData data) {
        return new CardCollection(data.id(), data.name(), data.type(),
                data.entries().stream().map(e -> new CollectionEntry(e.cardName(), e.quantity())).toList());
    }

    private static CollectionResponse toResponse(CollectionData data) {
        CardCollection collection = toDomain(data);
        return new CollectionResponse(collection.id(), collection.name(), collection.type(), collection.cardCount(),
                collection.entries().stream().map(e -> new CollectionEntryResponse(e.cardName(), e.quantity())).toList());
    }
}
