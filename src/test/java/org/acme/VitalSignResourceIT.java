package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class VitalSignResourceIT {

    @Test
    public void testHelloEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/vital-sign")
        .then()
            .statusCode(202)
            .body(is(""));
    }

}
