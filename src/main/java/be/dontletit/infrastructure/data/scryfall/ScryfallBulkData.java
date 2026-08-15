package be.dontletit.infrastructure.data.scryfall;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScryfallBulkData {

    @JsonProperty("jsonl_download_uri")
    public String downloadUri;

    @JsonProperty("updated_at")
    public String updatedAt;
}
