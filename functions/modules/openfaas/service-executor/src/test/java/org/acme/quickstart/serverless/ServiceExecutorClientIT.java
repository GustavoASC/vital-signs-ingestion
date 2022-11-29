package org.acme.quickstart.serverless;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import org.acme.quickstart.input.ServiceExecutorInputDto;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ServiceExecutorClientIT {

    private static final int STUBBED_SERVICE_EXECUTOR_PORT = 8595;

    ServiceExecutorClient serviceExecutorClient;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(
            wireMockConfig()
                .port(STUBBED_SERVICE_EXECUTOR_PORT)
                .notifier(new Slf4jNotifier(true)))
        .build();

    @BeforeAll
    static void beforeAll() {
        configureFor(STUBBED_SERVICE_EXECUTOR_PORT);
    }

    @BeforeEach
    public void beforeEach() {
        serviceExecutorClient = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:8595"))
                .build(ServiceExecutorClient.class);
    }

    @ParameterizedTest
    @CsvSource({
        "output-service-executor-empty-json.json",
        "output-service-executor-xml.xml"
    })
    void shouldSendAppropriatePayloadForVerticalOffloadingWithResponse(String responseFile) throws IOException {
        stubFor(
            post("/function/service-executor")
            .withRequestBody(equalToJson(textFromResource("input-service-executor.json")))
            .willReturn(ok(textFromResource(responseFile))));

        ServiceExecutorInputDto input = new ServiceExecutorInputDto(
            "bar-function",
            "{\"heartbeat\": 100}",
            3,
            UUID.fromString("4ae04269-8d88-4fa7-bbcb-34c39cad3601")
        );
        assertThat(serviceExecutorClient.runServiceExecutor(input))
            .isNotNull();
    }

    @Test
    void shouldSendAppropriatePayloadForVerticalOffloadingWithoutResponse() throws IOException {
        stubFor(
            post("/function/service-executor")
            .withRequestBody(equalToJson(textFromResource("input-service-executor.json"))));

        ServiceExecutorInputDto input = new ServiceExecutorInputDto(
            "bar-function",
            "{\"heartbeat\": 100}",
            3,
            UUID.fromString("4ae04269-8d88-4fa7-bbcb-34c39cad3601")
        );
        assertThat(serviceExecutorClient.runServiceExecutor(input))
            .isNull();
    }

    private String textFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/serverless-function-client", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}
