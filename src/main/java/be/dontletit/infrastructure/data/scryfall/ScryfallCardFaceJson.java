package be.dontletit.infrastructure.data.scryfall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One face of a multi-face Scryfall card object (`card_faces`). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryfallCardFaceJson {

    public String name;

    @JsonProperty("mana_cost")
    public String manaCost;

    @JsonProperty("type_line")
    public String typeLine;

    @JsonProperty("oracle_text")
    public String oracleText;

    public String power;
    public String toughness;

    @JsonProperty("image_uris")
    public ScryfallImageUris imageUris;
}
