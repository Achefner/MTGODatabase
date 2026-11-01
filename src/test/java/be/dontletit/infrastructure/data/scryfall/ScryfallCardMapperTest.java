package be.dontletit.infrastructure.data.scryfall;

import be.dontletit.spi.CardPrintingData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
    void parseTypeLine_splitCardSameTypes_deduplicated() {
        var result = mapper.parseTypeLine("Sorcery // Sorcery");
        assertEquals(List.of("SORCERY"), result.types());
        assertEquals(List.of(), result.subtypes());
    }

    @Test
    void parseTypeLine_splitCardDifferentTypes_unionInOrder() {
        var result = mapper.parseTypeLine("Sorcery // Instant");
        assertEquals(List.of("SORCERY", "INSTANT"), result.types());
    }

    @Test
    void parseTypeLine_doubleFacedCreature_mergesSubtypesWithoutSeparator() {
        var result = mapper.parseTypeLine("Creature — Human Werewolf // Creature — Werewolf");
        assertEquals(List.of("CREATURE"), result.types());
        assertEquals(List.of("Human", "Werewolf"), result.subtypes());
        assertEquals(List.of(), result.surtypes());
    }

    @Test
    void parseTypeLine_adventure_mergesSupertypesTypesAndSubtypes() {
        var result = mapper.parseTypeLine("Legendary Creature — Elf Knight // Sorcery — Adventure");
        assertEquals(List.of("LEGENDARY"), result.surtypes());
        assertEquals(List.of("CREATURE", "SORCERY"), result.types());
        assertEquals(List.of("Elf", "Knight", "Adventure"), result.subtypes());
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

    // --- toPrinting ---

    @Test
    void toPrinting_creature_mapsAllFields() {
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
        card.rarity = "common";
        card.releasedAt = "2005-07-29";
        card.setName = "Ninth Edition";
        card.setType = "core";
        card.promo = true;
        ScryfallImageUris uris = new ScryfallImageUris();
        uris.large = "https://example.com/storm-crow.jpg";
        card.imageUris = uris;

        CardPrintingData printing = mapper.toPrinting(card);

        assertEquals("abc-123", printing.id());
        assertEquals("Storm Crow", printing.name());
        assertEquals("{1}{U}", printing.manaCost());
        assertEquals(List.of("CREATURE"), printing.types());
        assertEquals(List.of("Bird"), printing.subtypes());
        assertEquals("Flying", printing.text());
        assertEquals(1.0, printing.power());
        assertEquals(2.0, printing.toughness());
        assertEquals("U", printing.colorIdentity());
        assertEquals("9ed", printing.set());
        assertEquals("https://example.com/storm-crow.jpg", printing.imgUrl());
        assertEquals("COMMON", printing.rarity());
        assertEquals(LocalDate.of(2005, 7, 29), printing.releasedAt());
        assertEquals("Ninth Edition", printing.setName());
        assertEquals("core", printing.setType());
        assertTrue(printing.promo());
    }

    @Test
    void toPrinting_missingPromoFlag_isNotPromo() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "x";
        card.name = "X";
        card.typeLine = "Instant";

        CardPrintingData printing = mapper.toPrinting(card);

        assertFalse(printing.promo());
        assertNull(printing.setName());
        assertNull(printing.setType());
    }

    @Test
    void toPrinting_nullRarityAndReleaseDate_stayNull() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "bare";
        card.name = "Bare";
        card.typeLine = "Instant";

        CardPrintingData printing = mapper.toPrinting(card);

        assertNull(printing.rarity());
        assertNull(printing.releasedAt());
    }

    @Test
    void toPrinting_unparseableReleaseDate_isNull() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "bad-date";
        card.name = "Bad Date";
        card.typeLine = "Instant";
        card.releasedAt = "not-a-date";

        assertNull(mapper.toPrinting(card).releasedAt());
    }

    @Test
    void toPrinting_noImageUris_imgUrlNull() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "no-image";
        card.name = "Faceless Card";
        card.typeLine = "Instant";
        card.imageUris = null;

        CardPrintingData printing = mapper.toPrinting(card);

        assertNull(printing.imgUrl());
    }

    @Test
    void toPrinting_multicolorIdentity_joined() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "multi";
        card.name = "Multicolor";
        card.typeLine = "Creature — Human";
        card.colorIdentity = List.of("W", "U", "B");

        CardPrintingData printing = mapper.toPrinting(card);

        assertEquals("WUB", printing.colorIdentity());
    }

    @Test
    void toPrinting_nullSet_emptySets() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "no-set";
        card.name = "Token";
        card.typeLine = "Creature — Saproling";
        card.set = null;

        CardPrintingData printing = mapper.toPrinting(card);

        assertNull(printing.set());
    }

    // --- names of multi-face objects ---

    @Test
    void toPrinting_identicalFaceNames_collapsedToOneName() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "reversible";
        card.name = "Zndrsplt, Eye of Wisdom // Zndrsplt, Eye of Wisdom";
        card.typeLine = "Legendary Creature — Homunculus // Legendary Creature — Homunculus";

        assertEquals("Zndrsplt, Eye of Wisdom", mapper.toPrinting(card).name());
    }

    @Test
    void toPrinting_differentFaceNames_keptAsScryfallNamesThem() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "dfc";
        card.name = "Delver of Secrets // Insectile Aberration";
        card.typeLine = "Creature — Human Wizard // Creature — Human Insect";

        assertEquals("Delver of Secrets // Insectile Aberration", mapper.toPrinting(card).name());
    }

    // --- isImportable ---

    @Test
    void isImportable_regularLayouts_true() {
        for (String layout : new String[] {"normal", "split", "transform", "modal_dfc", "adventure", "saga", "planar", "reversible_card", null}) {
            ScryfallCardJson card = new ScryfallCardJson();
            card.layout = layout;
            assertTrue(mapper.isImportable(card), "layout " + layout);
        }
    }

    @Test
    void isImportable_nonCardLayouts_false() {
        for (String layout : new String[] {"art_series", "token", "double_faced_token", "emblem"}) {
            ScryfallCardJson card = new ScryfallCardJson();
            card.layout = layout;
            assertFalse(mapper.isImportable(card), "layout " + layout);
        }
    }

    // --- multi-face cards: the front face is the card as the game sees it ---

    private static ScryfallCardFaceJson face(String name, String manaCost, String typeLine, String text, String power, String toughness, String image) {
        ScryfallCardFaceJson f = new ScryfallCardFaceJson();
        f.name = name; f.manaCost = manaCost; f.typeLine = typeLine; f.oracleText = text; f.power = power; f.toughness = toughness;
        if (image != null) { f.imageUris = new ScryfallImageUris(); f.imageUris.large = image; }
        return f;
    }

    @Test
    void toPrinting_adventure_usesTheCreatureFaceOnly() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "brazen"; card.name = "Brazen Borrower // Petty Theft"; card.layout = "adventure";
        card.manaCost = "{1}{U}{U} // {1}{U}";
        card.typeLine = "Creature — Faerie Rogue // Instant — Adventure";
        card.oracleText = "Flash\nFlying // Return target nonland permanent an opponent controls to its owner's hand.";
        card.power = "3"; card.toughness = "1";
        card.imageUris = new ScryfallImageUris(); card.imageUris.large = "https://img/brazen.jpg";
        card.cardFaces = List.of(
                face("Brazen Borrower", "{1}{U}{U}", "Creature — Faerie Rogue", "Flash\nFlying", "3", "1", null),
                face("Petty Theft", "{1}{U}", "Instant — Adventure", "Return target nonland permanent...", null, null, null));

        CardPrintingData printing = mapper.toPrinting(card);

        assertEquals("Brazen Borrower // Petty Theft", printing.name());
        assertEquals("{1}{U}{U}", printing.manaCost());
        assertEquals(List.of("CREATURE"), printing.types());
        assertEquals(List.of("Faerie", "Rogue"), printing.subtypes());
        assertEquals("Flash\nFlying", printing.text());
        assertEquals(3.0, printing.power());
        assertEquals(1.0, printing.toughness());
        assertEquals("https://img/brazen.jpg", printing.imgUrl());
    }

    @Test
    void toPrinting_transform_usesTheFrontFaceAndItsImage() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "delver"; card.name = "Delver of Secrets // Insectile Aberration"; card.layout = "transform";
        card.manaCost = null;
        card.typeLine = "Creature — Human Wizard // Creature — Human Insect";
        card.imageUris = null;
        card.cardFaces = List.of(
                face("Delver of Secrets", "{U}", "Creature — Human Wizard", "At the beginning of your upkeep...", "1", "1", "https://img/delver-front.jpg"),
                face("Insectile Aberration", "", "Creature — Human Insect", "Flying", "3", "2", "https://img/delver-back.jpg"));

        CardPrintingData printing = mapper.toPrinting(card);

        assertEquals("{U}", printing.manaCost());
        assertEquals(List.of("CREATURE"), printing.types());
        assertEquals(List.of("Human", "Wizard"), printing.subtypes());
        assertEquals(1.0, printing.power());
        assertEquals(1.0, printing.toughness());
        assertEquals("https://img/delver-front.jpg", printing.imgUrl());
    }

    @Test
    void toPrinting_splitCard_keepsTheCombinedCharacteristics() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "spring-mind"; card.name = "Spring // Mind"; card.layout = "split";
        card.manaCost = "{2}{G} // {4}{G}{U}";
        card.typeLine = "Sorcery // Instant";
        card.oracleText = "Search... // Draw two cards.";
        card.imageUris = new ScryfallImageUris(); card.imageUris.large = "https://img/spring-mind.jpg";
        card.cardFaces = List.of(
                face("Spring", "{2}{G}", "Sorcery", "Search...", null, null, null),
                face("Mind", "{4}{G}{U}", "Instant", "Draw two cards.", null, null, null));

        CardPrintingData printing = mapper.toPrinting(card);

        assertEquals("{2}{G} // {4}{G}{U}", printing.manaCost());
        assertEquals(List.of("SORCERY", "INSTANT"), printing.types());
        assertEquals("https://img/spring-mind.jpg", printing.imgUrl());
    }

    @Test
    void toPrinting_singleFaceWithEmptyFacesList_unchanged() {
        ScryfallCardJson card = new ScryfallCardJson();
        card.id = "bolt"; card.name = "Lightning Bolt"; card.layout = "normal";
        card.manaCost = "{R}"; card.typeLine = "Instant"; card.cardFaces = List.of();

        CardPrintingData printing = mapper.toPrinting(card);

        assertEquals("{R}", printing.manaCost());
        assertEquals(List.of("INSTANT"), printing.types());
    }
}
