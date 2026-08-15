package be.dontletit.infrastructure.rest;

import be.dontletit.infrastructure.data.scryfall.ScryfallImportService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin/scryfall")
@Produces(MediaType.APPLICATION_JSON)
public class ScryfallImportController {

    @Inject
    ScryfallImportService importService;

    @POST
    @Path("/import")
    public Response triggerImport() {
        int count = importService.importFromScryfall();
        return Response.ok("{\"imported\": " + count + "}").build();
    }
}
