package org.acme;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;

@QuarkusTest
public class VitalSignResourceIT {

    private static final int SERVERLESS_PLATFORM_PORT = 8586;
    private static final int VITAL_SIGN_PORT = 8587;

    @RegisterExtension
    static WireMockExtension wmServerless = WireMockExtension.newInstance()
        .options(
            WireMockConfiguration.wireMockConfig()
            .port(SERVERLESS_PLATFORM_PORT)
            .notifier(new Slf4jNotifier(true))
        ).build();

    @RegisterExtension
    static WireMockExtension wmVitalSign = WireMockExtension.newInstance()
        .options(
            WireMockConfiguration.wireMockConfig()
            .port(VITAL_SIGN_PORT)
            .notifier(new Slf4jNotifier(true))
        ).build();

    @InjectMock
    ResourcesLocator resourcesLocator;

    @Test
    public void testSingleVitalSignIngestionOnLocalMachineWithoutSpecificService() throws Throwable {
        
        configureFor(SERVERLESS_PLATFORM_PORT);
        stubFor(
            post("/async-function/foo-function")
                .withHost(equalTo("localhost"))
                .withPort(SERVERLESS_PLATFORM_PORT)
                .withRequestBody(equalToJson("{\"heartbeat\": 100}"))
        );
        
        stubFor(
            post("/async-function/bar-function")
                .withHost(equalTo("localhost"))
                .withPort(SERVERLESS_PLATFORM_PORT)
                .withRequestBody(equalToJson("{\"heartbeat\": 100}"))
        );
        
        when(resourcesLocator.usedCpuPercentage())
            .thenReturn(0);
        
        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("vital-sign-with-user-priority.json"))
        .when()
            .post("/vital-sign")
        .then()
            .statusCode(202)
            .body(is(""));

        verify(1, postRequestedFor(urlEqualTo("/async-function/foo-function")));
        verify(1, postRequestedFor(urlEqualTo("/async-function/bar-function")));
    }

    @Test
    public void testSingleVitalSignIngestionOnRemoteMachineWithoutSpecificService() throws Throwable {
        configureFor(VITAL_SIGN_PORT);

        stubFor(
            post("/vital-sign")
                .withHost(equalTo("localhost"))
                .withPort(VITAL_SIGN_PORT)
        );
        
        when(resourcesLocator.usedCpuPercentage())
            .thenReturn(99);
        
        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("vital-sign-with-user-priority.json"))
        .when()
            .post("/vital-sign")
        .then()
            .statusCode(202)
            .body(is(""));

        verify(1,
            postRequestedFor(urlEqualTo("/vital-sign"))
                .withRequestBody(equalToJson(jsonFromResource("vital-sign-with-foo-service-and-user-priority.json")))
        );
        verify(1,
            postRequestedFor(urlEqualTo("/vital-sign"))
                .withRequestBody(equalToJson(jsonFromResource("vital-sign-with-bar-service-and-user-priority.json")))
        );
        verify(2, postRequestedFor(urlEqualTo("/vital-sign")));
    }

    public String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/vital-sign-resource", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}
