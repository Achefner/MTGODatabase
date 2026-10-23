package be.dontletit.infrastructure.data.scryfall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryfallCardJson {

    public String id;
    public String name;
    public String layout;

    @JsonProperty("mana_cost")
    public String manaCost;

    @JsonProperty("type_line")
    public String typeLine;

    @JsonProperty("oracle_text")
    public String oracleText;

    public String power;
    public String toughness;

    @JsonProperty("color_identity")
    public List<String> colorIdentity;

    @JsonProperty("image_uris")
    public ScryfallImageUris imageUris;

    @JsonProperty("card_faces")
    public List<ScryfallCardFaceJson> cardFaces;

    public String set;

    @JsonProperty("set_name")
    public String setName;

    @JsonProperty("set_type")
    public String setType;

    public Boolean promo;

    @JsonProperty("released_at")
    public String releasedAt;

    public String rarity;

    @JsonProperty("flavor_text")
    public String flavorText;
}
