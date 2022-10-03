package org.acme.quickstart.serverless;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.acme.quickstart.input.ServiceExecutorInputDto;
import org.acme.quickstart.offloading.duration.OffloadDurationInputDto;
import org.acme.quickstart.offloading.duration.OffloadDurationOutputDto;
import org.acme.quickstart.offloading.duration.PreviousDurationInputDto;
import org.acme.quickstart.offloading.ranking.OffloadRankingInputDto;
import org.acme.quickstart.offloading.ranking.OffloadRankingOutputDto;
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
        stubFor(post("/function/foo-fn"));

        assertThat(serverlessFunctionClient.runFunction("foo-fn", jsonFromResource("input-sample-fn-response.json")))
            .isNull();
    }

    @Test
    void shouldReturnJsonContentForAGivenServerlessFunction() throws IOException {
        stubFor(
            post("/function/foo-fn")
                    .willReturn(okJson(jsonFromResource("output-sample-fn-response.json"))));

        assertThat(serverlessFunctionClient.runFunction("foo-fn", jsonFromResource("input-sample-fn-response.json")))
            .isEqualTo(jsonFromResource("output-sample-fn-response.json"));
    }

    @Test
    void shouldSendAppropriatePayloadForVerticalOffloading() throws IOException {
        stubFor(
            post("/function/service-executor")
            .withRequestBody(equalToJson(jsonFromResource("input-service-executor.json"))));

        ServiceExecutorInputDto input = new ServiceExecutorInputDto(
            "bar-function",
            "{\"heartbeat\": 100}",
            3
        );
        assertThat(serverlessFunctionClient.runServiceExecutor("service-executor", input))
            .isNull();
    }

    @Test
    void shouldSerializeInputEvenWhenRankingsListIsEmpty() throws IOException {
        stubFor(
            post("/function/ranking-offloading")
            .withRequestBody(equalToJson(jsonFromResource("input-ranking-heuristic-empty-rankings.json")))
            .willReturn(okJson(jsonFromResource("output-ranking-heuristic.json"))));

        OffloadRankingInputDto input = new OffloadRankingInputDto(
            Collections.emptyList(),
            2
        );
        assertThat(serverlessFunctionClient.runRankingOffloading("ranking-offloading", input))
            .isEqualTo(new OffloadRankingOutputDto("OFFLOAD"));
    }

    @Test
    void shouldReturnOffloadAsDecisionForRankingHeuristic() throws IOException {
        stubFor(
            post("/function/ranking-offloading")
            .withRequestBody(equalToJson(jsonFromResource("input-ranking-heuristic.json")))
            .willReturn(okJson(jsonFromResource("output-ranking-heuristic.json"))));

        OffloadRankingInputDto input = new OffloadRankingInputDto(
            List.of(7, 7, 7, 7, 7, 7, 7),
            2
        );
        assertThat(serverlessFunctionClient.runRankingOffloading("ranking-offloading", input))
            .isEqualTo(new OffloadRankingOutputDto("OFFLOAD"));
    }

    @Test
    void shouldReturnOffloadAsDecisionForDurationHeuristic() throws IOException {
        stubFor(
            post("/function/duration-offloading")
            .withRequestBody(equalToJson(jsonFromResource("input-duration-heuristic.json")))
            .willReturn(okJson(jsonFromResource("output-duration-heuristic.json"))));

        OffloadDurationInputDto input = new OffloadDurationInputDto(
            List.of(
                new PreviousDurationInputDto("foo", List.of(1l, 2l, 3l)),
                new PreviousDurationInputDto("bar", List.of(4l, 5l, 6l))
            ),
            "foo"
        );
        assertThat(serverlessFunctionClient.runOffloadingDuration("duration-offloading", input))
            .isEqualTo(new OffloadDurationOutputDto("OFFLOAD"));
    }

    private String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/serverless-function-client", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}
