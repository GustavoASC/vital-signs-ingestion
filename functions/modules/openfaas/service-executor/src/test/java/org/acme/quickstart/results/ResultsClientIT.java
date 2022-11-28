package org.acme.quickstart.results;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ResultsClientIT {

    private static final int STUBBED_MACHINE_RESOURCES_PORT = 8678;

    @Inject
    @RestClient
    ResultsClient resultsClient;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(
            wireMockConfig()
                .port(STUBBED_MACHINE_RESOURCES_PORT)
                .notifier(new Slf4jNotifier(true)))
        .build();

    @BeforeAll
    static void beforeAll() {
        configureFor(STUBBED_MACHINE_RESOURCES_PORT);
    }

    @Test
    public void shouldReturnMachineResources() throws Throwable {
        stubFor(
            patch(urlEqualTo("/results/19b23bbe-be83-4204-ae65-f1390cb2e141"))
                .withRequestBody(equalToJson(jsonFromResource(("input-results.json")))));

        Results results = new Results();
        results.initialServiceTimestamp = 1669648346608l;
        results.endTimestamp = 1669648346808l;
        
        assertThat(resultsClient.sendResults("19b23bbe-be83-4204-ae65-f1390cb2e141", results))
            .isNull();
    }

    private String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/results", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}
