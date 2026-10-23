package be.dontletit.infrastructure.rest;

import be.dontletit.api.CardApiPort;
import be.dontletit.api.CardResponse;
import be.dontletit.api.SetResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/cards")
@Produces(MediaType.APPLICATION_JSON)
public class CardController {

    @Inject
    CardApiPort cardApiPort;

    @GET
    @Path("/names")
    public List<String> names() {
        return cardApiPort.getAllCardNames();
    }

    @GET
    @Path("/sets")
    public List<SetResponse> sets() {
        return cardApiPort.getFirstPrintingSets();
    }

    @GET
    @Path("/random")
    public CardResponse random() {
        return cardApiPort.getRandomCard();
    }

    @GET
    @Path("/random/{count}")
    public List<CardResponse> randomMultiple(@PathParam("count") int count) {
        return cardApiPort.getRandomCards(count);
    }

    @GET
    @Path("/{name}")
    public CardResponse byName(@PathParam("name") String name) {
        return cardApiPort.getCardByName(name)
                .orElseThrow(() -> new NotFoundException("Card not found: " + name));
    }
}
