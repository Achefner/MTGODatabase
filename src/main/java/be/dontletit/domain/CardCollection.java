package be.dontletit.domain;

import java.util.List;

/** A named set of cards: a deck, a cube, or anything else. */
public record CardCollection(String id, String name, CollectionType type, List<CollectionEntry> entries) {

    public int cardCount() {
        return entries.stream().mapToInt(CollectionEntry::quantity).sum();
    }
}
