package org.acme.quickstart.metrics;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
public class MetricsClientIT {

    private static final int STUBBED_MACHINE_RESOURCES_PORT = 8674;

    @Inject
    @RestClient
    MetricsClient metricsClient;

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
            post("/metrics")
                .withRequestBody(equalToJson(jsonFromResource(("input-metrics.json")))));

        Metrics metrics = new Metrics();
        metrics.function = "foo-function";
        metrics.usedCpu = new BigDecimal("96");
        metrics.lastCpuObservation = new BigDecimal("18.090625000000003");
        metrics.cpuCollectionTimestamp = 1663527788128l;
        metrics.userPriority = 5;
        metrics.ranking = 8;
        metrics.offloading = true;
        
        assertThat(metricsClient.sendMetrics(metrics))
            .isNull();
    }

    private String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/metrics", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}
