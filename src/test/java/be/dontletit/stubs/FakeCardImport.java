package be.dontletit.stubs;

import be.dontletit.api.CardImportApiPort;
import be.dontletit.api.ImportResultResponse;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

/** In @QuarkusTest runs the import never downloads Scryfall: it reports 42 printings. */
@Alternative
@Priority(1)
@ApplicationScoped
public class FakeCardImport implements CardImportApiPort {

    public static final int IMPORTED = 42;

    @Override
    public ImportResultResponse importCatalog() {
        return new ImportResultResponse(IMPORTED);
    }
}
