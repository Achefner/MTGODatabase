package be.dontletit.infrastructure.data.scryfall;

import be.dontletit.infrastructure.data.entity.CardEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@ApplicationScoped
public class ScryfallImportService {

    private static final Logger LOG = Logger.getLogger(ScryfallImportService.class);
    private static final int BATCH_SIZE = 500;
    private static final String BULK_DATA_URI = "https://api.scryfall.com/bulk-data/default-cards";
    private static final String USER_AGENT = "MTGODatabase/1.0 (contact@dontletit.be)";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    EntityManager em;

    @Inject
    ScryfallCardMapper mapper;

    public int importFromScryfall() {
        try {
            HttpClient http = HttpClient.newHttpClient();

            LOG.info("Fetching Scryfall bulk data metadata...");
            ScryfallBulkData bulkData = fetchBulkDataMetadata(http);
            LOG.infof("Downloading from %s (updated %s)", bulkData.downloadUri, bulkData.updatedAt);

            HttpRequest downloadRequest = HttpRequest.newBuilder()
                    .uri(new URI(bulkData.downloadUri))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "*/*")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = http.send(downloadRequest, HttpResponse.BodyHandlers.ofInputStream());
            int count = streamImport(response.body());
            LOG.infof("Import complete: %d cards processed", count);
            return count;

        } catch (Exception e) {
            throw new RuntimeException("Scryfall import failed", e);
        }
    }

    private ScryfallBulkData fetchBulkDataMetadata(HttpClient http) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BULK_DATA_URI))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "*/*")
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), ScryfallBulkData.class);
    }

    private int streamImport(InputStream body) throws Exception {
        int total = 0;
        List<CardEntity> batch = new ArrayList<>(BATCH_SIZE);

        try (var gzip = new GZIPInputStream(body);
             var reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                ScryfallCardJson card = objectMapper.readValue(line, ScryfallCardJson.class);
                if (!mapper.isImportable(card)) {
                    continue;
                }
                batch.add(mapper.toEntity(card));
                total++;

                if (batch.size() >= BATCH_SIZE) {
                    persistBatch(batch);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            persistBatch(batch);
        }

        return total;
    }

    @Transactional
    void persistBatch(List<CardEntity> batch) {
        batch.forEach(em::merge);
        em.flush();
        em.clear();
    }
}
