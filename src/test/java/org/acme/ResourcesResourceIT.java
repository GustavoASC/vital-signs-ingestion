package org.acme;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class ResourcesResourceIT {

    @Inject
    ResourcesLocator resourcesLocator;

    @Test
    public void testHelloEndpoint() throws IOException {
        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("used-cpu-percentage.json"))
        .when()
            .post("/resources")
        .then()
            .statusCode(200)
            .body(is(""));

        assertThat(resourcesLocator.getUsedCpuPercentage())
                .isEqualTo(37);
    }

    public String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/resources", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}
