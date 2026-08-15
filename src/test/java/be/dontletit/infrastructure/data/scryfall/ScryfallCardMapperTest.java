package be.dontletit.infrastructure.data.scryfall;

import be.dontletit.infrastructure.data.entity.CardEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScryfallCardMapperTest {

    ScryfallCardMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ScryfallCardMapper();
    }

    // --- parseTypeLine ---

    @Test
    void parseTypeLine_creature_withSubtypes() {
        var result = mapper.parseTypeLine("Creature — Elf Druid");
        assertEquals(List.of("CREATURE"), result.types());
        assertEquals(List.of(), result.surtypes());
        assertEquals(List.of("Elf", "Druid"), result.subtypes());
    }

    @Test
    void parseTypeLine_legendaryCreature_withSubtype() {
        var result = mapper.parseTypeLine("Legendary Creature — Angel");
        assertEquals(List.of("CREATURE"), result.types());
        assertEquals(List.of("LEGENDARY"), result.surtypes());
        assertEquals(List.of("Angel"), result.subtypes());
    }

    @Test
    void parseTypeLine_artifact_noSubtypes() {
        var result = mapper.parseTypeLine("Artifact");
        assertEquals(List.of("ARTIFACT"), result.types());
        assertEquals(List.of(), result.surtypes());
        assertEquals(List.of(), result.subtypes());
    }

    @Test
    void parseTypeLine_enchantmentAura() {
        var result = mapper.parseTypeLine("Enchantment — Aura");
        assertEquals(List.of("ENCHANTMENT"), result.types());
        assertEquals(List.of(), result.surtypes());
        assertEquals(List.of("Aura"), result.subtypes());
    }

    @Test
    void parseTypeLine_basicLand() {
        var result = mapper.parseTypeLine("Basic Land — Forest");
        assertEquals(List.of("LAND"), result.types());
        assertEquals(List.of("BASIC"), result.surtypes());
        assertEquals(List.of("Forest"), result.subtypes());
    }

    @Test
    void parseTypeLine_sorcery_noSubtypes() {
        var result = mapper.parseTypeLine("Sorcery");
        assertEquals(List.of("SORCERY"), result.types());
        assertEquals(List.of(), result.surtypes());
        assertEquals(List.of(), result.subtypes());
    }

    @Test
    void parseTypeLine_null_returnsEmpty() {
        var result = mapper.parseTypeLine(null);
        assertEquals(List.of(), result.types());
        assertEquals(List.of(), result.surtypes());
        assertEquals(List.of(), result.subtypes());
    }

    // --- parsePowerToughness ---

    @Test
    void parsePowerToughness_integer() {
        assertEquals(3.0, mapper.parsePowerToughness("3"));
    }

    @Test
    void parsePowerToughness_zero() {
        assertEquals(0.0, mapper.parsePowerToughness("0"));
    }

    @Test
    void parsePowerToughness_star_returnsNull() {
        assertNull(mapper.parsePowerToughness("*"));
    }

    @Test
    void parsePowerToughness_starPlus_returnsNull() {
        assertNull(mapper.parsePowerToughness("*+1"));
    }

    @Test
    void parsePowerToughness_null_returnsNull() {
        assertNull(mapper.parsePowerToughness(null));
    }

    // --- toEntity ---

    @Test
    void toEntity_creature_mapsAllFields() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "abc-123";
        card.name = "Storm Crow";
        card.manaCost = "{1}{U}";
        card.typeLine = "Creature — Bird";
        card.oracleText = "Flying";
        card.power = "1";
        card.toughness = "2";
        card.colorIdentity = List.of("U");
        card.set = "9ed";
        ScryfallImageUris uris = new ScryfallImageUris();
        uris.large = "https://example.com/storm-crow.jpg";
        card.imageUris = uris;

        CardEntity entity = mapper.toEntity(card);

        assertEquals("abc-123", entity.id);
        assertEquals("Storm Crow", entity.name);
        assertEquals("{1}{U}", entity.manaCost);
        assertEquals(List.of("CREATURE"), entity.types);
        assertEquals(List.of("Bird"), entity.subtypes);
        assertEquals("Flying", entity.text);
        assertEquals(1.0, entity.power);
        assertEquals(2.0, entity.toughness);
        assertEquals("U", entity.colorIdentity);
        assertEquals(List.of("9ed"), entity.sets);
        assertEquals("9ed", entity.originalPrintedSet);
        assertEquals("https://example.com/storm-crow.jpg", entity.imgUrl);
    }

    @Test
    void toEntity_noImageUris_imgUrlNull() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "no-image";
        card.name = "Faceless Card";
        card.typeLine = "Instant";
        card.imageUris = null;

        CardEntity entity = mapper.toEntity(card);

        assertNull(entity.imgUrl);
    }

    @Test
    void toEntity_multicolorIdentity_joined() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "multi";
        card.name = "Multicolor";
        card.typeLine = "Creature — Human";
        card.colorIdentity = List.of("W", "U", "B");

        CardEntity entity = mapper.toEntity(card);

        assertEquals("WUB", entity.colorIdentity);
    }

    @Test
    void toEntity_nullSet_emptySets() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "no-set";
        card.name = "Token";
        card.typeLine = "Creature — Saproling";
        card.set = null;

        CardEntity entity = mapper.toEntity(card);

        assertEquals(List.of(), entity.sets);
        assertNull(entity.originalPrintedSet);
    }
}
