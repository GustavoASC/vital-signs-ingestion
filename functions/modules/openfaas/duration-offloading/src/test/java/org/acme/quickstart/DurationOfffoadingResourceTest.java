package org.acme.quickstart;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class DurationOfffoadingResourceTest {

    private static final int STUBBED_SERVERLESS_PLATFORM_PORT = 8597;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(
                WireMockConfiguration.wireMockConfig()
                            .port(STUBBED_SERVERLESS_PLATFORM_PORT)
                            .notifier(new Slf4jNotifier(true)))
            .build();

    @BeforeAll
    static void beforeAll() {
        configureFor(STUBBED_SERVERLESS_PLATFORM_PORT);
    }

    @Test
    public void shouldDetectLocalExecution() throws Throwable {

        stubPredictorFunctionForGivenPayloadAndResponse(
            "/duration-predictor/input-one-two-three.json",
            "/duration-predictor/output-forecast-two.json"
        );
        stubPredictorFunctionForGivenPayloadAndResponse(
            "/duration-predictor/input-four-five-six.json",
            "/duration-predictor/output-forecast-five.json"
        );

        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("input-services-with-different-durations.json"))
        .when()
            .post("/")
        .then()
             .statusCode(200)
             .body(is("{\"offloading_decision\":\"RUN_LOCALLY\"}"));

    }

    @Test
    public void shouldNotDetectOffloading() throws Throwable {

        stubPredictorFunctionForGivenPayloadAndResponse(
            "/duration-predictor/input-one-two-three.json",
            "/duration-predictor/output-forecast-two.json"
        );

        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("input-all-services-same-durations.json"))
        .when()
            .post("/")
        .then()
             .statusCode(200)
             .body(is("{\"offloading_decision\":\"UNKNOWN\"}"));
    }

    private void stubPredictorFunctionForGivenPayloadAndResponse(String requestBodyFile, String responseFile) throws IOException {
        stubFor(
            post("/function/predictor")
                .withHost(equalTo("localhost"))
                .withPort(STUBBED_SERVERLESS_PLATFORM_PORT)
                .withRequestBody(equalToJson(jsonFromResource(requestBodyFile)))
                .willReturn(okJson(jsonFromResource(responseFile)))
        );
    }

    private String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/component", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }    

}