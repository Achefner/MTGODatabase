package be.dontletit.application;

import be.dontletit.api.CardImportApiPort;
import be.dontletit.api.ImportResultResponse;
import be.dontletit.spi.CardCatalogSpiPort;
import be.dontletit.spi.CardPrintingData;
import be.dontletit.spi.CardSpiPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/** Streams the catalog into the card store, a batch at a time, so the ~110k printings never sit in memory at once. */
@ApplicationScoped
public class CardImportApiAdapter implements CardImportApiPort {

    static final int BATCH_SIZE = 500;

    private final CardCatalogSpiPort catalog;
    private final CardSpiPort cards;

    @Inject
    public CardImportApiAdapter(CardCatalogSpiPort catalog, CardSpiPort cards) {
        this.catalog = catalog;
        this.cards = cards;
    }

    @Override
    public ImportResultResponse importCatalog() {
        List<CardPrintingData> batch = new ArrayList<>(BATCH_SIZE);
        int[] total = {0};
        catalog.readPrintings(printing -> {
            batch.add(printing);
            total[0]++;
            if (batch.size() == BATCH_SIZE) {
                cards.savePrintings(List.copyOf(batch));
                batch.clear();
            }
        });
        if (!batch.isEmpty()) cards.savePrintings(List.copyOf(batch));
        return new ImportResultResponse(total[0]);
    }
}
