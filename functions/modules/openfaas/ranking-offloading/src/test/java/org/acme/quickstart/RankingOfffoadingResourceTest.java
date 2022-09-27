package org.acme.quickstart;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class RankingOfffoadingResourceTest {

    @Test
    public void shouldDetectOffloading() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"all_rankings\": [1, 3, 6, 3, 7, 8, 2], \"calculated_ranking\": 15}")
        .when()
            .post("/")
        .then()
             .statusCode(200)
             .body(is("{\"offloading_decision\":\"RUN_LOCALLY\"}"));
    }

    @Test
    public void shouldDetectLocalExecution() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"all_rankings\": [7, 7, 7, 7, 7, 7, 7], \"calculated_ranking\": 2}")
        .when()
            .post("/")
        .then()
             .statusCode(200)
             .body(is("{\"offloading_decision\":\"OFFLOAD\"}"));
    }

    @Test
    public void shouldNotDetectOffloading() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"all_rankings\": [7, 7, 7, 7, 7, 7, 7], \"calculated_ranking\": 7}")
        .when()
            .post("/")
        .then()
             .statusCode(200)
             .body(is("{\"offloading_decision\":\"UNKNOWN\"}"));
    }

}