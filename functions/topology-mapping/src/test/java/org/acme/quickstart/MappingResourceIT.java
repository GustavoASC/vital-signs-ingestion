package org.acme.quickstart;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import javax.inject.Inject;

import org.acme.quickstart.restclient.MappingProviderClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class MappingResourceIT {

    private static final int STUBBED_MAPPING_PROVIDER = 9234;

    @Inject
    @RestClient
    MappingProviderClient mappingProviderClient;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(
                    wireMockConfig()
                            .port(STUBBED_MAPPING_PROVIDER)
                            .notifier(new Slf4jNotifier(true)))
            .build();

    @BeforeAll
    static void beforeAll() {
        configureFor(STUBBED_MAPPING_PROVIDER);
    }

    @Test
    public void shouldReturnMappingForConfiuredAlias() throws Throwable {

        stubFor(
            get("/nodes-mappings.properties")
            .willReturn(ok(textFromResource("nodes-mappings.properties")))
        );

        given().when()
            .get("/")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(is(textFromResource("response-with-mapping.json")));
    }

    private String textFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/component-test", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}
