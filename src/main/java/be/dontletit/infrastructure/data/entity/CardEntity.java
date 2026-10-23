package be.dontletit.infrastructure.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cards")
public class CardEntity extends PanacheEntityBase {

    @Id
    public String id;

    @Column(nullable = false)
    public String name;

    @Column(name = "mana_cost")
    public String manaCost;

    @Convert(converter = StringListConverter.class)
    @Column(name = "types")
    public List<String> types;

    @Column(name = "text", columnDefinition = "TEXT")
    public String text;

    public Double power;
    public Double toughness;

    @Convert(converter = StringListConverter.class)
    @Column(name = "surtypes")
    public List<String> surtypes;

    @Convert(converter = StringListConverter.class)
    @Column(name = "subtypes")
    public List<String> subtypes;

    @Column(name = "color_identity")
    public String colorIdentity;

    @Convert(converter = StringListConverter.class)
    @Column(name = "sets")
    public List<String> sets;

    @Column(name = "original_printed_set")
    public String originalPrintedSet;

    @Column(name = "set_name")
    public String setName;

    @Column(name = "set_type")
    public String setType;

    @Column(nullable = false)
    public boolean promo;

    @Column(name = "released_at")
    public LocalDate releasedAt;

    public String rarity;

    @Column(name = "img_url")
    public String imgUrl;
}
