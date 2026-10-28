package be.dontletit.infrastructure.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CollectionEntryEmbeddable {

    @Column(name = "card_name", nullable = false)
    public String cardName;

    @Column(nullable = false)
    public int quantity;

    public CollectionEntryEmbeddable() {}

    public CollectionEntryEmbeddable(String cardName, int quantity) {
        this.cardName = cardName;
        this.quantity = quantity;
    }
}
