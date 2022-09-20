package org.acme;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
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
public class ServerlessFunctionClientIT {

    private static final int STUBBED_SERVERLESS_PLATFORM_PORT = 8586;

    @Inject
    @RestClient
    ServerlessFunctionClient serverlessFunctionClient;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(
                    wireMockConfig()
                            .port(STUBBED_SERVERLESS_PLATFORM_PORT)
                            .notifier(new Slf4jNotifier(true)))
            .build();

    @BeforeAll
    static void beforeAll() {
        configureFor(STUBBED_SERVERLESS_PLATFORM_PORT);
    }

    @Test
    void shouldNotReturnAnyContentForAGivenServerlessFunction() throws IOException {
        stubFor(
                post("/function/foo-fn")
                        .withHost(equalTo("localhost")));

        var result = serverlessFunctionClient.runFunction("foo-fn", "{\"abc\": 10}");

        assertThat(result)
                .isNull();
    }

    @Test
    void shouldReturnJsonContentForAGivenServerlessFunction() throws IOException {
        stubFor(
                post("/function/foo-fn")
                        .withHost(equalTo("localhost"))
                        .willReturn(okJson(jsonFromResource("output-sample-fn-response.json"))));

        var result = serverlessFunctionClient.runFunction("foo-fn", "{\"abc\": 10}");

        assertThat(result)
                .isEqualTo(jsonFromResource("output-sample-fn-response.json"));
    }

    private String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/serverless-function-client", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}
