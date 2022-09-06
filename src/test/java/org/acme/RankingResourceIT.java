package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class RankingResourceIT {
    
    @Test
    public void testHelloEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("service_name", "foo")
        .when()
            .post("/ranking/{service_name}")
          .then()
             .statusCode(200)
             .body(is(""));
    }

}
