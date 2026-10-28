package be.dontletit.infrastructure.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import be.dontletit.domain.CollectionType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collections")
public class CollectionEntity extends PanacheEntityBase {

    @Id
    public String id;

    @Column(nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type", nullable = false)
    public CollectionType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "collection_entries", joinColumns = @JoinColumn(name = "collection_id"))
    @OrderColumn(name = "position")
    public List<CollectionEntryEmbeddable> entries = new ArrayList<>();
}
