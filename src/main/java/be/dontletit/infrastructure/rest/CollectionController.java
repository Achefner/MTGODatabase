package be.dontletit.infrastructure.rest;

import be.dontletit.api.CardResponse;
import be.dontletit.api.CollectionApiPort;
import be.dontletit.api.CollectionEntryRequest;
import be.dontletit.api.CollectionResponse;
import be.dontletit.api.CollectionSummaryResponse;
import be.dontletit.api.UnknownCardsException;
import be.dontletit.application.DecklistFormat;
import be.dontletit.domain.CollectionType;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Path("/collections")
@Produces(MediaType.APPLICATION_JSON)
public class CollectionController {

    @Inject
    CollectionApiPort collectionApi;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CollectionRequest request, UriInfo uriInfo) {
        if (request == null) throw new BadRequestException("Missing body");
        CollectionResponse created = collectionApi.create(request.name(), parseType(request.type()), request.entries());
        return created(created, uriInfo);
    }

    /** Imports a plain-text decklist ("4 Lightning Bolt" per line) as a new collection. */
    @POST
    @Path("/import")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response importDecklist(@QueryParam("name") String name, @QueryParam("type") String type, String decklist, UriInfo uriInfo) {
        CollectionResponse created = collectionApi.create(name, parseType(type), DecklistFormat.parse(decklist));
        return created(created, uriInfo);
    }

    @GET
    public List<CollectionSummaryResponse> list() {
        return collectionApi.list();
    }

    @GET
    @Path("/{id}")
    public CollectionResponse get(@PathParam("id") String id) {
        return collectionApi.get(id).orElseThrow(() -> new NotFoundException("Collection not found: " + id));
    }

    /** Exports the collection as a plain-text decklist. */
    @GET
    @Path("/{id}/export")
    @Produces(MediaType.TEXT_PLAIN)
    public String export(@PathParam("id") String id) {
        return DecklistFormat.format(get(id).entries());
    }

    @GET
    @Path("/{id}/cards")
    public List<CardResponse> cards(@PathParam("id") String id) {
        return collectionApi.cards(id).orElseThrow(() -> new NotFoundException("Collection not found: " + id));
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public CollectionResponse replace(@PathParam("id") String id, CollectionRequest request) {
        if (request == null) throw new BadRequestException("Missing body");
        return collectionApi.replace(id, request.name(), parseType(request.type()), request.entries())
                .orElseThrow(() -> new NotFoundException("Collection not found: " + id));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        if (!collectionApi.delete(id)) throw new NotFoundException("Collection not found: " + id);
        return Response.noContent().build();
    }

    private static Response created(CollectionResponse created, UriInfo uriInfo) {
        URI location = uriInfo.getBaseUriBuilder().path("collections").path(created.id()).build();
        return Response.created(location).entity(created).build();
    }

    private static CollectionType parseType(String type) {
        if (type == null) throw new BadRequestException("type is required: DECK, CUBE or MISC");
        try {
            return CollectionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown collection type: " + type + " (expected DECK, CUBE or MISC)");
        }
    }

    @ServerExceptionMapper
    public Response unknownCards(UnknownCardsException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage(), "unknownCards", e.unknownNames()))
                .build();
    }

    @ServerExceptionMapper
    public Response invalidInput(IllegalArgumentException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
    }
}
