package be.dontletit.infrastructure.rest;

import be.dontletit.infrastructure.data.entity.CardEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import be.dontletit.infrastructure.data.PostgresCardRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CollectionControllerTest {

    @Inject
    EntityManager em;

    @Inject
    PostgresCardRepository cards;

    @BeforeEach
    @Transactional
    void seedCards() {
        em.createQuery("DELETE FROM CollectionEntity").executeUpdate();
        em.createQuery("DELETE FROM CardEntity").executeUpdate();
        for (String name : List.of("Black Lotus", "Lightning Bolt", "Ancestral Recall", "Brazen Borrower // Petty Theft")) {
            CardEntity e = new CardEntity();
            e.id = name.toLowerCase().replace(' ', '-');
            e.name = name;
            e.types = List.of("INSTANT");
            e.surtypes = List.of();
            e.subtypes = List.of();
            e.sets = List.of("lea");
            e.colorIdentity = "";
            e.manaCost = "{0}";
            em.persist(e);
        }
        cards.invalidate();  // these cards were written behind the repository's back
    }

    @Test
    void createFromJson_thenGet_export_andList() {
        String id = given()
                .contentType("application/json")
                .body("""
                        {"name": "Power LSV Cube", "type": "CUBE",
                         "entries": [{"name": "black lotus"}, {"name": "Lightning Bolt", "quantity": 1}]}
                        """)
                .when().post("/collections")
                .then().statusCode(201)
                .header("Location", containsString("/collections/"))
                .body("name", equalTo("Power LSV Cube"))
                .body("type", equalTo("CUBE"))
                .body("cardCount", equalTo(2))
                .body("entries[0].cardName", equalTo("Black Lotus"))
                .extract().path("id");

        given().when().get("/collections/" + id)
                .then().statusCode(200)
                .body("id", equalTo(id))
                .body("entries.size()", equalTo(2));

        given().accept("text/plain").when().get("/collections/" + id + "/export")
                .then().statusCode(200)
                .contentType(containsString("text/plain"))
                .body(equalTo("1 Black Lotus\n1 Lightning Bolt\n"));

        given().when().get("/collections")
                .then().statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(id))
                .body("[0].name", equalTo("Power LSV Cube"))
                .body("[0].type", equalTo("CUBE"))
                .body("[0].cardCount", equalTo(2));
    }

    @Test
    void importDecklistText_createsTheCollection() {
        given()
                .contentType("text/plain")
                .queryParam("name", "Burn")
                .queryParam("type", "DECK")
                .body("4 Lightning Bolt\n1 Brazen Borrower\n")
                .when().post("/collections/import")
                .then().statusCode(201)
                .body("name", equalTo("Burn"))
                .body("type", equalTo("DECK"))
                .body("cardCount", equalTo(5))
                .body("entries[1].cardName", equalTo("Brazen Borrower // Petty Theft"));
    }

    @Test
    void unknownCards_are400WithTheirNames() {
        given()
                .contentType("text/plain")
                .queryParam("name", "Bad")
                .queryParam("type", "CUBE")
                .body("1 Black Lotus\n1 Mox Pearl\n1 Timetwister\n")
                .when().post("/collections/import")
                .then().statusCode(400)
                .body("unknownCards", contains("Mox Pearl", "Timetwister"));
    }

    @Test
    void invalidTypeOrBlankName_are400() {
        given().contentType("application/json")
                .body("{\"name\": \"X\", \"type\": \"BINDER\", \"entries\": []}")
                .when().post("/collections")
                .then().statusCode(400);

        given().contentType("text/plain").queryParam("name", " ").queryParam("type", "CUBE").body("1 Black Lotus")
                .when().post("/collections/import")
                .then().statusCode(400);
    }

    @Test
    void replace_andDelete() {
        String id = given().contentType("application/json")
                .body("{\"name\": \"A\", \"type\": \"CUBE\", \"entries\": [{\"name\": \"Black Lotus\"}]}")
                .when().post("/collections").then().statusCode(201).extract().path("id");

        given().contentType("application/json")
                .body("{\"name\": \"A2\", \"type\": \"DECK\", \"entries\": [{\"name\": \"Lightning Bolt\", \"quantity\": 4}]}")
                .when().put("/collections/" + id)
                .then().statusCode(200)
                .body("name", equalTo("A2"))
                .body("cardCount", equalTo(4));

        given().when().get("/collections/" + id + "/cards")
                .then().statusCode(200)
                .body("size()", equalTo(4))
                .body("[0].name", equalTo("Lightning Bolt"));

        given().when().delete("/collections/" + id).then().statusCode(204);
        given().when().delete("/collections/" + id).then().statusCode(404);
        given().when().get("/collections/" + id).then().statusCode(404);
        given().contentType("application/json")
                .body("{\"name\": \"A3\", \"type\": \"DECK\", \"entries\": []}")
                .when().put("/collections/" + id).then().statusCode(404);
    }
}
