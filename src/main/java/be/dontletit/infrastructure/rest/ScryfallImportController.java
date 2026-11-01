package be.dontletit.infrastructure.rest;

import be.dontletit.api.CardImportApiPort;
import be.dontletit.api.ImportResultResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin/scryfall")
@Produces(MediaType.APPLICATION_JSON)
public class ScryfallImportController {

    @Inject
    CardImportApiPort cardImport;

    /** Downloads Scryfall's bulk data and stores every printing: `{"imported": n}`. Takes a few minutes. */
    @POST
    @Path("/import")
    public ImportResultResponse triggerImport() {
        return cardImport.importCatalog();
    }
}
