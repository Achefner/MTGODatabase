package be.dontletit.infrastructure.rest;

import be.dontletit.stubs.FakeCardImport;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/** The import port is replaced by {@link FakeCardImport} in tests. */
@QuarkusTest
class ScryfallImportControllerTest {

    @Test
    void import_goesThroughTheImportPort_andReportsTheCount() {
        given().when().post("/admin/scryfall/import")
                .then().statusCode(200)
                .body("imported", equalTo(FakeCardImport.IMPORTED));
    }
}
