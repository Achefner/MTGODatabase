package be.dontletit.application;

import be.dontletit.api.ImportResultResponse;
import be.dontletit.spi.CardCatalogSpiPort;
import be.dontletit.spi.CardPrintingData;
import be.dontletit.stubs.CardSpiStub;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class CardImportApiAdapterTest {

    static CardPrintingData printing(int i) {
        return new CardPrintingData("id-" + i, "Card " + i, "{1}", List.of("INSTANT"), List.of(), List.of(), "", null, null,
                "", "set", "Set", "expansion", false, LocalDate.of(2000, 1, 1), "COMMON", null);
    }

    /** A catalog of {@code size} printings. */
    static CardCatalogSpiPort catalog(int size) {
        return (Consumer<CardPrintingData> sink) -> IntStream.range(0, size).mapToObj(CardImportApiAdapterTest::printing).forEach(sink);
    }

    @Test
    void importCatalog_savesEveryPrintingInBatches_andCountsThem() {
        CardSpiStub cards = new CardSpiStub();

        ImportResultResponse result = new CardImportApiAdapter(catalog(1_200), cards).importCatalog();

        assertEquals(new ImportResultResponse(1_200), result);
        assertEquals(List.of(500, 500, 200), cards.savedBatches.stream().map(List::size).toList());
        assertEquals("id-0", cards.savedBatches.get(0).get(0).id());
        assertEquals("id-1199", cards.savedBatches.get(2).get(199).id());
    }

    @Test
    void importCatalog_emptyCatalog_savesNothing() {
        CardSpiStub cards = new CardSpiStub();

        assertEquals(new ImportResultResponse(0), new CardImportApiAdapter(catalog(0), cards).importCatalog());
        assertTrue(cards.savedBatches.isEmpty());
    }

    @Test
    void importCatalog_aFailingCatalog_keepsWhatWasSavedAndFails() {
        CardSpiStub cards = new CardSpiStub();
        CardCatalogSpiPort broken = sink -> {
            IntStream.range(0, 600).mapToObj(CardImportApiAdapterTest::printing).forEach(sink);
            throw new IllegalStateException("download interrupted");
        };

        assertThrows(IllegalStateException.class, () -> new CardImportApiAdapter(broken, cards).importCatalog());
        assertEquals(List.of(500), cards.savedBatches.stream().map(List::size).toList());
    }
}
