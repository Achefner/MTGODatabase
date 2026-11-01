package be.dontletit.infrastructure.data.scryfall;

import be.dontletit.spi.CardCatalogSpiPort;
import be.dontletit.spi.CardPrintingData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/** Scryfall's "default cards" bulk data: downloaded, read line by line, mapped to printings. */
@ApplicationScoped
public class ScryfallCatalog implements CardCatalogSpiPort {

    private static final Logger LOG = Logger.getLogger(ScryfallCatalog.class);
    private static final String BULK_DATA_URI = "https://api.scryfall.com/bulk-data/default-cards";
    private static final String USER_AGENT = "MTGODatabase/1.0 (contact@dontletit.be)";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ScryfallCardMapper mapper;

    @Override
    public void readPrintings(Consumer<CardPrintingData> sink) {
        try {
            HttpClient http = HttpClient.newHttpClient();
            LOG.info("Fetching Scryfall bulk data metadata...");
            ScryfallBulkData bulkData = objectMapper.readValue(get(http, BULK_DATA_URI, HttpResponse.BodyHandlers.ofString()), ScryfallBulkData.class);
            LOG.infof("Downloading from %s (updated %s)", bulkData.downloadUri, bulkData.updatedAt);
            int count = read(get(http, bulkData.downloadUri, HttpResponse.BodyHandlers.ofInputStream()), sink);
            LOG.infof("Read %d printings", count);
        } catch (IOException e) {
            throw new IllegalStateException("Scryfall import failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Scryfall import interrupted", e);
        }
    }

    private static <T> T get(HttpClient http, String uri, HttpResponse.BodyHandler<T> handler) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "*/*")
                .GET()
                .build();
        return http.send(request, handler).body();
    }

    private int read(InputStream body, Consumer<CardPrintingData> sink) throws IOException {
        int count = 0;
        try (var gzip = new GZIPInputStream(body);
             var reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                ScryfallCardJson card = objectMapper.readValue(line, ScryfallCardJson.class);
                if (!mapper.isImportable(card)) continue;
                sink.accept(mapper.toPrinting(card));
                count++;
            }
        }
        return count;
    }
}
